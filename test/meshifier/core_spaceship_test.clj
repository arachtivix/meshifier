(ns meshifier.core-spaceship-test
  (:require [clojure.test :refer :all]
            [meshifier.core-spaceship :refer [generate-spaceship-mesh]]
            [clojure.data.json :as json]))

(deftest generate-spaceship-mesh-test
  (testing "generate-spaceship-mesh returns correct structure"
    (let [mesh (generate-spaceship-mesh)]
      ; Test basic structure
      (is (map? mesh))
      (is (contains? mesh :vertices))
      (is (contains? mesh :faces))
      
      ; Test vertex structure
      (is (every? vector? (:vertices mesh)) "Vertices should be vectors")
      (is (every? #(= 3 (count %)) (:vertices mesh)) "Vertices should have 3 coordinates")
      (is (every? number? (flatten (:vertices mesh))) "Vertex coordinates should be numbers")
      
      ; Test face structure
      (is (every? vector? (:faces mesh)) "Faces should be vectors")
      (is (every? #(= 3 (count %)) (:faces mesh)) "Faces should have 3 vertices")
      (is (every? #(every? (fn [idx] (< idx (count (:vertices mesh)))) %) (:faces mesh))
          "Face indices should be less than vertex count")
      
      ; Test component counts
      (let [vertex-count (count (:vertices mesh))
            face-count (count (:faces mesh))]
        ; Each cuboid has 8 vertices and 12 faces
        ; Spaceship has 8 components (main body, cockpit, 2 wings, 2 engines, 2 nozzles)
        (is (= (* 8 8) vertex-count) "Should have correct number of vertices")
        (is (= (* 12 8) face-count) "Should have correct number of faces"))
      
      ; Test spaceship dimensions and component positions
      (let [vertices (:vertices mesh)
            x-coords (map first vertices)
            y-coords (map second vertices)
            z-coords (map last vertices)
            x-min (apply min x-coords)
            x-max (apply max x-coords)
            y-min (apply min y-coords)
            y-max (apply max y-coords)
            z-min (apply min z-coords)
            z-max (apply max z-coords)]
        
        ; Test overall dimensions are reasonable
        (is (> (- x-max x-min) 0) "Spaceship should have positive width")
        (is (> (- y-max y-min) 0) "Spaceship should have positive height")
        (is (> (- z-max z-min) 0) "Spaceship should have positive depth")
        
        ; Test relative positions
        (is (> (- x-max x-min) 2.0) "Spaceship should be wide enough for wings")
        (is (> (- z-max z-min) 3.0) "Spaceship should be long enough for body and engines")
        (is (> z-max 1.0) "Cockpit should extend forward")
        (is (< z-min -2.0) "Engines should extend backward")))))