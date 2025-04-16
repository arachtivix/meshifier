(ns meshifier.png-vision-test
  (:require [clojure.test :refer :all]
            [meshifier.png-vision :refer [single-color?]])
  (:import [javax.imageio ImageIO]
           [java.awt.image BufferedImage]
           [java.awt Color]
           [java.io File]))

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
  (let [single-color-file "test-single-color.png"
        multi-color-file "test-multi-color.png"]
    
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
