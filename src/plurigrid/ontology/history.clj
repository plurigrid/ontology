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
(def default-seed 21211)
(def default-steps 24)
(def default-cache-file ".cache/ontology-history.edn")
(def max-page-size 100)

(def ^:private history-query
  "query($owner:String!,$name:String!,$cursor:String){
     repository(owner:$owner,name:$name){
       nameWithOwner url defaultBranchRef{name}
       refs(refPrefix:\"refs/heads/\",first:100){nodes{name target{... on Commit{oid}}}}
       tags:refs(refPrefix:\"refs/tags/\",first:100){nodes{name target{__typename ... on Commit{oid} ... on Tag{target{__typename ... on Commit{oid}}}}}}
       pullRequests(first:100){nodes{number title state url createdAt closedAt mergedAt
                                    commits(first:100){totalCount nodes{commit{oid}}}}}
       object(expression:\"HEAD\"){... on Commit{
         history(first:100,after:$cursor){
           pageInfo{hasNextPage endCursor}
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

(defn- sha256 [s]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes (str s) StandardCharsets/UTF_8))]
    (format "%064x" (BigInteger. 1 digest))))

(defn- parse-repository [repo]
  (let [[owner name & extra] (str/split (str repo) #"/")]
    (when (or (str/blank? owner) (str/blank? name) (seq extra))
      (throw (ex-info "Repository must be OWNER/NAME"
                      {:type ::invalid-repository :repository repo})))
    [owner name]))

(defn- parse-long-value [label value]
  (try
    (cond
      (integer? value) (long value)
      (re-matches #"(?i)0x[0-9a-f]+" (str value))
      (.longValue (BigInteger. (subs (str value) 2) 16))
      :else (Long/parseLong (str value)))
    (catch Exception cause
      (throw (ex-info (str "Invalid " label ": " value)
                      {:type ::invalid-option :option label :value value}
                      cause)))))

(defn- canonical-data [value]
  (cond
    (map? value) (into (sorted-map) (map (fn [[k v]] [k (canonical-data v)])) value)
    (set? value) (mapv canonical-data (sort-by pr-str value))
    (sequential? value) (mapv canonical-data value)
    :else value))

(defn- default-gh-runner [args]
  (let [process (-> (ProcessBuilder. ^java.util.List (mapv str (cons "gh" args)))
                    (.redirectErrorStream false)
                    (.start))
        stdout (future (slurp (.getInputStream process)))
        stderr (future (slurp (.getErrorStream process)))
        exit (.waitFor process)]
    {:exit exit :out @stdout :err @stderr}))

(defn- graphql-call [gh-runner query variables]
  (let [variable-args (mapcat (fn [[key value]]
                                (when (some? value)
                                  ["-F" (str (clojure.core/name key) "=" value)]))
                              variables)
        args (into ["api" "graphql" "-f" (str "query=" query)] variable-args)
        {:keys [exit out err] :as result} (gh-runner args)]
    (when-not (zero? (long (or exit -1)))
      (throw (ex-info "gh api graphql failed"
                      {:type ::gh-failed
                       :exit exit
                       :stderr (str/trim (str err))
                       :args (vec (take 8 args))})))
    (let [response (try
                     (json/read-str out :key-fn keyword)
                     (catch Exception cause
                       (throw (ex-info "gh returned unreadable JSON"
                                       {:type ::invalid-response
                                        :stdout (subs (str out) 0 (min 500 (count (str out))))}
                                       cause))))]
      (when-let [errors (:errors response)]
        (throw (ex-info "GitHub GraphQL returned errors"
                        {:type ::graphql-errors :errors errors})))
      {:result result :response response})))

(defn- graphql-page [gh-runner owner name cursor]
  (graphql-call gh-runner history-query
                {:owner owner :name name :cursor cursor}))

(defn- graphql-commit [gh-runner owner name oid]
  (get-in (:response (graphql-call gh-runner commit-query
                                   {:owner owner :name name :oid oid}))
          [:data :repository :object]))

(defn- history-page [response]
  (get-in response [:data :repository :object :history]))

(defn- ref-target-oid [ref]
  (or (get-in ref [:target :oid])
      (get-in ref [:target :target :oid])))

(defn- fetch-commit-closure
  "Complete every parent chain rooted at ROOT-OIDS, stopping at KNOWN commits."
  [runner owner name known root-oids]
  (loop [queue (into clojure.lang.PersistentQueue/EMPTY
                     (sort (distinct (remove str/blank? root-oids))))
         by-oid known
         fetched []]
    (if (empty? queue)
      {:by-oid by-oid :fetched-oids fetched}
      (let [oid (peek queue)
            queue (pop queue)]
        (if (contains? by-oid oid)
          (recur queue by-oid fetched)
          (let [commit (graphql-commit runner owner name oid)]
            (when-not (= "Commit" (:__typename commit))
              (throw (ex-info "Referenced commit could not be fetched"
                              {:type ::commit-not-found :oid oid})))
            (let [commit (dissoc commit :__typename)
                  parents (->> (get-in commit [:parents :nodes])
                               (map :oid)
                               (remove #(or (str/blank? %) (contains? by-oid %)))
                               sort)]
              (recur (into queue parents)
                     (assoc by-oid oid commit)
                     (conj fetched oid)))))))))

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
  ([{:keys [repository gh-runner max-pages]
     :or {repository repository
          gh-runner default-gh-runner
          max-pages 10000}}]
   (let [[owner name] (parse-repository repository)
         runner gh-runner]
     (loop [cursor nil pages 0 by-oid {} order [] repo-meta nil refs nil tags nil pull-requests nil rates []]
       (when (>= pages max-pages)
         (throw (ex-info "GraphQL pagination exceeded max-pages"
                         {:type ::pagination-limit :max-pages max-pages :cursor cursor})))
       (let [{:keys [response]} (graphql-page runner owner name cursor)
             repo (get-in response [:data :repository])
             page (history-page response)
             page-info (:pageInfo page)
             nodes (or (:nodes page) [])
             [by-oid order]
             (reduce (fn [[m o] commit]
                       (let [oid (:oid commit)]
                         (if (or (str/blank? oid) (contains? m oid))
                           [m o]
                           [(assoc m oid commit) (conj o oid)])))
                     [by-oid order]
                     nodes)
             has-next? (true? (:hasNextPage page-info))
             next-cursor (:endCursor page-info)]
         (when (nil? repo)
           (throw (ex-info "GitHub repository was not found or is inaccessible"
                           {:type ::repository-not-found :repository repository})))
         (when (and has-next? (or (str/blank? next-cursor) (= cursor next-cursor)))
           (throw (ex-info "GraphQL pagination did not advance"
                           {:type ::stalled-pagination :cursor cursor :next-cursor next-cursor})))
         (if has-next?
           (recur next-cursor (inc pages) by-oid order
                  (or repo-meta (select-keys repo [:nameWithOwner :url :defaultBranchRef]))
                  (or refs (get-in repo [:refs :nodes]))
                  (or tags (get-in repo [:tags :nodes]))
                  (or pull-requests (get-in repo [:pullRequests :nodes]))
                  (conj rates (get-in response [:data :rateLimit])))
           (let [newest-first (mapv by-oid order)
                 repo (merge repo-meta repo)
                 default-commits (vec (reverse newest-first))
                 pull-requests (or pull-requests (get-in repo [:pullRequests :nodes]))
                 pr-oids (->> pull-requests
                              (mapcat #(get-in % [:commits :nodes]))
                              (map #(get-in % [:commit :oid]))
                              (remove str/blank?)
                              distinct
                              vec)
                 ref-oids (keep ref-target-oid
                                (concat (or refs (get-in repo [:refs :nodes]))
                                        (or tags (get-in repo [:tags :nodes]))))
                 closure (fetch-commit-closure runner owner name by-oid
                                               (concat ref-oids pr-oids))
                 commits (->> (vals (:by-oid closure))
                              (sort-by (juxt :committedDate :oid))
                              vec)
                 pr-by-oid (reduce
                            (fn [m pr]
                              (reduce (fn [m node]
                                        (update m (get-in node [:commit :oid]) (fnil conj [])
                                                (select-keys pr [:number :title :state :url
                                                                 :createdAt :closedAt :mergedAt])))
                                      m (get-in pr [:commits :nodes])))
                            {} pull-requests)]
             {:schema-version 1
              :repository (:nameWithOwner repo)
              :url (:url repo)
              :default-branch (get-in repo [:defaultBranchRef :name])
              :fetched-at (str (java.time.Instant/now))
              :pages (inc pages)
              :default-commit-oids (mapv :oid default-commits)
              :closure-fetches (count (:fetched-oids closure))
              :refs (->> (or refs (get-in repo [:refs :nodes]))
                         (keep (fn [ref]
                                 (when-let [oid (ref-target-oid ref)]
                                   {:kind :branch :name (:name ref) :oid oid})))
                         (sort-by :name)
                         vec)
              :tags (->> (or tags (get-in repo [:tags :nodes]))
                         (keep (fn [ref]
                                 (when-let [oid (ref-target-oid ref)]
                                   {:kind :tag :name (:name ref) :oid oid})))
                         (sort-by :name)
                         vec)
              :pull-requests (mapv #(dissoc % :commits) pull-requests)
              :pr-by-oid pr-by-oid
              :rate-limit (conj rates (get-in response [:data :rateLimit]))
              :commits commits})))))))

(defn- cache-envelope [history]
  (let [payload (pr-str (canonical-data history))]
    {:format :plurigrid.ontology/history-cache
     :version 1
     :sha256 (sha256 payload)
     :history history}))

(defn write-cache!
  "Atomically write HISTORY as EDN with a SHA-256 integrity envelope."
  [path history]
  (let [target (.toAbsolutePath (.normalize (.toPath (io/file path))))
        parent (.getParent target)
        _ (when parent (Files/createDirectories parent (make-array java.nio.file.attribute.FileAttribute 0)))
        tmp (Files/createTempFile parent ".ontology-history-" ".edn"
                                  (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (spit (.toFile tmp) (str (pr-str (cache-envelope history)) "\n"))
      (try
        (Files/move tmp target
                    (into-array StandardCopyOption
                                [StandardCopyOption/ATOMIC_MOVE StandardCopyOption/REPLACE_EXISTING]))
        (catch java.nio.file.AtomicMoveNotSupportedException _
          (Files/move tmp target (into-array StandardCopyOption [StandardCopyOption/REPLACE_EXISTING]))))
      (str target)
      (finally
        (Files/deleteIfExists tmp)))))

(defn read-cache
  "Read and verify a cache produced by `write-cache!`."
  [path]
  (let [file (io/file path)]
    (when-not (.isFile file)
      (throw (ex-info "History cache does not exist"
                      {:type ::cache-missing :path (str path)})))
    (let [{:keys [format version history] :as envelope}
          (try
            (edn/read-string (slurp file))
            (catch Exception cause
              (throw (ex-info "History cache is not valid EDN"
                              {:type ::invalid-cache :path (str path)} cause))))
          expected (:sha256 envelope)
          actual (sha256 (pr-str (canonical-data history)))]
      (when-not (and (= format :plurigrid.ontology/history-cache)
                     (= version 1)
                     (= expected actual)
                     (vector? (:commits history)))
        (throw (ex-info "History cache failed validation"
                        {:type ::invalid-cache
                         :path (str path)
                         :expected expected
                         :actual actual
                         :format format
                         :version version})))
      history)))

(defn refresh!
  "Fetch complete history and atomically refresh `:cache-file`."
  ([] (refresh! {}))
  ([{:keys [cache-file] :or {cache-file default-cache-file} :as opts}]
   (let [history (fetch-history opts)]
     (write-cache! cache-file history)
     history)))

(defn- commit-index [history]
  (into {} (map-indexed (fn [index commit] [(:oid commit) index]) (:commits history))))

(defn- refs-by-oid [history]
  (reduce (fn [m {:keys [kind name oid]}]
            (update m oid (fnil conj []) {:kind kind :name name}))
          {}
          (concat (:refs history) (:tags history))))

(defn prepare-history
  "Build the derived graph used by walkers. Missing parents are retained as
  frontier edges but are not selectable until fetched into `:commits`."
  [history]
  (let [commits (:commits history)
        default-oids (set (or (:default-commit-oids history) (map :oid commits)))
        by-oid (into {} (map (juxt :oid identity)) commits)
        index (commit-index history)
        children (reduce
                  (fn [m {:keys [oid parents]}]
                    (reduce (fn [m parent]
                              (update m (:oid parent) (fnil conj []) oid))
                            m (:nodes parents)))
                  {} commits)
        labels (refs-by-oid history)
        commits (mapv
                 (fn [commit]
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

(defn commit-headlines [history]
  (sp/select [:commits sp/ALL :messageHeadline] history))

(defn merge-commits [history]
  (sp/select [:commits sp/ALL #(> (count (:parents-known %)) 1)] history))

(defn commits-touching
  "Select commits whose headline, author, committer, or ref/tag labels contain
  NEEDLE (case-insensitive)."
  [history needle]
  (let [needle (str/lower-case (str needle))]
    (sp/select
     [:commits sp/ALL
      #(str/includes?
        (str/lower-case
         (str (:messageHeadline %) " "
              (get-in % [:author :name]) " "
              (get-in % [:author :user :login]) " "
              (get-in % [:committer :name]) " "
              (str/join " " (map :name (:labels %)))))
        needle)]
     history)))

(def ^:private golden-gamma -7046029254386353131)
(def ^:private mix-one -4658895280553007687)
(def ^:private mix-two -7723592293110705685)

(defn splitmix64
  "Return `[next-state random-long]` for SplitMix64."
  [state]
  (let [state (unchecked-add (long state) golden-gamma)
        z1 (unchecked-multiply
            (bit-xor state (unsigned-bit-shift-right state 30)) mix-one)
        z2 (unchecked-multiply
            (bit-xor z1 (unsigned-bit-shift-right z1 27)) mix-two)]
    [state (bit-xor z2 (unsigned-bit-shift-right z2 31))]))

(defn- trit [random-long]
  (dec (Math/floorMod (long random-long) 3)))

(defn- color [random-long]
  (format "#%06X" (bit-and (long random-long) 0xFFFFFF)))

(defn- unsigned-index [random-long n]
  (when (pos? n)
    (int (Long/remainderUnsigned (long random-long) (long n)))))

(defn- neighbors [prepared oid direction]
  (let [{:keys [parents-known children-known]} (get-in prepared [:by-oid oid])]
    (case direction
      :past parents-known
      :future children-known
      :both (->> (concat parents-known children-known)
                 distinct
                 (sort-by (:index prepared))
                 vec)
      (throw (ex-info "Direction must be :past, :future, or :both"
                      {:type ::invalid-direction :direction direction})))))

(defn- endpoint-oid [prepared endpoint]
  (let [commits (:commits prepared)
        refs (concat (:refs prepared) (:tags prepared))]
    (cond
      (nil? endpoint) (endpoint-oid prepared :head)
      (= endpoint :oldest) (:oid (first commits))
      (= endpoint :latest) (:oid (last commits))
      (or (= endpoint :head) (= endpoint :newest))
      (or (some (fn [{:keys [kind name oid]}]
                  (when (and (= kind :branch) (= name (:default-branch prepared))) oid))
                (:refs prepared))
          (:oid (last (filter :on-default-branch? commits)))
          (:oid (last commits)))
      (integer? endpoint) (:oid (nth commits endpoint nil))
      :else (or (some (fn [{:keys [name oid]}] (when (= name (str endpoint)) oid)) refs)
                (some (fn [{:keys [oid abbreviatedOid]}]
                        (when (or (= oid (str endpoint)) (= abbreviatedOid (str endpoint))) oid))
                      commits)
                (when (= 1 (count (filter #(str/starts-with? (:oid %) (str endpoint)) commits)))
                  (:oid (first (filter #(str/starts-with? (:oid %) (str endpoint)) commits))))))))

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
  ([history {:keys [seed steps direction start restart?]
             :or {seed default-seed steps default-steps direction :past start :head restart? false}}]
   (let [prepared (if (:prepared? history) history (prepare-history history))
         seed (parse-long-value "seed" seed)
         steps (parse-long-value "steps" steps)
         start-oid (endpoint-oid prepared start)]
     (when (neg? steps)
       (throw (ex-info "Steps must be non-negative"
                       {:type ::invalid-option :option "steps" :value steps})))
     (when (empty? (:commits prepared))
       (throw (ex-info "Cannot walk empty history" {:type ::empty-history})))
     (when-not start-oid
       (throw (ex-info "Walk start could not be resolved"
                       {:type ::invalid-start :start start})))
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
                           (select-keys [:oid :abbreviatedOid :committedDate :messageHeadline
                                         :additions :deletions :changedFilesIfAvailable :index :labels])
                           (assoc :step transition
                                  :direction direction
                                  :degree (count candidates)
                                  :trit (trit random-long)
                                  :color (color random-long)))
             path (conj path walk-step)]
         (cond
           (>= transition steps) path
           (seq pool)
           (let [next-oid (nth pool (unsigned-index random-long (count pool)))]
             (recur next-oid next-state (inc transition) (conj visited next-oid) path))
           restart?
           (let [unvisited (vec (remove #(contains? visited (:oid %)) (:commits prepared)))]
             (if (seq unvisited)
               (let [next-oid (:oid (nth unvisited (unsigned-index random-long (count unvisited))))]
                 (recur next-oid next-state (inc transition) (conj visited next-oid)
                        (let [path (vec path)
                              index (dec (count path))]
                          (assoc path index (assoc (nth path index) :teleport true)))))
               path))
           :else path))))))

(defn summary [history]
  (let [prepared (if (:prepared? history) history (prepare-history history))
        commits (:commits prepared)
        dates (keep :committedDate commits)]
    {:repository (:repository prepared)
     :default-branch (:default-branch prepared)
     :commits (count commits)
     :default-branch-commits (count (:default-commit-oids prepared))
     :pull-requests (count (:pull-requests prepared))
     :closure-fetches (:closure-fetches prepared)
     :merges (count (merge-commits prepared))
     :branches (count (:refs prepared))
     :tags (count (:tags prepared))
     :oldest (first dates)
     :newest (last dates)
     :pages (:pages prepared)}))

(defn format-walk [walk]
  (with-out-str
    (doseq [{:keys [step abbreviatedOid committedDate messageHeadline degree trit color labels teleport]} walk]
      (printf "%02d  %s  %s  %s  trit=%s  degree=%d%s%s%n"
              step color abbreviatedOid (subs (or committedDate "") 0 (min 10 (count (or committedDate ""))))
              (format "%+d" trit) degree
              (if teleport "  teleport" "")
              (if (seq labels)
                (str "  " (str/join "," (map (fn [{:keys [kind name]}] (str (clojure.core/name kind) ":" name)) labels)))
                ""))
      (println "    " messageHeadline))))

(defn- usage []
  (str "Usage: clojure -M:run [refresh|walk|summary|select] [options]\n"
       "  --repo OWNER/NAME       repository (default plurigrid/ontology)\n"
       "  --cache PATH            cache EDN (default " default-cache-file ")\n"
       "  --seed N|0xHEX          deterministic SplitMix64 seed\n"
       "  --steps N               maximum transitions\n"
       "  --direction past|future|both\n"
       "  --start head|latest|oldest|REF|OID\n"
       "  --restart               teleport at dead ends\n"
       "  --refresh               refresh cache before command\n"))

(defn- parse-args [args]
  (loop [args (seq args) opts {:command :walk}]
    (if-not args
      opts
      (let [[arg value & more] args]
        (case arg
          "refresh" (recur (next args) (assoc opts :command :refresh))
          "walk" (recur (next args) (assoc opts :command :walk))
          "summary" (recur (next args) (assoc opts :command :summary))
          "select" (recur (next args) (assoc opts :command :select))
          "--repo" (recur more (assoc opts :repository value))
          "--cache" (recur more (assoc opts :cache-file value))
          "--seed" (recur more (assoc opts :seed value))
          "--steps" (recur more (assoc opts :steps value))
          "--direction" (recur more (assoc opts :direction (keyword value)))
          "--start" (recur more (assoc opts :start (case value
                                                      "head" :head
                                                      "newest" :head
                                                      "latest" :latest
                                                      "oldest" :oldest
                                                      value)))
          "--restart" (recur (next args) (assoc opts :restart? true))
          "--refresh" (recur (next args) (assoc opts :refresh? true))
          (throw (ex-info (str "Unknown argument: " arg) {:type ::unknown-argument :argument arg})))))))

(defn load-history
  "Read cache, refreshing when requested or when cache is missing."
  [{:keys [cache-file refresh?] :or {cache-file default-cache-file} :as opts}]
  (if (or refresh? (not (.isFile (io/file cache-file))))
    (refresh! opts)
    (read-cache cache-file)))

(defn -main [& args]
  (try
    (let [{:keys [command] :as opts} (parse-args args)
          history (if (= command :refresh) (refresh! opts) (load-history opts))]
      (case command
        :refresh (pprint/pprint (summary history))
        :summary (pprint/pprint (summary history))
        :select (doseq [headline (commit-headlines history)] (println headline))
        :walk (print (format-walk (random-walk history opts))))
      (shutdown-agents))
    (catch clojure.lang.ExceptionInfo error
      (binding [*out* *err*]
        (println (.getMessage error))
        (pprint/pprint (ex-data error))
        (println (usage)))
      (shutdown-agents)
      (System/exit 2))))
