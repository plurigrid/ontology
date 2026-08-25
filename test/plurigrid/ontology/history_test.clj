(ns plurigrid.ontology.history-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer [deftest is testing]]
            [com.rpl.specter :as sp]
            [plurigrid.ontology.history :as history]))

(defn commit
  ([oid parents] (commit oid parents (str "commit " oid)))
  ([oid parents headline]
   {:oid oid,
    :abbreviatedOid oid,
    :committedDate (str "2023-01-0" (inc (count parents)) "T00:00:00Z"),
    :messageHeadline headline,
    :additions 1,
    :deletions 0,
    :changedFilesIfAvailable 1,
    :author {:name "monaduck1069",
             :email "monaduck1069@users.noreply.github.com"},
    :committer {:name "monaduck1069",
                :email "monaduck1069@users.noreply.github.com"},
    :parents {:totalCount (count parents),
              :nodes (mapv #(hash-map :oid %) parents)},
    :tree {:oid (str "tree-" oid)}}))

(def fixture-history
  {:schema-version 1,
   :repository "plurigrid/ontology",
   :url "https://github.com/plurigrid/ontology",
   :default-branch "main",
   :fetched-at "2026-08-24T00:00:00Z",
   :pages 1,
   :default-commit-oids ["a" "b" "c" "d" "e"],
   :refs [{:kind :branch, :name "main", :oid "e"}
          {:kind :branch, :name "side", :oid "c"}],
   :tags [{:kind :tag, :name "v1", :oid "a"}],
   :rate-limit [],
   ;; Oldest first.  e is a merge of c and d; b is shared ancestry.
   :commits [(commit "a" []) (commit "b" ["a"]) (commit "c" ["b"] "side branch")
             (commit "d" ["b"]) (commit "e" ["c" "d"] "merge side")]})

(defn response
  [{:keys [nodes has-next? cursor refs tags pull-requests],
    :or {nodes [], has-next? false}}]
  (let [refs (or refs [])
        tags (or tags [])
        pull-requests (or pull-requests [])]
    {:data {:repository {:nameWithOwner "plurigrid/ontology",
                         :url "https://github.com/plurigrid/ontology",
                         :defaultBranchRef {:name "main"},
                         :refs {:totalCount (count refs), :nodes refs},
                         :tags {:totalCount (count tags), :nodes tags},
                         :pullRequests {:totalCount (count pull-requests),
                                        :nodes pull-requests},
                         :object {:history {:pageInfo {:hasNextPage has-next?,
                                                       :endCursor cursor},
                                            :nodes nodes}}},
            :rateLimit {:cost 1, :remaining 4999}}}))

(defn fake-runner
  [responses calls]
  (fn [args]
    (swap! calls conj args)
    (let [value (first @responses)]
      (swap! responses subvec 1)
      (if (map? value) {:exit 0, :out (json/write-str value), :err ""} value))))

(defn org-repo
  ([full oid] (org-repo full oid {}))
  ([full oid extra]
   (let [[_ name] (clojure.string/split full #"/")]
     (merge {:nameWithOwner full,
             :name name,
             :url (str "https://github.com/" full),
             :isArchived false,
             :isFork false,
             :isEmpty (nil? oid),
             :isPrivate false,
             :visibility "PUBLIC",
             :defaultBranchRef (when oid
                                 {:name "main",
                                  :target {:oid oid,
                                           :abbreviatedOid oid,
                                           :committedDate
                                             "2023-01-01T00:00:00Z",
                                           :messageHeadline (str "head " full),
                                           :history {:totalCount 3}}})}
            extra))))

(defn org-response
  [{:keys [nodes has-next? cursor total],
    :or {nodes [], has-next? false, total 0}}]
  {:data {:organization {:login "plurigrid",
                         :name "Plurigrid",
                         :url "https://github.com/plurigrid",
                         :repositories {:totalCount total,
                                        :pageInfo {:hasNextPage has-next?,
                                                   :endCursor cursor},
                                        :nodes nodes}},
          :rateLimit {:cost 1, :remaining 4999, :nodeCount (count nodes)}}})

(defn org-commit-response
  [full commit]
  {:data {:repository {:nameWithOwner full,
                       :url (str "https://github.com/" full),
                       :object (assoc commit :__typename "Commit")},
          :rateLimit {:cost 1, :remaining 4999, :nodeCount 2}}})

(deftest fetches-entire-organization-with-order-dedup-and-empty-repos
  (let [calls (atom [])
        pages (atom [(org-response {:nodes [(org-repo "plurigrid/zeta" "z")
                                            (org-repo "plurigrid/alpha" "a")],
                                    :has-next? true,
                                    :cursor "org-next",
                                    :total 3})
                     (org-response {:nodes [(org-repo "plurigrid/alpha" "a")
                                            (org-repo "plurigrid/empty" nil)],
                                    :total 3})])
        catalog (history/fetch-organization {:gh-runner (fake-runner pages
                                                                     calls)})]
    (is (= ["plurigrid/alpha" "plurigrid/empty" "plurigrid/zeta"]
           (mapv :nameWithOwner (:repositories catalog))))
    (is (= 3 (count (:by-name catalog))))
    (is (= ["plurigrid/alpha" "a"]
           (get-in catalog [:by-name "plurigrid/alpha" :head-key])))
    (is (nil? (get-in catalog [:by-name "plurigrid/empty" :head-key])))
    (is (= 2 (:pages catalog)))
    (is (= 3 (:reported-repositories catalog)))
    (is (some #{"pageSize=25"} (first @calls)))
    (is (some #{"cursor=org-next"} (second @calls)))
    (is (= {:organization "plurigrid",
            :repositories 3,
            :reported-repositories 3,
            :source-repositories 3,
            :forks 0,
            :archived 0,
            :empty 1,
            :nonempty 2,
            :default-branch-commits 6,
            :pages 2}
           (history/organization-summary catalog)))))

(deftest organization-fetch-error-boundaries
  (testing "blank organization"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"must not be blank"
                          (history/fetch-organization {:organization ""}))))
  (testing "invalid organization page size"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"page-size"
                          (history/fetch-organization {:page-size 101}))))
  (testing "missing organization"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"not found"
                          (history/fetch-organization
                            {:gh-runner (constantly
                                          {:exit 0,
                                           :out (json/write-str
                                                  {:data {:organization nil}}),
                                           :err ""})}))))
  (testing "stalled organization cursor"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"did not advance"
                          (history/fetch-organization
                            {:gh-runner (constantly {:exit 0,
                                                     :out (json/write-str
                                                            (org-response
                                                              {:has-next? true,
                                                               :cursor nil})),
                                                     :err ""})}))))
  (testing "missing organization pagination shape"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"missing repository pagination"
          (history/fetch-organization
            {:gh-runner (constantly {:exit 0,
                                     :out (json/write-str
                                            {:data {:organization
                                                      {:login "plurigrid"}}}),
                                     :err ""})}))))
  (testing "malformed organization pagination nodes"
    (let [page (update-in (org-response {})
                          [:data :organization :repositories]
                          dissoc
                          :nodes)]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"missing repository pagination"
            (history/fetch-organization
              {:gh-runner (constantly {:exit 0,
                                       :out (json/write-str page),
                                       :err ""})})))))
  (testing "malformed organization pagination flag"
    (let [page (update-in (org-response {})
                          [:data :organization :repositories :pageInfo]
                          dissoc
                          :hasNextPage)]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"missing repository pagination"
            (history/fetch-organization
              {:gh-runner (constantly {:exit 0,
                                       :out (json/write-str page),
                                       :err ""})})))))
  (testing "organization count changes during pagination"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"count changed"
          (history/fetch-organization
            {:gh-runner (constantly
                          {:exit 0,
                           :out (json/write-str
                                  (org-response
                                    {:nodes [(org-repo "plurigrid/alpha" "a")],
                                     :total 2})),
                           :err ""})}))))
  (testing "organization total changes between pages"
    (let [pages (atom [(org-response {:nodes [(org-repo "plurigrid/alpha" "a")],
                                      :has-next? true,
                                      :cursor "next",
                                      :total 2})
                       (org-response {:nodes [(org-repo "plurigrid/beta" "b")],
                                      :total 3})])]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"count changed"
                            (history/fetch-organization
                              {:gh-runner (fake-runner pages (atom []))})))))
  (testing "repository changes at an overlapping page boundary"
    (let [pages (atom [(org-response {:nodes [(org-repo "plurigrid/alpha" "a")],
                                      :has-next? true,
                                      :cursor "next",
                                      :total 2})
                       (org-response {:nodes [(org-repo "plurigrid/alpha"
                                                        "changed")
                                              (org-repo "plurigrid/beta" "b")],
                                      :total 2})])]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"repository changed"
                            (history/fetch-organization
                              {:gh-runner (fake-runner pages (atom []))})))))
  (testing "organization identity changes between pages"
    (let [second-page (assoc-in (org-response
                                  {:nodes [(org-repo "plurigrid/beta" "b")],
                                   :total 2})
                        [:data :organization :login]
                        "different")
          pages (atom [(org-response {:nodes [(org-repo "plurigrid/alpha" "a")],
                                      :has-next? true,
                                      :cursor "next",
                                      :total 2}) second-page])]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"identity changed"
                            (history/fetch-organization
                              {:gh-runner (fake-runner pages (atom []))})))))
  (testing "organization page bound"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"exceeded max-pages"
                          (history/fetch-organization
                            {:max-pages 0,
                             :gh-runner (constantly
                                          {:exit 0, :out "{}", :err ""})})))))

(deftest fetches-all-pages-oldest-first-and-deduplicates
  (let [calls (atom [])
        pages (atom [(response {:nodes [(commit "e" ["c" "d"])
                                        (commit "d" ["b"])],
                                :has-next? true,
                                :cursor "next",
                                :refs [{:name "main", :target {:oid "e"}}],
                                :tags [{:name "v1",
                                        :target {:__typename "Tag",
                                                 :target {:oid "a"}}}]})
                     (response {:nodes [(commit "d" ["b"]) (commit "c" ["b"])
                                        (commit "b" ["a"]) (commit "a" [])]})])
        result (history/fetch-history {:gh-runner (fake-runner pages calls)})]
    (is (= ["a" "b" "c" "d" "e"] (mapv :oid (:commits result))))
    (is (= 0 (:closure-fetches result)))
    (is (= 2 (:pages result)))
    (is (= [{:kind :branch, :name "main", :oid "e"}] (:refs result)))
    (is (= [{:kind :tag, :name "v1", :oid "a"}] (:tags result)))
    (is (= 2 (count @calls)))
    (is (some #{"cursor=next"} (second @calls)))))

(deftest fetch-error-paths
  (testing "gh process failure"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"gh api graphql failed"
          (history/fetch-history
            {:gh-runner (constantly {:exit 1, :out "", :err "auth failed"})}))))
  (testing "malformed JSON"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"unreadable JSON"
                          (history/fetch-history
                            {:gh-runner (constantly
                                          {:exit 0, :out "{", :err ""})}))))
  (testing "GraphQL errors"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"GraphQL returned errors"
          (history/fetch-history
            {:gh-runner (constantly {:exit 0,
                                     :out (json/write-str
                                            {:errors [{:message "nope"}]}),
                                     :err ""})}))))
  (testing "missing repository"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"not found"
                          (history/fetch-history
                            {:gh-runner (constantly {:exit 0,
                                                     :out (json/write-str
                                                            {:data {:repository
                                                                      nil}}),
                                                     :err ""})}))))
  (testing "missing history pagination"
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"missing commit pagination"
          (history/fetch-history
            {:gh-runner (constantly
                          {:exit 0,
                           :out (json/write-str
                                  {:data {:repository
                                          {:nameWithOwner "plurigrid/ontology"}}}),
                           :err ""})}))))
  (testing "malformed history pagination flag"
    (let [page (update-in (response {})
                          [:data :repository :object :history :pageInfo]
                          dissoc
                          :hasNextPage)]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"missing commit pagination"
            (history/fetch-history
              {:gh-runner (constantly {:exit 0,
                                       :out (json/write-str page),
                                       :err ""})})))))
  (testing "stalled cursor"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"did not advance"
                          (history/fetch-history
                            {:gh-runner (constantly {:exit 0,
                                                     :out (json/write-str
                                                            (response
                                                              {:has-next? true,
                                                               :cursor nil})),
                                                     :err ""})}))))
  (testing "pagination safety bound"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"exceeded max-pages"
                          (history/fetch-history
                            {:max-pages 0,
                             :gh-runner (constantly
                                          {:exit 0, :out "{}", :err ""})}))))
  (testing "truncated parents in the default-branch history"
    (let [head (assoc (commit "head" ["root"])
                 :parents {:totalCount 101, :nodes [{:oid "root"}]})]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"parent list exceeded"
            (history/fetch-history
              {:gh-runner (fake-runner
                            (atom [(response {:nodes [head]})])
                            (atom []))})))))
  (testing "truncated parents in a retained-root closure"
    (let [retained (assoc (commit "retained" ["root"])
                     :parents {:totalCount 101, :nodes [{:oid "root"}]})]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"parent list exceeded"
            (history/fetch-history
              {:gh-runner
                 (fake-runner
                   (atom [(response {:nodes [(commit "head" [])],
                                    :refs [{:name "retained",
                                            :target {:oid "retained"}}]})
                          (org-commit-response "plurigrid/ontology" retained)])
                   (atom []))})))))
  (testing "truncated branch roots"
    (let [page (assoc-in
                 (response {:nodes [(commit "head" [])],
                            :refs [{:name "main", :target {:oid "head"}}]})
                 [:data :repository :refs :totalCount]
                 101)]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"branch list was missing or truncated"
            (history/fetch-history
              {:gh-runner (fake-runner (atom [page]) (atom []))})))))
  (testing "truncated pull-request commits"
    (let [pull-request {:number 70,
                        :commits {:totalCount 101,
                                  :nodes [{:commit {:oid "head"}}]}}]
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"pull request commit list was missing or truncated"
            (history/fetch-history
              {:gh-runner
                 (fake-runner
                   (atom [(response {:nodes [(commit "head" [])],
                                    :pull-requests [pull-request]})])
                   (atom []))})))))
  (testing "invalid repository name"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"OWNER/NAME"
                          (history/fetch-history {:repository "ontology"})))))

(deftest prepares-complete-dag-and-specter-queries
  (let [prepared (history/prepare-history fixture-history)]
    (is (= ["c" "d"] (get-in prepared [:by-oid "e" :parents-known])))
    (is (= ["c" "d"] (get-in prepared [:by-oid "b" :children-known])))
    (is (= [{:kind :tag, :name "v1"}] (get-in prepared [:by-oid "a" :labels])))
    (is (= ["merge side"]
           (mapv :messageHeadline (history/merge-commits prepared))))
    (is (= ["side branch" "merge side"]
           (mapv :messageHeadline (history/commits-touching prepared "SIDE"))))
    (is (= ["a" "b" "c" "d" "e"]
           (history/specter-select [:commits sp/ALL :oid] prepared)))))

(deftest lazy-organization-walk-is-deterministic-and-fork-safe
  (let [catalog
          {:kind :github-organization,
           :organization "plurigrid",
           :repositories [(org-repo "plurigrid/alpha" "shared")
                          (org-repo "plurigrid/fork" "shared" {:isFork true})],
           :by-name {"plurigrid/alpha" (org-repo "plurigrid/alpha" "shared"),
                     "plurigrid/fork"
                       (org-repo "plurigrid/fork" "shared" {:isFork true})}}
        responses (fn []
                    (atom [(org-commit-response
                             "plurigrid/alpha"
                             (commit "shared" ["root"] "alpha head"))
                           (org-commit-response "plurigrid/alpha"
                                                (commit "root" [] "alpha root"))
                           (org-commit-response
                             "plurigrid/fork"
                             (commit "shared" ["root"] "fork head"))]))
        run (fn []
              (let [calls (atom [])]
                {:walk (history/organization-random-walk
                         catalog
                         {:repository "plurigrid/alpha",
                          :seed 69,
                          :steps 2,
                          :restart? true,
                          :gh-runner (fake-runner (responses) calls)}),
                 :calls @calls}))
        first-run (run)
        second-run (run)
        walk (:walk first-run)]
    (is (= (:walk first-run) (:walk second-run)))
    (is (= [["plurigrid/alpha" "shared"] ["plurigrid/alpha" "root"]
            ["plurigrid/fork" "shared"]]
           (mapv :key walk)))
    (is (= ["plurigrid/alpha" "plurigrid/alpha" "plurigrid/fork"]
           (mapv :repository walk)))
    (is (true? (:teleport (second walk))))
    (is (= 3 (count (:calls first-run))))
    (is (every? #{-1 0 1} (map :trit walk)))
    (is (zero? (mod (reduce + (map :trit walk)) 3)))
    (is (every? #(re-matches #"#[0-9A-F]{6}" %) (map :color walk)))))

(deftest organization-restart-does-not-reenter-start-repository
  (let [catalog {:kind :github-organization,
                 :organization "plurigrid",
                 :repositories [(org-repo "plurigrid/alpha" "alpha-head")
                                (org-repo "plurigrid/beta" "beta-head")],
                 :by-name
                   {"plurigrid/alpha" (org-repo "plurigrid/alpha" "alpha-head"),
                    "plurigrid/beta" (org-repo "plurigrid/beta" "beta-head")}}
        responses (atom [(org-commit-response "plurigrid/alpha"
                                              (commit "alpha-root" []))
                         (org-commit-response "plurigrid/beta"
                                              (commit "beta-head" []))])
        walk (history/organization-random-walk
               catalog
               {:repository "plurigrid/alpha",
                :start-oid "alpha-root",
                :seed 69,
                :steps 1,
                :restart? true,
                :gh-runner (fake-runner responses (atom []))})]
    (is (= [["plurigrid/alpha" "alpha-root"] ["plurigrid/beta" "beta-head"]]
           (mapv :key walk)))
    (is (true? (:teleport (first walk))))))

(deftest organization-walk-boundaries-and-failures
  (let [empty-catalog {:kind :github-organization,
                       :organization "plurigrid",
                       :repositories [(org-repo "plurigrid/empty" nil)]}
        catalog {:kind :github-organization,
                 :organization "plurigrid",
                 :repositories [(org-repo "plurigrid/alpha" "head")],
                 :by-name {"plurigrid/alpha" (org-repo "plurigrid/alpha"
                                                       "head")}}
        missing-head-catalog
          {:kind :github-organization,
           :organization "plurigrid",
           :repositories [(org-repo "plurigrid/no-head" nil {:isEmpty false})],
           :by-name {"plurigrid/no-head"
                       (org-repo "plurigrid/no-head" nil {:isEmpty false})}}
        one-response (fn []
                       (atom [(org-commit-response "plurigrid/alpha"
                                                   (commit "head" []))]))]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"no non-empty"
                          (history/organization-random-walk empty-catalog)))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"not present"
                          (history/organization-random-walk
                            catalog
                            {:repository "plurigrid/missing"})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"empty repository"
                          (history/organization-random-walk
                            missing-head-catalog
                            {:repository "plurigrid/no-head"})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"non-negative"
                          (history/organization-random-walk catalog
                                                            {:steps -1})))
    (is (= 1
           (count (history/organization-random-walk
                    catalog
                    {:repository "plurigrid/alpha",
                     :steps 0,
                     :gh-runner (fake-runner (one-response) (atom []))}))))
    (is (zero? (:trit (first (history/organization-random-walk
                               catalog
                               {:repository "plurigrid/alpha",
                                :steps 0,
                                :gh-runner (fake-runner (one-response)
                                                        (atom []))})))))
    (is (= 1
           (count (history/organization-random-walk
                    catalog
                    {:repository "plurigrid/alpha",
                     :steps 10,
                     :gh-runner (fake-runner (one-response) (atom []))}))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"OID must not be blank"
                          (history/fetch-org-commit
                            "plurigrid/alpha"
                            nil
                            {:gh-runner (fn [_]
                                          (throw
                                            (AssertionError.
                                              "runner must not be called")))})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"exceeded the GraphQL page"
                          (history/fetch-org-commit
                            "plurigrid/alpha"
                            "octopus"
                            {:gh-runner
                               (constantly
                                 {:exit 0,
                                  :out (json/write-str
                                         (org-commit-response
                                           "plurigrid/alpha"
                                           (assoc (commit "octopus" ["a"])
                                             :parents {:totalCount 101,
                                                       :nodes [{:oid "a"}]}))),
                                  :err ""})})))
    (is (thrown-with-msg?
          clojure.lang.ExceptionInfo
          #"Commit was not found"
          (history/fetch-org-commit
            "plurigrid/alpha"
            "missing"
            {:gh-runner (constantly {:exit 0,
                                     :out (json/write-str
                                            {:data {:repository
                                                      {:nameWithOwner
                                                         "plurigrid/alpha",
                                                       :object nil}}}),
                                     :err ""})})))
    (is (nil? (history/fetch-org-commit
                "plurigrid/alpha"
                "missing"
                {:missing-ok? true,
                 :gh-runner (constantly {:exit 0,
                                         :out (json/write-str
                                                {:data {:repository
                                                          {:nameWithOwner
                                                             "plurigrid/alpha",
                                                           :object nil}}}),
                                         :err ""})})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"identity changed"
                          (history/fetch-org-commit
                            "plurigrid/alpha"
                            "head"
                            {:gh-runner (constantly
                                          {:exit 0,
                                           :out (json/write-str
                                                  (org-commit-response
                                                    "plurigrid/renamed"
                                                    (commit "head" []))),
                                           :err ""})})))))

(deftest cli-option-values-fail-closed
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"--org requires a value"
                        (#'history/parse-args ["org-walk" "--org"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"--direction requires a value"
                        (#'history/parse-args
                         ["walk" "--direction" "--restart"])))
  (is (= "abc123"
         (:start-oid (#'history/parse-args
                      ["org-walk" "--start-oid" "abc123"]))))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"--start-oid requires a value"
                        (#'history/parse-args ["org-walk" "--start-oid"]))))

(deftest deterministic-walk-and-boundaries
  (let [a (history/random-walk fixture-history
                               {:seed 69, :steps 4, :direction :past})
        b (history/random-walk fixture-history
                               {:seed 69, :steps 4, :direction :past})]
    (is (= a b))
    (is (= "e" (:oid (first a))))
    (is (= "a" (:oid (last a))))
    (is (every? #{-1 0 1} (map :trit a)))
    (is (zero? (mod (reduce + (map :trit a)) 3)))
    (is (every? #(re-matches #"#[0-9A-F]{6}" %) (map :color a)))
    (is (= 1 (count (history/random-walk fixture-history {:steps 0}))))
    (is (= "a"
           (:oid (first (history/random-walk fixture-history
                                             {:start :oldest, :steps 0})))))
    (is (= "c"
           (:oid (first (history/random-walk fixture-history
                                             {:start "side", :steps 0})))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"non-negative"
                          (history/random-walk fixture-history {:steps -1})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"resolved"
                          (history/random-walk fixture-history
                                               {:start "missing"})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Direction"
                          (history/random-walk fixture-history
                                               {:direction :sideways})))))

(deftest bidirectional-walk-prefers-unvisited-neighbors
  (let [walk (history/random-walk
               fixture-history
               {:seed 1, :steps 8, :direction :both, :start "b"})
        oids (mapv :oid walk)]
    (is (= 5 (count (set oids))))
    (is (= 9 (count walk)))
    (is (every? string? oids))))

(deftest restart-covers-disconnected-components
  (let [disconnected (assoc fixture-history
                       :default-commit-oids ["a" "x"]
                       :refs [{:kind :branch, :name "main", :oid "x"}]
                       :tags []
                       :commits [(commit "a" []) (commit "x" [])])
        walk (history/random-walk
               disconnected
               {:start :newest, :steps 2, :direction :past, :restart? true})]
    (is (= #{"a" "x"} (set (map :oid walk))))
    (is (true? (:teleport (first walk))))))

(deftest empty-history-and-missing-parent-are-safe
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"empty history"
                        (history/random-walk
                          {:commits [], :refs [], :tags []})))
  (let [partial {:commits [(commit "b" ["not-fetched"])], :refs [], :tags []}
        prepared (history/prepare-history partial)]
    (is (empty? (get-in prepared [:by-oid "b" :parents-known])))
    (is (= 1 (count (history/random-walk prepared {:steps 10}))))))

(deftest cache-round-trip-and-integrity
  (let [path (str (java.nio.file.Files/createTempFile
                    "ontology-history-test-"
                    ".edn"
                    (make-array java.nio.file.attribute.FileAttribute 0)))
        org-path (str (java.nio.file.Files/createTempFile
                        "ontology-org-test-"
                        ".edn"
                        (make-array java.nio.file.attribute.FileAttribute 0)))
        catalog {:schema-version 1,
                 :kind :github-organization,
                 :organization "plurigrid",
                 :repositories [(org-repo "plurigrid/alpha" "a")],
                 :by-name {"plurigrid/alpha" (org-repo "plurigrid/alpha" "a")}}]
    (try (history/write-cache! path fixture-history)
         (is (= fixture-history (history/read-cache path)))
         (history/write-cache! org-path catalog)
         (is (= catalog (history/read-cache org-path)))
         (is (thrown-with-msg? clojure.lang.ExceptionInfo
                               #"does not match --repo"
                               (history/load-history
                                 {:cache-file path,
                                  :repository "plurigrid/different"})))
         (is (thrown-with-msg? clojure.lang.ExceptionInfo
                               #"does not match --org"
                               (history/load-organization {:cache-file org-path,
                                                           :organization
                                                             "different"})))
         (spit path (clojure.string/replace (slurp path) "commit a" "tampered"))
         (is (thrown-with-msg? clojure.lang.ExceptionInfo
                               #"failed validation"
                               (history/read-cache path)))
         (finally (java.nio.file.Files/deleteIfExists
                    (java.nio.file.Path/of path (make-array String 0)))
                  (java.nio.file.Files/deleteIfExists
                    (java.nio.file.Path/of org-path (make-array String 0)))))))
