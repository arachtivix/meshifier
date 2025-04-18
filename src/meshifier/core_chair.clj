(ns meshifier.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [meshifier.render :as render]))

(defn cross-product [[x1 y1 z1] [x2 y2 z2]]
  [(- (* y1 z2) (* z1 y2))
   (- (* z1 x2) (* x1 z2))
   (- (* x1 y2) (* y1 x2))])

(defn subtract-vectors [v1 v2]
  (mapv - v1 v2))

(defn generate-cuboid-mesh
  "Returns mesh data for a cuboid with given dimensions and position.
   Parameters:
   - width: Width along x-axis
   - height: Height along y-axis
   - depth: Depth along z-axis
   - position: [x y z] coordinates for the center of the cuboid
   Returns a map containing :vertices and :faces"
  [width height depth [pos-x pos-y pos-z]]
  (let [w2 (/ width 2)
        h2 (/ height 2)
        d2 (/ depth 2)
        vertices [[(- pos-x w2) (- pos-y h2) (- pos-z d2)]  ; 0: front bottom left
                 [(+ pos-x w2) (- pos-y h2) (- pos-z d2)]  ; 1: front bottom right
                 [(+ pos-x w2) (+ pos-y h2) (- pos-z d2)]  ; 2: front top right
                 [(- pos-x w2) (+ pos-y h2) (- pos-z d2)]  ; 3: front top left
                 [(- pos-x w2) (- pos-y h2) (+ pos-z d2)]  ; 4: back bottom left
                 [(+ pos-x w2) (- pos-y h2) (+ pos-z d2)]  ; 5: back bottom right
                 [(+ pos-x w2) (+ pos-y h2) (+ pos-z d2)]  ; 6: back top right
                 [(- pos-x w2) (+ pos-y h2) (+ pos-z d2)]] ; 7: back top left
        faces [[0 1 2] [0 2 3]  ; front face
               [1 5 6] [1 6 2]  ; right face
               [5 4 7] [5 7 6]  ; back face
               [4 0 3] [4 3 7]  ; left face
               [3 2 6] [3 6 7]  ; top face
               [0 4 5] [0 5 1]]] ; bottom face
    {:vertices vertices
     :faces faces}))

(defn generate-chair-mesh
  "Returns mesh data for a simple chair.
   Returns a map containing :vertices and :faces for the complete chair."
  []
  (let [; Define chair dimensions
        seat-width 2.0
        seat-height 0.3
        seat-depth 2.0
        seat-pos [0 1.5 0]
        
        backrest-width 2.0
        backrest-height 2.0
        backrest-depth 0.3
        backrest-pos [0 2.5 -0.85]
        
        leg-width 0.2
        leg-height 1.5
        leg-depth 0.2
        leg-offset-x 0.8
        leg-offset-z 0.8
        leg-pos-y 0.75
        
        ; Generate components
        seat (generate-cuboid-mesh seat-width seat-height seat-depth seat-pos)
        backrest (generate-cuboid-mesh backrest-width backrest-height backrest-depth backrest-pos)
        front-left-leg (generate-cuboid-mesh leg-width leg-height leg-depth [(- leg-offset-x) leg-pos-y leg-offset-z])
        front-right-leg (generate-cuboid-mesh leg-width leg-height leg-depth [leg-offset-x leg-pos-y leg-offset-z])
        back-left-leg (generate-cuboid-mesh leg-width leg-height leg-depth [(- leg-offset-x) leg-pos-y (- leg-offset-z)])
        back-right-leg (generate-cuboid-mesh leg-width leg-height leg-depth [leg-offset-x leg-pos-y (- leg-offset-z)])
        
        ; Combine all vertices
        all-vertices (vec (concat (:vertices seat)
                                (:vertices backrest)
                                (:vertices front-left-leg)
                                (:vertices front-right-leg)
                                (:vertices back-left-leg)
                                (:vertices back-right-leg)))
        
        ; Offset faces for each component
        seat-vertex-count (count (:vertices seat))
        backrest-vertex-count (count (:vertices backrest))
        leg-vertex-count (count (:vertices front-left-leg))
        
        backrest-offset seat-vertex-count
        front-left-leg-offset (+ backrest-offset backrest-vertex-count)
        front-right-leg-offset (+ front-left-leg-offset leg-vertex-count)
        back-left-leg-offset (+ front-right-leg-offset leg-vertex-count)
        back-right-leg-offset (+ back-left-leg-offset leg-vertex-count)
        
        offset-faces (fn [faces offset]
                      (map #(mapv (fn [idx] (+ idx offset)) %) faces))
        
        ; Combine all faces with proper offsets
        all-faces (vec (concat (:faces seat)
                             (offset-faces (:faces backrest) backrest-offset)
                             (offset-faces (:faces front-left-leg) front-left-leg-offset)
                             (offset-faces (:faces front-right-leg) front-right-leg-offset)
                             (offset-faces (:faces back-left-leg) back-left-leg-offset)
                             (offset-faces (:faces back-right-leg) back-right-leg-offset)))]
    
    {:vertices all-vertices
     :faces all-faces}))

(defn -main [& args]
  (let [mesh-data (generate-chair-mesh)  ; Using chair mesh by default
        mesh-json (json/write-str mesh-data)
        output-prefix (if (seq args)
                       (first args)
                       "output/render")]
    (if (= output-prefix "-")
      ; If output is "-", just print to stdout
      (println mesh-json)
      ; Otherwise render the mesh
      (let [result (render/render-mesh mesh-json output-prefix)]
        (if (:success result)
          (println (:message result))
          (do
            (println "Error:" (:message result))
            (System/exit 1)))))))