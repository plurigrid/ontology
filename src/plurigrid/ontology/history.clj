(ns plurigrid.ontology.history
  "A deterministic random walk over the complete reachable Git commit DAG.

  GitHub is queried through `gh api graphql`; the resulting immutable Clojure
  value is navigated with Specter.  Public functions accept an injected
  `:gh-runner`, so the walk and every error path can be exercised off-line."
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [com.rpl.specter :as sp])
  (:import (java.math BigInteger)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path StandardCopyOption)
           (java.security MessageDigest)))

(def repository "plurigrid/ontology")
(def organization "plurigrid")
(def default-seed 21211)
(def default-steps 24)
(def default-cache-file ".cache/ontology-history.edn")
(def default-org-cache-file ".cache/plurigrid-org.edn")
(def max-page-size 100)
(def default-org-page-size 25)

(def ^:private organization-query
  "query($org:String!,$cursor:String,$pageSize:Int!){
     organization(login:$org){
       login name url
       repositories(first:$pageSize,after:$cursor,orderBy:{field:NAME,direction:ASC}){
         totalCount pageInfo{hasNextPage endCursor}
         nodes{nameWithOwner name url description createdAt updatedAt pushedAt
               isArchived isFork isEmpty isPrivate visibility diskUsage
               defaultBranchRef{name target{... on Commit{
                 oid abbreviatedOid committedDate messageHeadline history(first:1){totalCount}}}}}
       }
     }
     rateLimit{cost remaining resetAt nodeCount}
   }")

(def ^:private org-commit-query
  "query($owner:String!,$name:String!,$oid:GitObjectID!){
     repository(owner:$owner,name:$name){
       nameWithOwner url
       object(oid:$oid){__typename ... on Commit{
         oid abbreviatedOid committedDate messageHeadline additions deletions changedFilesIfAvailable
         author{name email user{login}} committer{name email user{login}}
         parents(first:100){totalCount nodes{oid abbreviatedOid committedDate messageHeadline}} tree{oid}}}
     }
     rateLimit{cost remaining resetAt nodeCount}
   }")

(def ^:private history-query
  "query($owner:String!,$name:String!,$cursor:String){
     repository(owner:$owner,name:$name){
       nameWithOwner url defaultBranchRef{name}
       refs(refPrefix:\"refs/heads/\",first:100){totalCount nodes{name target{... on Commit{oid}}}}
       tags:refs(refPrefix:\"refs/tags/\",first:100){totalCount nodes{name target{__typename ... on Commit{oid} ... on Tag{target{__typename ... on Commit{oid}}}}}}
       pullRequests(first:100){totalCount nodes{number title state url createdAt closedAt mergedAt
                                    commits(first:100){totalCount nodes{commit{oid}}}}}
       object(expression:\"HEAD\"){... on Commit{
         history(first:100,after:$cursor){
           totalCount pageInfo{hasNextPage endCursor}
           nodes{oid abbreviatedOid committedDate messageHeadline additions deletions changedFilesIfAvailable
                 author{name email user{login}} committer{name email user{login}}
                 parents(first:100){totalCount nodes{oid}} tree{oid}}
         }
       }}
     }
     rateLimit{cost remaining resetAt}
   }")

(def ^:private commit-query
  "query($owner:String!,$name:String!,$oid:GitObjectID!){
     repository(owner:$owner,name:$name){
       object(oid:$oid){__typename ... on Commit{
         oid abbreviatedOid committedDate messageHeadline additions deletions changedFilesIfAvailable
         author{name email user{login}} committer{name email user{login}}
         parents(first:100){totalCount nodes{oid}} tree{oid}}}
     }
     rateLimit{cost remaining resetAt}
   }")

(defn- sha256
  [s]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes (str s) StandardCharsets/UTF_8))]
    (format "%064x" (BigInteger. 1 digest))))

(defn- parse-repository
  [repo]
  (let [[owner name & extra] (str/split (str repo) #"/")]
    (when (or (str/blank? owner) (str/blank? name) (seq extra))
      (throw (ex-info "Repository must be OWNER/NAME"
                      {:type ::invalid-repository, :repository repo})))
    [owner name]))

(defn- parse-long-value
  [label value]
  (try (cond (integer? value) (long value)
             (re-matches #"(?i)0x[0-9a-f]+" (str value))
               (.longValue (BigInteger. (subs (str value) 2) 16))
             :else (Long/parseLong (str value)))
       (catch Exception cause
         (throw (ex-info (str "Invalid " label ": " value)
                         {:type ::invalid-option, :option label, :value value}
                         cause)))))

(defn- canonical-data
  [value]
  (cond (map? value)
          (into (sorted-map) (map (fn [[k v]] [k (canonical-data v)])) value)
        (set? value) (mapv canonical-data (sort-by pr-str value))
        (sequential? value) (mapv canonical-data value)
        :else value))

(defn- same-canonical-data?
  [left right]
  (= (canonical-data left) (canonical-data right)))

(defn- default-gh-runner
  [args]
  (let [process (-> (ProcessBuilder. ^java.util.List
                                     (mapv str (cons "gh" args)))
                    (.redirectErrorStream false)
                    (.start))
        stdout (future (slurp (.getInputStream process)))
        stderr (future (slurp (.getErrorStream process)))
        exit (.waitFor process)]
    {:exit exit, :out @stdout, :err @stderr}))

(defn- graphql-call
  [gh-runner query variables]
  (let [variable-args (mapcat (fn [[key value]]
                                (when (some? value)
                                  ["-F"
                                   (str (clojure.core/name key) "=" value)]))
                        variables)
        args (into ["api" "graphql" "-f" (str "query=" query)] variable-args)
        {:keys [exit out err], :as result} (gh-runner args)]
    (when-not (zero? (long (or exit -1)))
      (throw (ex-info "gh api graphql failed"
                      {:type ::gh-failed,
                       :exit exit,
                       :stderr (str/trim (str err)),
                       :args (vec (take 8 args))})))
    (let [response (try (json/read-str out :key-fn keyword)
                        (catch Exception cause
                          (throw (ex-info "gh returned unreadable JSON"
                                          {:type ::invalid-response,
                                           :stdout
                                             (subs (str out)
                                                   0
                                                   (min 500 (count (str out))))}
                                          cause))))]
      (when-let [errors (:errors response)]
        (throw (ex-info "GitHub GraphQL returned errors"
                        {:type ::graphql-errors, :errors errors})))
      {:result result, :response response})))

(defn- graphql-organization-page
  [gh-runner org cursor page-size]
  (graphql-call gh-runner
                organization-query
                {:org org, :cursor cursor, :pageSize page-size}))

(defn- graphql-org-commit
  [gh-runner owner name oid]
  (:response (graphql-call gh-runner
                           org-commit-query
                           {:owner owner, :name name, :oid oid})))

(defn- graphql-page
  [gh-runner owner name cursor]
  (graphql-call gh-runner
                history-query
                {:owner owner, :name name, :cursor cursor}))

(defn- graphql-commit
  [gh-runner owner name oid]
  (get-in (:response (graphql-call gh-runner
                                   commit-query
                                   {:owner owner, :name name, :oid oid}))
          [:data :repository :object]))

(defn- history-page
  [response]
  (get-in response [:data :repository :object :history]))

(defn- ref-target-oid
  [ref]
  (or (get-in ref [:target :oid]) (get-in ref [:target :target :oid])))

(defn- validate-complete-parents
  [commit context]
  (let [parents (:parents commit)
        reported (:totalCount parents)
        nodes (:nodes parents)
        fetched (when (sequential? nodes) (count nodes))]
    (when-not (and (integer? reported) (some? fetched) (= reported fetched))
      (throw (ex-info "Commit parent list exceeded the GraphQL page"
                      (merge {:type ::truncated-parents,
                              :oid (:oid commit),
                              :reported reported,
                              :fetched fetched}
                             context))))
    commit))

(defn- complete-connection-nodes
  [connection label context]
  (let [reported (:totalCount connection)
        nodes (:nodes connection)
        fetched (when (sequential? nodes) (count nodes))]
    (when-not (and (map? connection)
                   (integer? reported)
                   (some? fetched)
                   (= reported fetched))
      (throw (ex-info (str "GitHub " label " list was missing or truncated")
                      (merge {:type ::incomplete-connection,
                              :connection label,
                              :reported reported,
                              :fetched fetched}
                             context))))
    nodes))

(defn- fetch-commit-closure
  "Complete every parent chain rooted at ROOT-OIDS, stopping at KNOWN commits."
  [runner owner name known root-oids]
  (loop [queue (into clojure.lang.PersistentQueue/EMPTY
                     (sort (distinct (remove str/blank? root-oids))))
         by-oid known
         fetched []]
    (if (empty? queue)
      {:by-oid by-oid, :fetched-oids fetched}
      (let [oid (peek queue)
            queue (pop queue)]
        (if (contains? by-oid oid)
          (recur queue by-oid fetched)
          (let [commit (graphql-commit runner owner name oid)]
            (when-not (= "Commit" (:__typename commit))
              (throw (ex-info "Referenced commit could not be fetched"
                              {:type ::commit-not-found, :oid oid})))
            (let [commit (-> commit
                             (validate-complete-parents
                               {:repository (str owner "/" name)}))
                  commit (dissoc commit :__typename)
                  parents (->> (get-in commit [:parents :nodes])
                               (map :oid)
                               (remove #(or (str/blank? %)
                                            (contains? by-oid %)))
                               sort)]
              (recur (into queue parents)
                     (assoc by-oid oid commit)
                     (conj fetched oid)))))))))

(defn- normalize-organization-repository
  [repo]
  (let [full (:nameWithOwner repo)
        head (get-in repo [:defaultBranchRef :target])
        head-oid (or (:head-oid repo) (:oid head))]
    (assoc repo
      :head-oid head-oid
      :head-key (when (and (not (str/blank? full)) (not (str/blank? head-oid)))
                  [full head-oid])
      :default-branch (or (:default-branch repo)
                          (get-in repo [:defaultBranchRef :name]))
      :default-branch-commits (or (:default-branch-commits repo)
                                  (get-in head [:history :totalCount])))))

(defn fetch-organization
  "Fetch a complete, name-ordered repository catalog for a GitHub organization.

  Options:
  - `:organization` organization login (default plurigrid)
  - `:gh-runner` injectable gh process function
  - `:page-size` repositories per query (default 25, maximum 100)
  - `:max-pages` pagination safety bound (default 10000)

  Empty repositories are retained with nil `:head-oid`; each non-empty repo
  has a namespaced head key `[nameWithOwner oid]`, preventing shared fork OIDs
  from collapsing into one node. Duplicate repositories at page boundaries are
  removed by `:nameWithOwner`, and GitHub order is normalized by that key."
  ([] (fetch-organization {}))
  ([{:keys [organization gh-runner page-size max-pages],
     :or {organization organization,
          gh-runner default-gh-runner,
          page-size default-org-page-size,
          max-pages 10000}}]
   (when (str/blank? (str organization))
     (throw (ex-info "Organization login must not be blank"
                     {:type ::invalid-organization,
                      :organization organization})))
   (when-not (and (integer? page-size) (<= 1 page-size max-page-size))
     (throw (ex-info
              "Organization page-size must be an integer from 1 through 100"
              {:type ::invalid-option, :option "page-size", :value page-size})))
   (loop [cursor nil
          pages 0
          org-meta nil
          reported-total nil
          repos {}
          rates []]
     (when (>= pages max-pages)
       (throw (ex-info "Organization pagination exceeded max-pages"
                       {:type ::pagination-limit,
                        :max-pages max-pages,
                        :cursor cursor})))
     (let [{:keys [response]}
             (graphql-organization-page gh-runner organization cursor page-size)
           org (get-in response [:data :organization])]
       (when (nil? org)
         (throw (ex-info "GitHub organization was not found or is inaccessible"
                         {:type ::organization-not-found,
                          :organization organization})))
       (let [connection (:repositories org)
             page-info (:pageInfo connection)]
         (when-not (and (map? connection)
                        (integer? (:totalCount connection))
                        (map? page-info)
                        (boolean? (:hasNextPage page-info))
                        (sequential? (:nodes connection)))
           (throw
             (ex-info
               "GitHub organization response was missing repository pagination data"
               {:type ::invalid-response, :organization organization})))
         (let
           [page-org-meta (select-keys org [:login :name :url])
            page-total (:totalCount connection)
            nodes (:nodes connection)
            repos
              (reduce
                (fn [acc repo]
                  (let [full (:nameWithOwner repo)
                        normalized (normalize-organization-repository repo)
                        existing (get acc full)]
                    (if (str/blank? full)
                      acc
                      (if (and existing
                               (not (same-canonical-data? existing normalized)))
                        (throw
                          (ex-info
                            "Organization repository changed during pagination"
                            {:type ::catalog-entry-changed,
                             :organization organization,
                             :repository full,
                             :previous existing,
                             :current normalized}))
                        (assoc acc full normalized)))))
                repos
                nodes)
            has-next? (true? (:hasNextPage page-info))
            next-cursor (:endCursor page-info)
            rates (conj rates (get-in response [:data :rateLimit]))]
           (when (and reported-total (not= reported-total page-total))
             (throw (ex-info
                      "Organization repository count changed during pagination"
                      {:type ::catalog-count-mismatch,
                       :organization organization,
                       :previous reported-total,
                       :reported page-total,
                       :fetched (count repos)})))
           (when (and org-meta (not= (:login org-meta) (:login page-org-meta)))
             (throw (ex-info "Organization identity changed during pagination"
                             {:type ::organization-changed,
                              :requested organization,
                              :previous (:login org-meta),
                              :current (:login page-org-meta)})))
           (when (and has-next?
                      (or (str/blank? next-cursor) (= cursor next-cursor)))
             (throw (ex-info "Organization pagination did not advance"
                             {:type ::stalled-pagination,
                              :cursor cursor,
                              :next-cursor next-cursor})))
           (if has-next?
             (recur next-cursor
                    (inc pages)
                    (or org-meta page-org-meta)
                    (or reported-total page-total)
                    repos
                    rates)
             (let [repositories (->> (vals repos)
                                     (sort-by :nameWithOwner)
                                     vec)
                   meta* (merge org-meta page-org-meta)
                   reported (or reported-total page-total)]
               (when-not (= reported (count repositories))
                 (throw
                   (ex-info
                     "Organization repository count changed during pagination"
                     {:type ::catalog-count-mismatch,
                      :organization (:login meta*),
                      :reported reported,
                      :fetched (count repositories)})))
               {:schema-version 1,
                :kind :github-organization,
                :organization (:login meta*),
                :name (:name meta*),
                :url (:url meta*),
                :fetched-at (str (java.time.Instant/now)),
                :pages (inc pages),
                :reported-repositories reported,
                :repositories repositories,
                :by-name
                  (into {} (map (juxt :nameWithOwner identity)) repositories),
                :rate-limit rates}))))))))

(defn organization-repositories
  "Select organization repositories with a Specter path.

  With no path, returns all repositories. Example:
  `(organization-repositories catalog [sp/ALL #(not (:isFork %))])`."
  ([catalog] (:repositories catalog))
  ([catalog path] (sp/select path (:repositories catalog))))

(defn organization-summary
  [catalog]
  (let [repos (mapv normalize-organization-repository (:repositories catalog))
        commit-counts (keep :default-branch-commits repos)]
    {:organization (:organization catalog),
     :repositories (count repos),
     :reported-repositories (:reported-repositories catalog),
     :source-repositories (count (remove :isFork repos)),
     :forks (count (filter :isFork repos)),
     :archived (count (filter :isArchived repos)),
     :empty (count (filter :isEmpty repos)),
     :nonempty (count (remove :isEmpty repos)),
     :default-branch-commits (reduce + 0 commit-counts),
     :pages (:pages catalog)}))

(defn fetch-org-commit
  "Fetch one commit in one repository. The identity is `[repository oid]`.

  This intentionally keeps repository identity even when forks share an OID.
  Returns nil only when `:missing-ok?` is true and GitHub has no such commit."
  ([repository oid] (fetch-org-commit repository oid {}))
  ([repository oid
    {:keys [gh-runner missing-ok?],
     :or {gh-runner default-gh-runner, missing-ok? false}}]
   (when (or (nil? oid) (str/blank? (str oid)))
     (throw (ex-info
              "Commit OID must not be blank"
              {:type ::invalid-commit, :repository repository, :oid oid})))
   (let [[owner name] (parse-repository repository)
         response (graphql-org-commit gh-runner owner name oid)
         repo (get-in response [:data :repository])
         commit (get repo :object)]
     (cond
       (nil? repo)
         (if missing-ok?
           nil
           (throw (ex-info "GitHub repository was not found or is inaccessible"
                           {:type ::repository-not-found,
                            :repository repository})))
       (not= "Commit" (:__typename commit))
         (if missing-ok?
           nil
           (throw (ex-info "Commit was not found in repository"
                           {:type ::commit-not-found,
                            :repository repository,
                            :oid oid})))
       :else
         (let [full (:nameWithOwner repo)
               commit (validate-complete-parents commit {:repository full})
               parents (:parents commit)
               parent-nodes (or (:nodes parents) [])]
           (when-not (= repository full)
             (throw
               (ex-info
                 "Repository identity changed; refresh the organization catalog"
                 {:type ::repository-changed,
                  :requested repository,
                  :current full,
                  :oid oid})))
           (-> commit
               (dissoc :__typename)
               (assoc :repository full
                      :key [full (:oid commit)]
                      :parent-keys (mapv (fn [{:keys [oid]}] [full oid])
                                     parent-nodes)
                      :rate-limit (get-in response [:data :rateLimit]))))))))

(defn fetch-history
  "Fetch every page of the repository's default-branch history via GraphQL.

  Options:
  - `:repository` OWNER/NAME (default plurigrid/ontology)
  - `:gh-runner` fn from gh argument vector to {:exit :out :err}
  - `:max-pages` safety bound (default 10000)

  Returns normalized, oldest-first `:commits`, refs/tags/PRs, and rate-limit
  observations. `:commits` is the transitive parent closure of every current
  branch, tag, and open/closed/merged pull-request commit; duplicate OIDs are
  removed. This is the complete history GitHub still exposes for the repo."
  ([] (fetch-history {}))
  ([{:keys [repository gh-runner max-pages],
     :or {repository repository, gh-runner default-gh-runner, max-pages 10000}}]
   (let [[owner name] (parse-repository repository)
         runner gh-runner]
     (loop [cursor nil
            pages 0
            by-oid {}
            order []
            repo-meta nil
            reported-history-total nil
            refs nil
            tags nil
            pull-requests nil
            rates []]
       (when (>= pages max-pages)
         (throw (ex-info "GraphQL pagination exceeded max-pages"
                         {:type ::pagination-limit,
                          :max-pages max-pages,
                          :cursor cursor})))
       (let [{:keys [response]} (graphql-page runner owner name cursor)
             repo (get-in response [:data :repository])
             page (history-page response)
             page-info (:pageInfo page)
             nodes (:nodes page)
             _ (when (nil? repo)
                 (throw (ex-info "GitHub repository was not found or inaccessible"
                                 {:type ::repository-not-found,
                                  :repository repository})))
             _ (when-not (and (map? page)
                              (map? page-info)
                              (integer? (:totalCount page))
                              (boolean? (:hasNextPage page-info))
                              (sequential? nodes))
                 (throw
                   (ex-info
                     "GitHub history response was missing commit pagination data"
                     {:type ::invalid-response,
                      :repository repository})))
             page-refs (complete-connection-nodes
                         (:refs repo)
                         "branch"
                         {:repository repository})
             page-tags (complete-connection-nodes
                         (:tags repo)
                         "tag"
                         {:repository repository})
             page-pull-requests (complete-connection-nodes
                                  (:pullRequests repo)
                                  "pull request"
                                  {:repository repository})
             _ (doseq [pull-request page-pull-requests]
                 (complete-connection-nodes
                   (:commits pull-request)
                   "pull request commit"
                   {:repository repository,
                    :pull-request (:number pull-request)}))
             page-repo-meta (select-keys repo
                                         [:nameWithOwner :url
                                          :defaultBranchRef])
             _ (when (and repo-meta
                          (not (same-canonical-data? repo-meta page-repo-meta)))
                 (throw
                   (ex-info
                     "Repository metadata changed during history pagination"
                     {:type ::repository-changed,
                      :repository repository,
                      :previous repo-meta,
                      :current page-repo-meta})))
             _ (doseq [[label previous current]
                       [["branch" refs page-refs]
                        ["tag" tags page-tags]
                        ["pull request" pull-requests page-pull-requests]]]
                 (when (and (some? previous)
                            (not (same-canonical-data? previous current)))
                   (throw
                     (ex-info
                       (str "Repository " label
                            " list changed during history pagination")
                       {:type ::repository-roots-changed,
                        :repository repository,
                        :connection label,
                        :previous previous,
                        :current current}))))
             page-history-total (:totalCount page)
             _ (when (and reported-history-total
                          (not= reported-history-total page-history-total))
                 (throw
                   (ex-info
                     "Default-branch history count changed during pagination"
                     {:type ::history-count-mismatch,
                      :repository repository,
                      :previous reported-history-total,
                      :reported page-history-total,
                      :fetched (count by-oid)})))
             [by-oid order] (reduce (fn [[m o] commit]
                                      (let [oid (:oid commit)
                                            commit
                                              (validate-complete-parents
                                                commit
                                                {:repository repository})]
                                        (cond
                                          (str/blank? oid) [m o]
                                          (not (contains? m oid))
                                            [(assoc m oid commit) (conj o oid)]
                                          (same-canonical-data? (get m oid) commit)
                                            [m o]
                                          :else
                                            (throw
                                              (ex-info
                                                "Commit changed during history pagination"
                                                {:type ::commit-changed,
                                                 :repository repository,
                                                 :oid oid,
                                                 :previous (get m oid),
                                                 :current commit})))))
                              [by-oid order]
                              nodes)
             has-next? (true? (:hasNextPage page-info))
             next-cursor (:endCursor page-info)]
         (when (and has-next?
                    (or (str/blank? next-cursor) (= cursor next-cursor)))
           (throw (ex-info "GraphQL pagination did not advance"
                           {:type ::stalled-pagination,
                            :cursor cursor,
                            :next-cursor next-cursor})))
         (if has-next?
           (recur next-cursor
                  (inc pages)
                  by-oid
                  order
                  (or repo-meta page-repo-meta)
                  (or reported-history-total page-history-total)
                  (or refs page-refs)
                  (or tags page-tags)
                  (or pull-requests page-pull-requests)
                  (conj rates (get-in response [:data :rateLimit])))
           (let [newest-first (mapv by-oid order)
                 repo (merge repo-meta repo)
                 default-commits (vec (reverse newest-first))
                 reported (or reported-history-total page-history-total)
                 _ (when-not (= reported (count default-commits))
                     (throw
                       (ex-info
                         "Default-branch history count changed during pagination"
                         {:type ::history-count-mismatch,
                          :repository repository,
                          :reported reported,
                          :fetched (count default-commits)})))
                 pull-requests (or pull-requests
                                   (get-in repo [:pullRequests :nodes]))
                 pr-oids (->> pull-requests
                              (mapcat #(get-in % [:commits :nodes]))
                              (map #(get-in % [:commit :oid]))
                              (remove str/blank?)
                              distinct
                              vec)
                 ref-oids (keep ref-target-oid
                                (concat (or refs (get-in repo [:refs :nodes]))
                                        (or tags (get-in repo [:tags :nodes]))))
                 closure (fetch-commit-closure runner
                                               owner
                                               name
                                               by-oid
                                               (concat ref-oids pr-oids))
                 commits (->> (vals (:by-oid closure))
                              (sort-by (juxt :committedDate :oid))
                              vec)
                 pr-by-oid (reduce (fn [m pr]
                                     (reduce (fn [m node]
                                               (update
                                                 m
                                                 (get-in node [:commit :oid])
                                                 (fnil conj [])
                                                 (select-keys
                                                   pr
                                                   [:number :title :state :url
                                                    :createdAt :closedAt
                                                    :mergedAt])))
                                       m
                                       (get-in pr [:commits :nodes])))
                             {}
                             pull-requests)]
             {:schema-version 1,
              :repository (:nameWithOwner repo),
              :url (:url repo),
              :default-branch (get-in repo [:defaultBranchRef :name]),
              :fetched-at (str (java.time.Instant/now)),
              :pages (inc pages),
              :default-commit-oids (mapv :oid default-commits),
              :closure-fetches (count (:fetched-oids closure)),
              :refs (->> (or refs (get-in repo [:refs :nodes]))
                         (keep
                           (fn [ref]
                             (when-let [oid (ref-target-oid ref)]
                               {:kind :branch, :name (:name ref), :oid oid})))
                         (sort-by :name)
                         vec),
              :tags (->> (or tags (get-in repo [:tags :nodes]))
                         (keep (fn [ref]
                                 (when-let [oid (ref-target-oid ref)]
                                   {:kind :tag, :name (:name ref), :oid oid})))
                         (sort-by :name)
                         vec),
              :pull-requests (mapv #(dissoc % :commits) pull-requests),
              :pr-by-oid pr-by-oid,
              :rate-limit (conj rates (get-in response [:data :rateLimit])),
              :commits commits})))))))

(defn- cache-envelope
  [history]
  (let [payload (pr-str (canonical-data history))]
    {:format :plurigrid.ontology/history-cache,
     :version 1,
     :sha256 (sha256 payload),
     :history history}))

(defn write-cache!
  "Atomically write HISTORY as EDN with a SHA-256 integrity envelope."
  [path history]
  (let [target (.toAbsolutePath (.normalize (.toPath (io/file path))))
        parent (.getParent target)
        _ (when parent
            (Files/createDirectories
              parent
              (make-array java.nio.file.attribute.FileAttribute 0)))
        tmp (Files/createTempFile
              parent
              ".ontology-history-"
              ".edn"
              (make-array java.nio.file.attribute.FileAttribute 0))]
    (try (spit (.toFile tmp) (str (pr-str (cache-envelope history)) "\n"))
         (try (Files/move tmp
                          target
                          (into-array StandardCopyOption
                                      [StandardCopyOption/ATOMIC_MOVE
                                       StandardCopyOption/REPLACE_EXISTING]))
              (catch java.nio.file.AtomicMoveNotSupportedException _
                (Files/move tmp
                            target
                            (into-array
                              StandardCopyOption
                              [StandardCopyOption/REPLACE_EXISTING]))))
         (str target)
         (finally (Files/deleteIfExists tmp)))))

(defn read-cache
  "Read and verify a cache produced by `write-cache!`."
  [path]
  (let [file (io/file path)]
    (when-not (.isFile file)
      (throw (ex-info "History cache does not exist"
                      {:type ::cache-missing, :path (str path)})))
    (let [{:keys [format version history], :as envelope}
            (try (edn/read-string (slurp file))
                 (catch Exception cause
                   (throw (ex-info "History cache is not valid EDN"
                                   {:type ::invalid-cache, :path (str path)}
                                   cause))))
          expected (:sha256 envelope)
          actual (sha256 (pr-str (canonical-data history)))
          shaped? (or (vector? (:commits history))
                      (and (= :github-organization (:kind history))
                           (vector? (:repositories history))))]
      (when-not (and (= format :plurigrid.ontology/history-cache)
                     (= version 1)
                     (= expected actual)
                     shaped?)
        (throw (ex-info "History cache failed validation"
                        {:type ::invalid-cache,
                         :path (str path),
                         :expected expected,
                         :actual actual,
                         :format format,
                         :version version})))
      history)))

(defn refresh!
  "Fetch complete repository history and atomically refresh `:cache-file`."
  ([] (refresh! {}))
  ([{:keys [cache-file], :or {cache-file default-cache-file}, :as opts}]
   (let [history (fetch-history opts)]
     (write-cache! cache-file history)
     history)))

(defn refresh-organization!
  "Fetch the complete organization catalog and atomically cache it."
  ([] (refresh-organization! {}))
  ([{:keys [cache-file], :or {cache-file default-org-cache-file}, :as opts}]
   (let [catalog (fetch-organization opts)]
     (write-cache! cache-file catalog)
     catalog)))

(defn- commit-index
  [history]
  (into {}
        (map-indexed (fn [index commit] [(:oid commit) index])
                     (:commits history))))

(defn- refs-by-oid
  [history]
  (reduce (fn [m {:keys [kind name oid]}]
            (update m oid (fnil conj []) {:kind kind, :name name}))
    {}
    (concat (:refs history) (:tags history))))

(defn prepare-history
  "Build the derived graph used by walkers. Missing parents are retained as
  frontier edges but are not selectable until fetched into `:commits`."
  [history]
  (let [commits (:commits history)
        default-oids (set (or (:default-commit-oids history)
                              (map :oid commits)))
        by-oid (into {} (map (juxt :oid identity)) commits)
        index (commit-index history)
        children (reduce (fn [m {:keys [oid parents]}]
                           (reduce
                             (fn [m parent]
                               (update m (:oid parent) (fnil conj []) oid))
                             m
                             (:nodes parents)))
                   {}
                   commits)
        labels (refs-by-oid history)
        commits
          (mapv (fn [commit]
                  (let [oid (:oid commit)
                        known-parents (->> (get-in commit [:parents :nodes])
                                           (map :oid)
                                           (filter by-oid)
                                           distinct
                                           (sort-by index)
                                           vec)
                        known-children (->> (get children oid)
                                            distinct
                                            (sort-by index)
                                            vec)]
                    (assoc commit
                      :index (index oid)
                      :parents-known known-parents
                      :children-known known-children
                      :labels (vec (sort-by (juxt :kind :name) (labels oid)))
                      :on-default-branch? (contains? default-oids oid)
                      :pull-requests (vec (get-in history [:pr-by-oid oid])))))
            commits)]
    (assoc history
      :commits commits
      :by-oid (into {} (map (juxt :oid identity)) commits)
      :index (into {} (map (juxt :oid :index)) commits)
      :prepared? true)))

(defn specter-select
  "Run a Specter path against prepared history. Useful directly in CIDER.

  Examples:
  `(specter-select [sp/ALL :messageHeadline] (:commits h))`
  `(specter-select [sp/ALL #(seq (:labels %))] (:commits h))`"
  [path value]
  (sp/select path value))

(defn commit-headlines
  [history]
  (sp/select [:commits sp/ALL :messageHeadline] history))

(defn merge-commits
  [history]
  (sp/select [:commits sp/ALL #(> (count (:parents-known %)) 1)] history))

(defn commits-touching
  "Select commits whose headline, author, committer, or ref/tag labels contain
  NEEDLE (case-insensitive)."
  [history needle]
  (let [needle (str/lower-case (str needle))]
    (sp/select [:commits sp/ALL
                #(str/includes? (str/lower-case
                                  (str (:messageHeadline %)
                                       " " (get-in % [:author :name])
                                       " " (get-in % [:author :user :login])
                                       " " (get-in % [:committer :name])
                                       " " (str/join " "
                                                     (map :name (:labels %)))))
                                needle)]
               history)))

(def ^:private golden-gamma -7046029254386353131)
(def ^:private mix-one -4658895280553007687)
(def ^:private mix-two -7723592293110705685)

(defn splitmix64
  "Return `[next-state random-long]` for SplitMix64."
  [state]
  (let [state (unchecked-add (long state) golden-gamma)
        z1 (unchecked-multiply (bit-xor state
                                        (unsigned-bit-shift-right state 30))
                               mix-one)
        z2 (unchecked-multiply (bit-xor z1 (unsigned-bit-shift-right z1 27))
                               mix-two)]
    [state (bit-xor z2 (unsigned-bit-shift-right z2 31))]))

(defn- trit [random-long] (dec (Math/floorMod (long random-long) 3)))

(defn- color
  [random-long]
  (format "#%06X" (bit-and (long random-long) 0xFFFFFF)))

(defn- unsigned-index
  [random-long n]
  (when (pos? n) (int (Long/remainderUnsigned (long random-long) (long n)))))

(defn- conserved-trit
  [sum]
  (case (Math/floorMod (long (- sum)) 3)
    0 0
    1 1
    2 -1))

(defn- conserve-trits
  [path]
  (if (seq path)
    (let [index (dec (count path))
          prefix-sum (reduce + 0 (map :trit (take index path)))]
      (assoc (vec path)
        index (assoc (nth path index) :trit (conserved-trit prefix-sum))))
    path))

(defn organization-random-walk
  "Lazily random-walk commit history across a GitHub organization.

  The first seeded choice selects a non-empty repository (or `:repository`
  selects one explicitly); subsequent choices select commit parents via live
  `gh api graphql` calls. At a root, `:restart?` selects another non-empty
  repository and continues. Each commit key is `[repository oid]`, so equal
  OIDs in forks remain distinct. `:steps` is the maximum number of transitions;
  zero returns only the selected repository head. An injected `:gh-runner`
  makes every network and failure path testable.

  Supported options: `:seed`, `:steps`, `:repository`, `:start-oid`,
  `:restart?`, `:gh-runner`."
  ([catalog] (organization-random-walk catalog {}))
  ([catalog
    {:keys [seed steps repository start-oid restart? gh-runner],
     :or {seed default-seed,
          steps default-steps,
          restart? false,
          gh-runner default-gh-runner}}]
   (let [seed (parse-long-value "seed" seed)
         steps (parse-long-value "steps" steps)
         all-repos (mapv normalize-organization-repository
                     (:repositories catalog))
         repos (vec (remove #(or (:isEmpty %) (str/blank? (:head-oid %)))
                      all-repos))
         by-name (into {} (map (juxt :nameWithOwner identity)) all-repos)]
     (when (neg? steps)
       (throw (ex-info
                "Steps must be non-negative"
                {:type ::invalid-option, :option "steps", :value steps})))
     (when (and repository (nil? (get by-name repository)))
       (throw (ex-info "Walk repository is not present in organization catalog"
                       {:type ::invalid-start, :repository repository})))
     (when (and repository
                (or (:isEmpty (get by-name repository))
                    (str/blank? (:head-oid (get by-name repository)))))
       (throw (ex-info
                "Cannot start an organization walk in an empty repository"
                {:type ::empty-repository, :repository repository})))
     (when (empty? repos)
       (throw (ex-info
                "Cannot walk an organization with no non-empty repositories"
                {:type ::empty-organization,
                 :organization (:organization catalog)})))
     (let [[state choice] (splitmix64 seed)
           start-repo (or (get by-name repository)
                          (nth repos (unsigned-index choice (count repos))))
           start-oid (or start-oid (:head-oid start-repo))]
       (when (str/blank? (str start-oid))
         (throw (ex-info "Organization walk start OID must not be blank"
                         {:type ::invalid-start,
                          :repository (:nameWithOwner start-repo),
                          :oid start-oid})))
       (conserve-trits
         (loop [repo start-repo
                oid start-oid
                state state
                transition 0
                visited #{}
                path []]
           (let [full (:nameWithOwner repo)
                 key [full oid]
                 commit (fetch-org-commit full oid {:gh-runner gh-runner})
                 [next-state random-long] (splitmix64 state)
                 parent-keys (:parent-keys commit)
                 fresh (vec (remove visited parent-keys))
                 pool (if (seq fresh) fresh parent-keys)
                 walk-step (-> commit
                               (dissoc :rate-limit)
                               (assoc :step transition
                                      :degree (count parent-keys)
                                      :trit (trit random-long)
                                      :color (color random-long)))
                 path (conj path walk-step)]
             (cond (>= transition steps) path
                   (seq pool) (let [[next-repo next-oid] (nth pool
                                                              (unsigned-index
                                                                random-long
                                                                (count pool)))]
                                (recur (get by-name next-repo repo)
                                       next-oid
                                       next-state
                                       (inc transition)
                                       (conj visited key)
                                       path))
                   restart?
                     (let [visited (conj visited key)
                           visited-repositories
                             (conj (into #{} (map first) visited) full)
                           unvisited-repos
                             (vec (remove #(contains? (set visited-repositories)
                                                      (:nameWithOwner %))
                                    repos))]
                       (if (seq unvisited-repos)
                         (let [next-repo (nth unvisited-repos
                                              (unsigned-index
                                                random-long
                                                (count unvisited-repos)))
                               path-index (dec (count path))]
                           (recur next-repo
                                  (:head-oid next-repo)
                                  next-state
                                  (inc transition)
                                  visited
                                  (assoc (vec path)
                                    path-index (assoc (nth path path-index)
                                                 :teleport true))))
                         path))
                   :else path))))))))

(defn format-organization-walk
  [walk]
  (with-out-str (doseq [{:keys [step repository abbreviatedOid committedDate
                                messageHeadline degree trit color teleport]}
                          walk]
                  (printf "%02d  %s  %-38s  %s  %s  trit=%s  degree=%d%s%n"
                          step
                          color
                          repository
                          abbreviatedOid
                          (subs (or committedDate "")
                                0
                                (min 10 (count (or committedDate ""))))
                          (format "%+d" trit)
                          degree
                          (if teleport "  teleport" ""))
                  (println "    " messageHeadline))))

(defn- neighbors
  [prepared oid direction]
  (let [{:keys [parents-known children-known]} (get-in prepared [:by-oid oid])]
    (case direction
      :past parents-known
      :future children-known
      :both (->> (concat parents-known children-known)
                 distinct
                 (sort-by (:index prepared))
                 vec)
      (throw (ex-info "Direction must be :past, :future, or :both"
                      {:type ::invalid-direction, :direction direction})))))

(defn- endpoint-oid
  [prepared endpoint]
  (let [commits (:commits prepared)
        refs (concat (:refs prepared) (:tags prepared))]
    (cond (nil? endpoint) (endpoint-oid prepared :head)
          (= endpoint :oldest) (:oid (first commits))
          (= endpoint :latest) (:oid (last commits))
          (or (= endpoint :head) (= endpoint :newest))
            (or (some (fn [{:keys [kind name oid]}]
                        (when (and (= kind :branch)
                                   (= name (:default-branch prepared)))
                          oid))
                      (:refs prepared))
                (:oid (last (filter :on-default-branch? commits)))
                (:oid (last commits)))
          (integer? endpoint) (:oid (nth commits endpoint nil))
          :else (or (some (fn [{:keys [name oid]}]
                            (when (= name (str endpoint)) oid))
                          refs)
                    (some (fn [{:keys [oid abbreviatedOid]}]
                            (when (or (= oid (str endpoint))
                                      (= abbreviatedOid (str endpoint)))
                              oid))
                          commits)
                    (when (= 1
                             (count (filter #(str/starts-with? (:oid %)
                                                               (str endpoint))
                                      commits)))
                      (:oid (first (filter #(str/starts-with? (:oid %)
                                                              (str endpoint))
                                     commits))))))))

(defn random-walk
  "Walk the commit DAG deterministically.

  Options:
  - `:seed` signed/unsigned 64-bit seed (default 21211)
  - `:steps` maximum transitions (default 24; 0 returns the start node)
  - `:direction` :past, :future, or :both (default :past)
  - `:start` :head/:newest, :latest, :oldest, index, branch/tag name, or unique OID prefix
  - `:restart?` teleport to an unvisited commit at a dead end (default false)

  A walk never silently loops on the same directed edge while an unvisited
  neighbor exists. Each step carries deterministic GF(3) trit/color metadata."
  ([history] (random-walk history {}))
  ([history
    {:keys [seed steps direction start restart?],
     :or {seed default-seed,
          steps default-steps,
          direction :past,
          start :head,
          restart? false}}]
   (let [prepared (if (:prepared? history) history (prepare-history history))
         seed (parse-long-value "seed" seed)
         steps (parse-long-value "steps" steps)
         start-oid (endpoint-oid prepared start)]
     (when (neg? steps)
       (throw (ex-info
                "Steps must be non-negative"
                {:type ::invalid-option, :option "steps", :value steps})))
     (when (empty? (:commits prepared))
       (throw (ex-info "Cannot walk empty history" {:type ::empty-history})))
     (when-not start-oid
       (throw (ex-info "Walk start could not be resolved"
                       {:type ::invalid-start, :start start})))
     (conserve-trits
       (loop [oid start-oid
              state seed
              transition 0
              visited #{start-oid}
              path []]
         (let [[next-state random-long] (splitmix64 state)
               commit (get-in prepared [:by-oid oid])
               candidates (neighbors prepared oid direction)
               fresh (vec (remove visited candidates))
               pool (if (seq fresh) fresh candidates)
               walk-step (-> commit
                             (select-keys [:oid :abbreviatedOid :committedDate
                                           :messageHeadline :additions
                                           :deletions :changedFilesIfAvailable
                                           :index :labels])
                             (assoc :step transition
                                    :direction direction
                                    :degree (count candidates)
                                    :trit (trit random-long)
                                    :color (color random-long)))
               path (conj path walk-step)]
           (cond (>= transition steps) path
                 (seq pool) (let [next-oid (nth pool
                                                (unsigned-index random-long
                                                                (count pool)))]
                              (recur next-oid
                                     next-state
                                     (inc transition)
                                     (conj visited next-oid)
                                     path))
                 restart? (let [unvisited (vec (remove #(contains? visited
                                                                   (:oid %))
                                                 (:commits prepared)))]
                            (if (seq unvisited)
                              (let [next-oid (:oid (nth unvisited
                                                        (unsigned-index
                                                          random-long
                                                          (count unvisited))))]
                                (recur next-oid
                                       next-state
                                       (inc transition)
                                       (conj visited next-oid)
                                       (let [path (vec path)
                                             index (dec (count path))]
                                         (assoc path
                                           index (assoc (nth path index)
                                                   :teleport true)))))
                              path))
                 :else path)))))))

(defn summary
  [history]
  (let [prepared (if (:prepared? history) history (prepare-history history))
        commits (:commits prepared)
        dates (keep :committedDate commits)]
    {:repository (:repository prepared),
     :default-branch (:default-branch prepared),
     :commits (count commits),
     :default-branch-commits (count (:default-commit-oids prepared)),
     :pull-requests (count (:pull-requests prepared)),
     :closure-fetches (:closure-fetches prepared),
     :merges (count (merge-commits prepared)),
     :branches (count (:refs prepared)),
     :tags (count (:tags prepared)),
     :oldest (first dates),
     :newest (last dates),
     :pages (:pages prepared)}))

(defn format-walk
  [walk]
  (with-out-str
    (doseq [{:keys [step abbreviatedOid committedDate messageHeadline degree
                    trit color labels teleport]}
              walk]
      (printf
        "%02d  %s  %s  %s  trit=%s  degree=%d%s%s%n"
        step
        color
        abbreviatedOid
        (subs (or committedDate "") 0 (min 10 (count (or committedDate ""))))
        (format "%+d" trit)
        degree
        (if teleport "  teleport" "")
        (if (seq labels)
          (str "  "
               (str/join ","
                         (map (fn [{:keys [kind name]}]
                                (str (clojure.core/name kind) ":" name))
                           labels)))
          ""))
      (println "    " messageHeadline))))

(defn- usage
  []
  (str
    "Usage: clojure -M:run COMMAND [options]\n"
    "Repository commands: refresh | walk | summary | select\n"
      "Organization commands: org-refresh | org-walk | org-summary | org-select\n"
    "  --org LOGIN             organization (default plurigrid)\n"
      "  --repo OWNER/NAME       repository / org-walk start repository\n"
    "  --cache PATH            cache EDN (command-specific default)\n"
      "  --seed N|0xHEX          deterministic SplitMix64 seed\n"
    "  --steps N               maximum transitions\n"
      "  --direction past|future|both (repository walk)\n"
    "  --start head|latest|oldest|REF|OID\n"
      "  --start-oid OID         organization walk start commit\n"
    "  --restart               teleport at dead ends\n"
      "  --refresh               refresh cache before command\n"))

(defn- option-value
  [option value]
  (when (or (nil? value) (str/starts-with? value "--"))
    (throw (ex-info (str option " requires a value")
                    {:type ::missing-option-value, :option option})))
  value)

(defn- parse-args
  [args]
  (loop [args (seq args)
         opts {:command :walk}]
    (if-not args
      opts
      (let [[arg value & more] args]
        (case arg
          "refresh" (recur (next args) (assoc opts :command :refresh))
          "walk" (recur (next args) (assoc opts :command :walk))
          "summary" (recur (next args) (assoc opts :command :summary))
          "select" (recur (next args) (assoc opts :command :select))
          "org-refresh" (recur (next args) (assoc opts :command :org-refresh))
          "org-walk" (recur (next args) (assoc opts :command :org-walk))
          "org-summary" (recur (next args) (assoc opts :command :org-summary))
          "org-select" (recur (next args) (assoc opts :command :org-select))
          "--org" (recur more
                         (assoc opts :organization (option-value arg value)))
          "--repo" (recur more
                          (assoc opts :repository (option-value arg value)))
          "--cache" (recur more
                           (assoc opts :cache-file (option-value arg value)))
          "--seed" (recur more (assoc opts :seed (option-value arg value)))
          "--steps" (recur more (assoc opts :steps (option-value arg value)))
          "--direction" (recur more
                               (assoc opts
                                 :direction (keyword (option-value arg value))))
          "--start" (let [value (option-value arg value)]
                      (recur more
                             (assoc opts
                               :start (case value
                                        "head" :head
                                        "newest" :head
                                        "latest" :latest
                                        "oldest" :oldest
                                        value))))
          "--start-oid" (recur more
                               (assoc opts :start-oid (option-value arg value)))
          "--restart" (recur (next args) (assoc opts :restart? true))
          "--refresh" (recur (next args) (assoc opts :refresh? true))
          (throw (ex-info (str "Unknown argument: " arg)
                          {:type ::unknown-argument, :argument arg})))))))

(defn load-history
  "Read repository cache, refreshing when requested or missing."
  [{:keys [cache-file refresh?], :or {cache-file default-cache-file}, :as opts}]
  (let [history (if (or refresh? (not (.isFile (io/file cache-file))))
                  (refresh! opts)
                  (read-cache cache-file))]
    (when (and (contains? opts :repository)
               (not= (:repository opts) (:repository history)))
      (throw (ex-info
               "Repository cache identity does not match --repo; use --refresh"
               {:type ::cache-identity-mismatch,
                :requested (:repository opts),
                :cached (:repository history),
                :cache-file cache-file})))
    history))

(defn load-organization
  "Read organization cache, refreshing when requested or missing."
  [{:keys [cache-file refresh?],
    :or {cache-file default-org-cache-file},
    :as opts}]
  (let [catalog (if (or refresh? (not (.isFile (io/file cache-file))))
                  (refresh-organization! opts)
                  (read-cache cache-file))]
    (when (and (contains? opts :organization)
               (not= (:organization opts) (:organization catalog)))
      (throw (ex-info
               "Organization cache identity does not match --org; use --refresh"
               {:type ::cache-identity-mismatch,
                :requested (:organization opts),
                :cached (:organization catalog),
                :cache-file cache-file})))
    catalog))

(defn -main
  [& args]
  (try
    (let [{:keys [command cache-file], :as parsed} (parse-args args)
          org-command? (contains? #{:org-refresh :org-walk :org-summary
                                    :org-select}
                                  command)
          opts (if (and org-command? (nil? cache-file))
                 (assoc parsed :cache-file default-org-cache-file)
                 parsed)
          data (cond (= command :refresh) (refresh! opts)
                     (= command :org-refresh) (refresh-organization! opts)
                     org-command? (load-organization opts)
                     :else (load-history opts))]
      (case command
        :refresh (pprint/pprint (summary data))
        :summary (pprint/pprint (summary data))
        :select (doseq [headline (commit-headlines data)] (println headline))
        :walk (print (format-walk (random-walk data opts)))
        :org-refresh (pprint/pprint (organization-summary data))
        :org-summary (pprint/pprint (organization-summary data))
        :org-select
          (doseq [name (sp/select [:repositories sp/ALL :nameWithOwner] data)]
            (println name))
        :org-walk (print (format-organization-walk
                           (organization-random-walk data opts))))
      (shutdown-agents))
    (catch clojure.lang.ExceptionInfo error
      (binding [*out* *err*]
        (println (.getMessage error))
        (pprint/pprint (ex-data error))
        (println (usage)))
      (shutdown-agents)
      (System/exit 2))))
