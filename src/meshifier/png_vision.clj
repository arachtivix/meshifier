(ns meshifier.png-vision
  (:import [javax.imageio ImageIO]
           [java.io File]))

(defn single-color?
  "Takes a path to a PNG file and determines if the image consists of a single color throughout.
   Returns true if the image is a single color, false otherwise.
   
   Parameters:
   - file-path: String path to the PNG file to analyze
   
   Throws:
   - java.io.IOException if the file cannot be read
   - IllegalArgumentException if the file is not a valid image"
  [file-path]
  (let [image (ImageIO/read (File. file-path))
        width (.getWidth image)
        height (.getHeight image)
        first-pixel (.getRGB image 0 0)]
    (loop [x 0
           y 0]
      (cond
        ;; If we've checked all pixels, the image is single color
        (and (= x 0) (= y height))
        true
        
        ;; Move to next row when we reach end of current row
        (= x width)
        (recur 0 (inc y))
        
        ;; Check if current pixel matches first pixel
        :else
        (if (= (.getRGB image x y) first-pixel)
          (recur (inc x) y)
          false)))))