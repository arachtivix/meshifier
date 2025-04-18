(ns meshifier.core-test
  (:require [clojure.test :refer :all]
            [meshifier.core :refer :all]
            [clojure.data.json :as json]))

(deftest point-in-tetrahedron-test
  (testing "point-in-tetrahedron? with regular tetrahedron"
    (let [p1 [1.0 1.0 1.0]      ; vertex 0
          p2 [-1.0 -1.0 1.0]    ; vertex 1
          p3 [-1.0 1.0 -1.0]    ; vertex 2
          p4 [1.0 -1.0 -1.0]]   ; vertex 3
      
      ; Test points inside
      (is (point-in-tetrahedron? [0.0 0.0 0.0] p1 p2 p3 p4) "Center point should be inside")
      (is (point-in-tetrahedron? [0.5 0.5 0.5] p1 p2 p3 p4) "Point near vertex p1 should be inside")
      
      ; Test vertices (should be inside/on surface)
      (is (point-in-tetrahedron? p1 p1 p2 p3 p4) "Vertex p1 should be inside")
      (is (point-in-tetrahedron? p2 p1 p2 p3 p4) "Vertex p2 should be inside")
      (is (point-in-tetrahedron? p3 p1 p2 p3 p4) "Vertex p3 should be inside")
      (is (point-in-tetrahedron? p4 p1 p2 p3 p4) "Vertex p4 should be inside")
      
      ; Test points on faces (should be inside/on surface)
      (is (point-in-tetrahedron? [(/ (+ (p1 0) (p2 0) (p3 0)) 3)
                                 (/ (+ (p1 1) (p2 1) (p3 1)) 3)
                                 (/ (+ (p1 2) (p2 2) (p3 2)) 3)]
                                p1 p2 p3 p4)
          "Point on face center should be inside")
      
      ; Test points outside
      (is (not (point-in-tetrahedron? [2.0 2.0 2.0] p1 p2 p3 p4)) "Point far from tetrahedron should be outside")
      (is (not (point-in-tetrahedron? [-2.0 -2.0 -2.0] p1 p2 p3 p4)) "Point far from tetrahedron should be outside")
      
      ; Test edge cases
      (is (not (point-in-tetrahedron? [0.0 0.0 0.0] p1 p1 p2 p3))
          "Degenerate tetrahedron (repeated vertex) should return false")))
  
  (testing "point-in-tetrahedron? with degenerate cases"
    (let [p1 [0.0 0.0 0.0]
          p2 [1.0 0.0 0.0]
          p3 [0.0 1.0 0.0]
          p4 [0.0 0.0 0.0]]  ; p4 same as p1 - degenerate case
      (is (not (point-in-tetrahedron? [0.25 0.25 0.0] p1 p2 p3 p4))
          "Point with degenerate tetrahedron should return false")))
  
  (testing "point-in-tetrahedron? with flat tetrahedron"
    (let [p1 [0.0 0.0 0.0]
          p2 [1.0 0.0 0.0]
          p3 [0.0 1.0 0.0]
          p4 [1.0 1.0 0.0]]  ; All points in same plane - degenerate case
      (is (not (point-in-tetrahedron? [0.5 0.5 0.0] p1 p2 p3 p4))
          "Point with flat tetrahedron should return false"))))

(deftest tetrahedron-mesh-test
  (testing "tetrahedron-mesh returns correct structure"
    (let [mesh (tetrahedron-mesh)]
      (is (map? mesh))
      (is (contains? mesh :vertices))
      (is (contains? mesh :faces))
      (is (= 4 (count (:vertices mesh))))
      (is (= 4 (count (:faces mesh))))
      (is (every? #(= 3 (count %)) (:vertices mesh)))
      (is (every? #(= 3 (count %)) (:faces mesh)))
      (is (every? number? (flatten (:vertices mesh))))
      (is (every? int? (flatten (:faces mesh)))))))

(deftest joined-tetrahedrons-mesh-test
  (testing "joined-tetrahedrons-mesh returns correct structure"
    (let [mesh (joined-tetrahedrons-mesh)]
      ; Test basic structure
      (is (map? mesh))
      (is (contains? mesh :vertices))
      (is (contains? mesh :faces))
      
      ; Test counts
      (is (= 5 (count (:vertices mesh))) "Should have 5 vertices (3 shared + 2 unique)")
      (is (= 7 (count (:faces mesh))) "Should have 7 faces (4 from first tetrahedron + 3 from second)")
      
      ; Test vertex and face structure
      (is (every? #(= 3 (count %)) (:vertices mesh)) "Each vertex should have 3 coordinates")
      (is (every? #(= 3 (count %)) (:faces mesh)) "Each face should have 3 vertices")
      (is (every? number? (flatten (:vertices mesh))) "All vertex coordinates should be numbers")
      (is (every? int? (flatten (:faces mesh))) "All face indices should be integers")
      
      ; Test vertex indices are valid
      (is (every? #(every? (fn [idx] (< idx 5)) %) (:faces mesh))
          "All face indices should be less than vertex count")
      
      ; Get the vertices for testing point locations
      (let [vertices (:vertices mesh)
            p1 (nth vertices 0)  ; shared vertices
            p2 (nth vertices 1)
            p3 (nth vertices 2)
            p4 (nth vertices 3)  ; unique vertex of first tetrahedron
            p5 (nth vertices 4)] ; unique vertex of second tetrahedron
        
        ; Test that unique vertices lie outside the opposite tetrahedron
        (is (not (point-in-tetrahedron? p4 p1 p2 p3 p5))
            "First tetrahedron's unique vertex should be outside second tetrahedron")
        (is (not (point-in-tetrahedron? p5 p1 p2 p3 p4))
            "Second tetrahedron's unique vertex should be outside first tetrahedron")))))


(deftest generate-cuboid-mesh-test
  (testing "generate-cuboid-mesh returns correct structure"
    (let [width 2.0
          height 1.0
          depth 3.0
          position [1.0 2.0 3.0]
          mesh (generate-cuboid-mesh width height depth position)]
      
      ; Test basic structure
      (is (map? mesh))
      (is (contains? mesh :vertices))
      (is (contains? mesh :faces))
      
      ; Test counts
      (is (= 8 (count (:vertices mesh))) "Should have 8 vertices")
      (is (= 12 (count (:faces mesh))) "Should have 12 triangular faces")
      
      ; Test vertex structure
      (is (every? vector? (:vertices mesh)) "Vertices should be vectors")
      (is (every? #(= 3 (count %)) (:vertices mesh)) "Vertices should have 3 coordinates")
      (is (every? number? (flatten (:vertices mesh))) "Vertex coordinates should be numbers")
      
      ; Test face structure
      (is (every? vector? (:faces mesh)) "Faces should be vectors")
      (is (every? #(= 3 (count %)) (:faces mesh)) "Faces should have 3 vertices")
      (is (every? #(every? (fn [idx] (< idx 8)) %) (:faces mesh))
          "Face indices should be less than vertex count")
      
      ; Test dimensions
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
        (is (= width (- x-max x-min)) "Width should match")
        (is (= height (- y-max y-min)) "Height should match")
        (is (= depth (- z-max z-min)) "Depth should match")
        (is (= position [(/ (+ x-min x-max) 2)
                        (/ (+ y-min y-max) 2)
                        (/ (+ z-min z-max) 2)])
            "Center position should match")))))

(deftest generate-chair-mesh-test
  (testing "generate-chair-mesh returns correct structure"
    (let [mesh (generate-chair-mesh)]
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
        ; Chair has 6 components (seat, backrest, 4 legs)
        (is (= (* 8 6) vertex-count) "Should have correct number of vertices")
        (is (= (* 12 6) face-count) "Should have correct number of faces"))
      
      ; Test chair dimensions and component positions
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
        (is (> (- x-max x-min) 0) "Chair should have positive width")
        (is (> (- y-max y-min) 0) "Chair should have positive height")
        (is (> (- z-max z-min) 0) "Chair should have positive depth")
        
        ; Test relative positions
        (is (> y-max 2.0) "Chair should be tall enough for backrest")
        (is (< y-min 1.0) "Chair should extend down for legs")
        (is (> (- x-max x-min) 1.0) "Chair should be wide enough for seat")
        (is (> (- z-max z-min) 1.0) "Chair should be deep enough for seat")))))
(deftest json-serialization-test
  (testing "mesh data can be serialized to JSON"
    (let [mesh (tetrahedron-mesh)
          json-str (json/write-str mesh)
          parsed (json/read-str json-str)]
      (is (string? json-str) "JSON serialization should produce a string")
      (is (map? parsed) "Parsed JSON should be a map")
      (is (= (count (:vertices mesh)) (count (get parsed "vertices")))
          "Vertices count should match after serialization/deserialization")
      (is (= (count (:faces mesh)) (count (get parsed "faces")))
          "Faces count should match after serialization/deserialization")))
  
  (testing "chair mesh data can be serialized to JSON"
    (let [mesh (generate-chair-mesh)
          json-str (json/write-str mesh)
          parsed (json/read-str json-str)]
      (is (string? json-str) "JSON serialization should produce a string")
      (is (map? parsed) "Parsed JSON should be a map")
      (is (= (count (:vertices mesh)) (count (get parsed "vertices")))
          "Vertices count should match after serialization/deserialization")
      (is (= (count (:faces mesh)) (count (get parsed "faces")))
          "Faces count should match after serialization/deserialization"))))





