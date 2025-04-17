(ns meshifier.render-test
  (:require [clojure.test :refer :all]
            [meshifier.render :as render]
            [meshifier.core :as core]
            [meshifier.png-vision :refer [single-color?]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn ensure-test-output-dir
  "Ensures the test output directory exists"
  []
  (let [dir (io/file "test/resources/output")]
    (when-not (.exists dir)
      (.mkdirs dir))))

(deftest ensure-directory-test
  (testing "ensure-directory creates directory"
    (ensure-test-output-dir)
    (let [test-dir "test/resources/output/test-dir"]
      ; Clean up any existing directory before test
      (when (.exists (io/file test-dir))
        (io/delete-file test-dir))
      
      (println "Creating test directory:" test-dir)
      ; Test directory creation
      (render/ensure-directory test-dir)
      (is (.exists (io/file test-dir))))))

(deftest get-script-path-test
  (testing "get-script-path returns a valid path"
    (let [script-path (render/get-script-path)]
      (println "Script path:" script-path)
      (is (string? script-path))
      (is (.contains script-path "render_mesh.py")))))

(deftest render-mesh-test
  (testing "render-mesh with invalid data returns error"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/test-render"
          _ (println "Testing render with output path:" output-path)
          result (render/render-mesh "invalid json" output-path)]
      (is (false? (:success result)))
      (is (string? (:message result))))))

(deftest render-tetrahedron-test
  (testing "rendered tetrahedron should not be a single color"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/tetrahedron-render"
          mesh (core/tetrahedron-mesh)
          mesh-json (json/write-str mesh)
          render-result (render/render-mesh mesh-json output-path)
          output-file (str output-path "_00.png")]
      
      ; Verify render was successful
      (is (:success render-result) 
          (str "Render failed: " (:message render-result)))
      
      ; Verify output file exists
      (is (.exists (io/file output-file))
          "Render output file should exist")
      
      ; Verify the rendered image is not a single color
      (is (not (single-color? output-file))
          "Rendered tetrahedron should not be a single color"))))

