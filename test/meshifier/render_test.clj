(ns meshifier.render-test
  (:require [clojure.test :refer :all]
            [meshifier.render :as render]
            [clojure.java.io :as io]))

(deftest ensure-directory-test
  (testing "ensure-directory creates directory"
    (let [test-dir (str (System/getProperty "java.io.tmpdir") "/test-dir")]
      ; Clean up any existing directory
      (when (.exists (io/file test-dir))
        (io/delete-file test-dir))
      
      ; Test directory creation
      (render/ensure-directory test-dir)
      (is (.exists (io/file test-dir)))
      
      ; Clean up
      (io/delete-file test-dir))))

(deftest get-script-path-test
  (testing "get-script-path returns a valid path"
    (let [script-path (render/get-script-path)]
      (is (string? script-path))
      (is (.contains script-path "render_mesh.py")))))

(deftest render-mesh-test
  (testing "render-mesh with invalid data returns error"
    (let [result (render/render-mesh "invalid json" "test-output")]
      (is (false? (:success result)))
      (is (string? (:message result))))))