(ns meshifier.core-test
  (:require [clojure.test :refer :all]
            [meshifier.core :refer :all]))

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


