(ns meshifier.png-vision-test
  (:require [clojure.test :refer :all]
            [meshifier.png-vision :refer [single-color?]]
            [clojure.java.io :as io])
  (:import [javax.imageio ImageIO]
           [java.awt.image BufferedImage]
           [java.awt Color]
           [java.io File]))

(defn ensure-test-output-dir
  "Ensures the test output directory exists"
  []
  (let [dir (io/file "test/resources/output")]
    (when-not (.exists dir)
      (.mkdirs dir))))
(defn create-test-image
  "Creates a test PNG image with specified dimensions and colors"
  [width height color-fn filename]
  (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        graphics (.createGraphics image)]
    (dotimes [x width]
      (dotimes [y height]
        (let [color (color-fn x y)]
          (.setColor graphics color)
          (.fillRect graphics x y 1 1))))
    (.dispose graphics)
    (ImageIO/write image "PNG" (File. filename))))

(defn cleanup-test-files
  "Removes test PNG files"
  [& filenames]
  (doseq [filename filenames]
    (let [file (File. filename)]
      (when (.exists file)
        (.delete file)))))

(deftest single-color-test
  (let [single-color-file "test/resources/output/test-single-color.png"
        multi-color-file "test/resources/output/test-multi-color.png"]
    
    ; Ensure test output directory exists
    (ensure-test-output-dir)
    
    ; Clean up any existing test files before running tests
    (cleanup-test-files single-color-file multi-color-file)
    
    (testing "Single color image returns true"
      (println "Creating test file:" (.getAbsolutePath (File. single-color-file)))
      (create-test-image 10 10 (fn [_ _] (Color. 255 0 0)) single-color-file)
      (is (true? (single-color? single-color-file))))
    
    (testing "Multiple color image returns false"
      (println "Creating test file:" (.getAbsolutePath (File. multi-color-file)))
      (create-test-image 10 10 
                        (fn [x y] 
                          (if (< x 5)
                            (Color. 255 0 0)
                            (Color. 0 255 0)))
                        multi-color-file)
      (is (false? (single-color? multi-color-file))))
    
    (testing "Non-existent file throws exception"
      (is (thrown? java.io.IOException
                   (single-color? "non-existent.png"))))))




