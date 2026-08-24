(ns plurigrid.ontology.history-test
  (:require [clojure.data.json :as json]
            [clojure.test :refer [deftest is testing]]
            [com.rpl.specter :as sp]
            [plurigrid.ontology.history :as history]))

(defn commit
  ([oid parents] (commit oid parents (str "commit " oid)))
  ([oid parents headline]
   {:oid oid
    :abbreviatedOid oid
    :committedDate (str "2023-01-0" (inc (count parents)) "T00:00:00Z")
    :messageHeadline headline
    :additions 1
    :deletions 0
    :changedFilesIfAvailable 1
    :author {:name "monaduck1069" :email "monaduck1069@users.noreply.github.com"}
    :committer {:name "monaduck1069" :email "monaduck1069@users.noreply.github.com"}
    :parents {:totalCount (count parents) :nodes (mapv #(hash-map :oid %) parents)}
    :tree {:oid (str "tree-" oid)}}))

(def fixture-history
  {:schema-version 1
   :repository "plurigrid/ontology"
   :url "https://github.com/plurigrid/ontology"
   :default-branch "main"
   :fetched-at "2026-08-24T00:00:00Z"
   :pages 1
   :default-commit-oids ["a" "b" "c" "d" "e"]
   :refs [{:kind :branch :name "main" :oid "e"}
          {:kind :branch :name "side" :oid "c"}]
   :tags [{:kind :tag :name "v1" :oid "a"}]
   :rate-limit []
   ;; Oldest first.  e is a merge of c and d; b is shared ancestry.
   :commits [(commit "a" [])
             (commit "b" ["a"])
             (commit "c" ["b"] "side branch")
             (commit "d" ["b"])
             (commit "e" ["c" "d"] "merge side") ]})

(defn response
  [{:keys [nodes has-next? cursor refs tags]
    :or {nodes [] has-next? false}}]
  {:data
   {:repository
    {:nameWithOwner "plurigrid/ontology"
     :url "https://github.com/plurigrid/ontology"
     :defaultBranchRef {:name "main"}
     :refs {:nodes (or refs [])}
     :tags {:nodes (or tags [])}
     :pullRequests {:nodes []}
     :object {:history {:pageInfo {:hasNextPage has-next? :endCursor cursor}
                        :nodes nodes}}}
    :rateLimit {:cost 1 :remaining 4999}}})

(defn fake-runner [responses calls]
  (fn [args]
    (swap! calls conj args)
    (let [value (first @responses)]
      (swap! responses subvec 1)
      (if (map? value)
        {:exit 0 :out (json/write-str value) :err ""}
        value))))

(deftest fetches-all-pages-oldest-first-and-deduplicates
  (let [calls (atom [])
        pages (atom [(response {:nodes [(commit "e" ["c" "d"])
                                        (commit "d" ["b"])]
                                :has-next? true
                                :cursor "next"
                                :refs [{:name "main" :target {:oid "e"}}]
                                :tags [{:name "v1" :target {:__typename "Tag"
                                                            :target {:oid "a"}}}]})
                     (response {:nodes [(commit "d" ["b"])
                                        (commit "c" ["b"])
                                        (commit "b" ["a"])
                                        (commit "a" [])]})])
        result (history/fetch-history {:gh-runner (fake-runner pages calls)})]
    (is (= ["a" "b" "c" "d" "e"] (mapv :oid (:commits result))))
    (is (= 0 (:closure-fetches result)))
    (is (= 2 (:pages result)))
    (is (= [{:kind :branch :name "main" :oid "e"}] (:refs result)))
    (is (= [{:kind :tag :name "v1" :oid "a"}] (:tags result)))
    (is (= 2 (count @calls)))
    (is (some #{"cursor=next"} (second @calls)))))

(deftest fetch-error-paths
  (testing "gh process failure"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"gh api graphql failed"
         (history/fetch-history
          {:gh-runner (constantly {:exit 1 :out "" :err "auth failed"})}))))
  (testing "malformed JSON"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"unreadable JSON"
         (history/fetch-history
          {:gh-runner (constantly {:exit 0 :out "{" :err ""})}))))
  (testing "GraphQL errors"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"GraphQL returned errors"
         (history/fetch-history
          {:gh-runner (constantly {:exit 0
                                   :out (json/write-str {:errors [{:message "nope"}]})
                                   :err ""})}))))
  (testing "missing repository"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"not found"
         (history/fetch-history
          {:gh-runner (constantly {:exit 0
                                   :out (json/write-str {:data {:repository nil}})
                                   :err ""})}))))
  (testing "stalled cursor"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"did not advance"
         (history/fetch-history
          {:gh-runner (constantly {:exit 0
                                   :out (json/write-str
                                         (response {:has-next? true :cursor nil}))
                                   :err ""})}))))
  (testing "pagination safety bound"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"exceeded max-pages"
         (history/fetch-history
          {:max-pages 0
           :gh-runner (constantly {:exit 0 :out "{}" :err ""})}))))
  (testing "invalid repository name"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"OWNER/NAME"
         (history/fetch-history {:repository "ontology"})))))

(deftest prepares-complete-dag-and-specter-queries
  (let [prepared (history/prepare-history fixture-history)]
    (is (= ["c" "d"] (get-in prepared [:by-oid "e" :parents-known])))
    (is (= ["c" "d"] (get-in prepared [:by-oid "b" :children-known])))
    (is (= [{:kind :tag :name "v1"}]
           (get-in prepared [:by-oid "a" :labels])))
    (is (= ["merge side"]
           (mapv :messageHeadline (history/merge-commits prepared))))
    (is (= ["side branch" "merge side"]
           (mapv :messageHeadline (history/commits-touching prepared "SIDE"))))
    (is (= ["a" "b" "c" "d" "e"]
           (history/specter-select [:commits sp/ALL :oid] prepared)))))

(deftest deterministic-walk-and-boundaries
  (let [a (history/random-walk fixture-history {:seed 69 :steps 4 :direction :past})
        b (history/random-walk fixture-history {:seed 69 :steps 4 :direction :past})]
    (is (= a b))
    (is (= "e" (:oid (first a))))
    (is (= "a" (:oid (last a))))
    (is (every? #{-1 0 1} (map :trit a)))
    (is (every? #(re-matches #"#[0-9A-F]{6}" %) (map :color a)))
    (is (= 1 (count (history/random-walk fixture-history {:steps 0}))))
    (is (= "a" (:oid (first (history/random-walk fixture-history
                                                   {:start :oldest :steps 0})))))
    (is (= "c" (:oid (first (history/random-walk fixture-history
                                                   {:start "side" :steps 0})))))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"non-negative"
                          (history/random-walk fixture-history {:steps -1})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"resolved"
                          (history/random-walk fixture-history {:start "missing"})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Direction"
                          (history/random-walk fixture-history {:direction :sideways})))))

(deftest bidirectional-walk-prefers-unvisited-neighbors
  (let [walk (history/random-walk fixture-history
                                  {:seed 1 :steps 8 :direction :both :start "b"})
        oids (mapv :oid walk)]
    (is (= 5 (count (set oids))))
    (is (= 9 (count walk)))
    (is (every? string? oids))))

(deftest restart-covers-disconnected-components
  (let [disconnected (assoc fixture-history
                            :default-commit-oids ["a" "x"]
                            :refs [{:kind :branch :name "main" :oid "x"}]
                            :tags []
                            :commits [(commit "a" []) (commit "x" [])])
        walk (history/random-walk disconnected
                                  {:start :newest :steps 2 :direction :past :restart? true})]
    (is (= #{"a" "x"} (set (map :oid walk))))
    (is (true? (:teleport (first walk))))))

(deftest empty-history-and-missing-parent-are-safe
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"empty history"
                        (history/random-walk {:commits [] :refs [] :tags []})))
  (let [partial {:commits [(commit "b" ["not-fetched"])] :refs [] :tags []}
        prepared (history/prepare-history partial)]
    (is (empty? (get-in prepared [:by-oid "b" :parents-known])))
    (is (= 1 (count (history/random-walk prepared {:steps 10}))))))

(deftest cache-round-trip-and-integrity
  (let [path (str (java.nio.file.Files/createTempFile
                   "ontology-history-test-" ".edn"
                   (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (history/write-cache! path fixture-history)
      (is (= fixture-history (history/read-cache path)))
      (spit path (clojure.string/replace (slurp path) "commit a" "tampered"))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"failed validation"
                            (history/read-cache path)))
      (finally
        (java.nio.file.Files/deleteIfExists (java.nio.file.Path/of path (make-array String 0)))))))
