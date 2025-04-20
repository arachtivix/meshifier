(ns meshifier.mesh-validator-test
  (:require [clojure.test :refer :all]
            [meshifier.mesh-validator :refer :all]
            [meshifier.core :refer [tetrahedron-mesh joined-tetrahedrons-mesh generate-chair-mesh generate-cat-mesh]]
            [meshifier.core-spaceship :refer [generate-spaceship-mesh]]
            [clojure.data.json :as json]))

(deftest mesh-validation-test
  (testing "validate-mesh with valid mesh data"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]}
          result (validate-mesh mesh)]
      (is (:valid result) "Valid mesh should pass validation")
      (is (nil? (:error result)) "No error should be reported for valid mesh")))
  
  (testing "validate-mesh with missing vertices"
    (let [mesh {:faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]}
          result (validate-mesh mesh)]
      (is (not (:valid result)) "Mesh without vertices should fail validation")
      (is (= "Missing required property: vertices" (:error result)))))
  
  (testing "validate-mesh with missing faces"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]}
          result (validate-mesh mesh)]
      (is (not (:valid result)) "Mesh without faces should fail validation")
      (is (= "Missing required property: faces" (:error result)))))
  
  (testing "validate-mesh with invalid vertex format"
    (let [mesh {:vertices [[0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]}
          result (validate-mesh mesh)]
      (is (not (:valid result)) "Mesh with invalid vertex format should fail validation")
      (is (= "Invalid vertex format. Each vertex must be an array of 3 numbers" (:error result)))))
  
  (testing "validate-mesh with invalid face format"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 5] [0 2 3] [1 2 3]]}
          result (validate-mesh mesh)]
      (is (not (:valid result)) "Mesh with invalid face format should fail validation")
      (is (= "Invalid face format. Each face must be an array of at least 3 valid vertex indices" (:error result)))))
  
  (testing "validate-mesh-json with valid JSON"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]}
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Valid JSON should pass validation")))
  
  (testing "validate-mesh-json with invalid JSON"
    (let [json-str "{\"vertices\":[[0,0,0],[1,0,0],[0,1,0],[0,0,1]],\"faces\":[[0,1,2],[0,1,3],[0,2,3],[1,2,3]"
          result (validate-mesh-json json-str)]
      (is (not (:valid result)) "Invalid JSON should fail validation")
      (is (re-find #"Invalid JSON:" (:error result))))))

(deftest mesh-types-validation-test
  (testing "Validate tetrahedron mesh"
    (let [mesh (tetrahedron-mesh)
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Tetrahedron mesh should be valid")
      (is (nil? (:error result)) "No error should be reported for tetrahedron mesh")))
  
  (testing "Validate joined-tetrahedrons mesh"
    (let [mesh (joined-tetrahedrons-mesh)
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Joined tetrahedrons mesh should be valid")
      (is (nil? (:error result)) "No error should be reported for joined tetrahedrons mesh")))
  
  (testing "Validate chair mesh"
    (let [mesh (generate-chair-mesh)
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Chair mesh should be valid")
      (is (nil? (:error result)) "No error should be reported for chair mesh")))
  
  (testing "Validate cat mesh"
    (let [mesh (generate-cat-mesh)
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Cat mesh should be valid")
      (is (nil? (:error result)) "No error should be reported for cat mesh")))
  
  (testing "Validate spaceship mesh"
    (let [mesh (generate-spaceship-mesh)
          json-str (json/write-str mesh)
          result (validate-mesh-json json-str)]
      (is (:valid result) "Spaceship mesh should be valid")
      (is (nil? (:error result)) "No error should be reported for spaceship mesh"))))

(deftest additional-properties-test
  (testing "Mesh with additional properties should still be valid"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2] [0 1 3] [0 2 3] [1 2 3]]
                :color [1.0 0.0 0.0]
                :name "Test Mesh"
                :metadata {:author "Test Author"
                           :created "2023-01-01"}}
          result (validate-mesh mesh)]
      (is (:valid result) "Mesh with additional properties should be valid")))
  
  (testing "Mesh with non-triangular faces should be valid"
    (let [mesh {:vertices [[0 0 0] [1 0 0] [1 1 0] [0 1 0] [0 0 1]]
                :faces [[0 1 2 3] [0 1 4] [1 2 4] [2 3 4] [3 0 4]]}
          result (validate-mesh mesh)]
      (is (:valid result) "Mesh with non-triangular faces should be valid"))))

