(ns meshifier.core-test
  (:require [clojure.test :refer :all]
            [meshifier.core :refer :all]))

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




