(ns meshifier.validate-meshes
  (:gen-class)
  (:require [meshifier.mesh-validator :refer [validate-mesh validate-mesh-json validate-all-mesh-types]]
            [meshifier.core :refer [available-shapes]]
            [clojure.data.json :as json]
            [clojure.string :as str]))

(defn print-validation-results
  "Print validation results in a readable format"
  [results]
  (println "\nMesh Validation Results:")
  (println "=======================")
  (doseq [[mesh-name result] results]
    (println (format "\n%s:" mesh-name))
    (if (:valid result)
      (println "  ✓ Valid")
      (println (format "  ✗ Invalid: %s" (:error result))))))

(defn -main [& args]
  (println "\nValidating all mesh types...")
  
  ; Extract generator functions from available-shapes
  (let [mesh-generators (reduce (fn [acc [id shape-info]]
                                 (assoc acc (:name shape-info) (:generator shape-info)))
                               {}
                               available-shapes)
        results (validate-all-mesh-types mesh-generators)]
    
    (print-validation-results results)
    
    ; Exit with error code if any mesh is invalid
    (if (every? :valid (vals results))
      (do
        (println "\nAll meshes are valid!")
        (System/exit 0))
      (do
        (println "\nSome meshes are invalid!")
        (System/exit 1)))))