(ns meshifier.validate-meshes-test
  (:require [clojure.test :refer :all]
            [meshifier.mesh-validator :refer [validate-mesh validate-mesh-json validate-all-mesh-types]]
            [meshifier.core :refer [available-shapes tetrahedron-mesh joined-tetrahedrons-mesh generate-chair-mesh generate-cat-mesh]]
            [meshifier.core-spaceship :refer [generate-spaceship-mesh]]
            [clojure.data.json :as json]))

(deftest validate-all-mesh-types-test
  (testing "Validate all mesh types"
    (let [mesh-generators {"Regular Tetrahedron" tetrahedron-mesh
                           "Joined Tetrahedrons" joined-tetrahedrons-mesh
                           "Simple Chair" generate-chair-mesh
                           "Stylized Cat" generate-cat-mesh
                           "Space Ship" generate-spaceship-mesh}
          results (validate-all-mesh-types mesh-generators)]
      
      (is (every? :valid (vals results)) "All mesh types should be valid")
      
      (doseq [[mesh-name result] results]
        (is (:valid result) (str mesh-name " should be valid"))
        (is (nil? (:error result)) (str "No error should be reported for " mesh-name))))))

(deftest validate-non-triangular-faces-test
  (testing "Validate mesh with non-triangular faces"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [1 1 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2 3] [0 1 4] [1 2 4] [2 3 4] [3 0 4]]}
          result (validate-mesh mesh)]
      (is (:valid result) "Mesh with non-triangular faces should be valid"))))

(deftest validate-mesh-with-additional-properties-test
  (testing "Validate mesh with additional properties"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]
                :color [1.0 0.0 0.0]
                :material {:name "Red Material"
                           :roughness 0.5
                           :metallic 0.0}
                :metadata {:author "Test Author"
                           :created "2023-01-01"
                           :tags ["test" "mesh" "validation"]}}
          result (validate-mesh mesh)]
      (is (:valid result) "Mesh with additional properties should be valid"))))