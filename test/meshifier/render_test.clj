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

(deftest render-joined-tetrahedrons-test
  (testing "rendered joined tetrahedrons should not be a single color"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/joined-tetrahedrons-render"
          mesh (core/joined-tetrahedrons-mesh)
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
          "Rendered joined tetrahedrons should not be a single color"))))

(deftest render-mesh-with-angles-test
  (testing "render-mesh with custom angles"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/angle-test-render"
          mesh (core/tetrahedron-mesh)
          ; Test with just 2 angles: 0 and PI/2
          angles [0 (/ Math/PI 2)]
          mesh-data {:mesh mesh :angles angles}
          render-result (render/render-mesh mesh-data output-path)
          output-file-0 (str output-path "_00.png")
          output-file-1 (str output-path "_01.png")]
      
      ; Verify render was successful
      (is (:success render-result)
          (str "Render failed: " (:message render-result)))
      
      ; Verify both output files exist
      (is (.exists (io/file output-file-0))
          "First render output file should exist")
      (is (.exists (io/file output-file-1))
          "Second render output file should exist")
      
      ; Verify only 2 files were created (not 8)
      (is (not (.exists (io/file (str output-path "_02.png"))))
          "Should only create files for specified angles")))

  (testing "render-mesh with explicit angle parameter"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/explicit-angle-test"
          mesh (core/tetrahedron-mesh)
          angles [(/ Math/PI 4) (* 3 (/ Math/PI 4))]  ; 45° and 135°
          render-result (render/render-mesh mesh output-path :angles angles)
          output-file-0 (str output-path "_00.png")
          output-file-1 (str output-path "_01.png")]
      
      (is (:success render-result))
      (is (.exists (io/file output-file-0)))
      (is (.exists (io/file output-file-1)))
      (is (not (.exists (io/file (str output-path "_02.png")))))))

  (testing "render-mesh with multiple angles"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/multi-angle-test"
          mesh (core/tetrahedron-mesh)
          angles (map #(* % (/ Math/PI 2)) (range 4))  ; [0, 90, 180, 270] degrees
          render-result (render/render-mesh mesh output-path :angles angles)]
      
      (is (:success render-result))
      (doseq [i (range 4)]
        (is (.exists (io/file (format "%s_%02d.png" output-path i)))
            (format "File %02d should exist" i)))
      (is (not (.exists (io/file (str output-path "_04.png"))))
          "Should not create more files than specified angles")
      (is (not (single-color? (str output-path "_00.png")))
          "First angle render should not be single color")
      (is (not (single-color? (str output-path "_01.png")))
          "Second angle render should not be single color")))

  (testing "render-mesh with default angles"
    (ensure-test-output-dir)
    (let [output-path "test/resources/output/default-angle-test"
          mesh (core/tetrahedron-mesh)
          render-result (render/render-mesh mesh output-path)
          output-file-0 (str output-path "_00.png")
          output-file-1 (str output-path "_01.png")]
      
      (is (:success render-result))
      (is (.exists (io/file output-file-0)))
      (is (.exists (io/file output-file-1)))
      (is (not (.exists (io/file (str output-path "_02.png"))))
          "Should only create files for default angles"))))



