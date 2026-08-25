(ns plurigrid.ontology.test-runner
  (:require [clojure.test :as test]
            [plurigrid.ontology.history-test :as history-test]))

(defn -main [& _]
  ;; Referencing the alias keeps static analyzers and namespace loading aligned.
  (assert (some? history-test/fixture-history))
  (let [{:keys [fail error]} (test/run-tests 'plurigrid.ontology.history-test)]
    (shutdown-agents)
    (when (pos? (+ fail error))
      (System/exit 1))))
