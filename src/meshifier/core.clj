(ns meshifier.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [meshifier.render :as render]))



(defn cross-product [[x1 y1 z1] [x2 y2 z2]]
  [(- (* y1 z2) (* z1 y2))
   (- (* z1 x2) (* x1 z2))
   (- (* x1 y2) (* y1 x2))])

(defn subtract-vectors [v1 v2]
  (mapv - v1 v2))



(defn custom-tetrahedron-mesh
  "Returns mesh data for a tetrahedron in a format compatible with Blender's Mesh.from_pydata().
   Takes four 3D points as input to form the tetrahedron vertices.
   Returns a map containing :vertices and :faces where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face"
  [p1 p2 p3 p4]
  (let [vertices [p1 p2 p3 p4]
        faces [[0 1 2]              ; face 0
               [0 1 3]              ; face 1
               [0 2 3]              ; face 2
               [1 2 3]]]            ; face 3
    {:vertices vertices
     :faces faces}))

(defn compute-signed-volume
  "Compute the signed volume of a tetrahedron"
  [p1 p2 p3 p4]
  (let [v1 (subtract-vectors p2 p1)
        v2 (subtract-vectors p3 p1)
        v3 (subtract-vectors p4 p1)
        cross-prod (cross-product v1 v2)
        dot-prod (reduce + (map * cross-prod v3))]
    (/ dot-prod 6.0)))

(defn point-in-tetrahedron?
  "Determines if a point p lies inside the tetrahedron defined by vertices p1, p2, p3, p4.
   Returns true if the point is inside or on the surface of the tetrahedron, false otherwise.
   
   Uses the consistent sign of volumes method:
   - Compute the signed volume of the full tetrahedron
   - Compute the signed volumes of four tetrahedra formed by the test point and three vertices
   - If all five volumes have the same sign (or are zero), the point is inside or on the surface"
  [p p1 p2 p3 p4]
  (let [total-volume (compute-signed-volume p1 p2 p3 p4)
        d1 (compute-signed-volume p p2 p3 p4)
        d2 (compute-signed-volume p1 p p3 p4)
        d3 (compute-signed-volume p1 p2 p p4)
        d4 (compute-signed-volume p1 p2 p3 p)]
    (if (zero? total-volume)
      false  ; Degenerate tetrahedron
      (let [sign (if (pos? total-volume) pos? neg?)]
        (and (or (zero? d1) (sign d1))
             (or (zero? d2) (sign d2))
             (or (zero? d3) (sign d3))
             (or (zero? d4) (sign d4)))))))

(defn tetrahedron-mesh
  "Returns mesh data for a regular tetrahedron with predefined vertices.
   Returns a map containing :vertices and :faces where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face"
  []
  (custom-tetrahedron-mesh [1.0 1.0 1.0]     ; vertex 0
                          [-1.0 -1.0 1.0]    ; vertex 1
                          [-1.0 1.0 -1.0]    ; vertex 2
                          [1.0 -1.0 -1.0]))  ; vertex 3
(defn joined-tetrahedrons-mesh
  "Returns mesh data for two tetrahedrons joined by one face.
   The remaining point of each tetrahedron lies outside of the other tetrahedron.
   Returns a map containing :vertices and :faces."
  []
  (let [; First tetrahedron vertices
        p1 [1.0 1.0 1.0]      ; shared vertex 1
        p2 [-1.0 -1.0 1.0]    ; shared vertex 2
        p3 [-1.0 1.0 -1.0]    ; shared vertex 3
        p4 [1.0 -1.0 -2.0]    ; unique vertex for first tetrahedron
        
        ; Second tetrahedron's unique vertex (placed above the shared face)
        p5 [-0.33 0.33 2.0]   ; unique vertex for second tetrahedron
        
        ; Combined vertices
        vertices [p1 p2 p3 p4 p5]
        
        ; Faces for first tetrahedron (0-based indices)
        faces1 [[0 1 2]    ; shared face
                [0 1 3]    ; unique faces for first tetrahedron
                [0 2 3]
                [1 2 3]]
        
        ; Faces for second tetrahedron
        faces2 [[0 1 4]    ; faces using the second tetrahedron's unique vertex
                [0 2 4]
                [1 2 4]]]
        
    ; Verify that p4 is outside the second tetrahedron
    (assert (not (point-in-tetrahedron? p4 p1 p2 p3 p5))
            "First tetrahedron's unique vertex must be outside second tetrahedron")
    
    ; Verify that p5 is outside the first tetrahedron
    (assert (not (point-in-tetrahedron? p5 p1 p2 p3 p4))
            "Second tetrahedron's unique vertex must be outside first tetrahedron")
    
    {:vertices vertices
     :faces (concat faces1 faces2)}))

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

(def available-shapes
  {"tetrahedron" {:name "Regular Tetrahedron"
                  :description "A regular tetrahedron with four equilateral triangular faces"
                  :generator tetrahedron-mesh}
   "joined-tetrahedrons" {:name "Joined Tetrahedrons"
                         :description "Two tetrahedrons joined by one face"
                         :generator joined-tetrahedrons-mesh}
   "chair" {:name "Simple Chair"
            :description "A simple chair with seat, backrest and four legs"
            :generator generate-chair-mesh}})

(defn list-shapes
  "Display available shapes with their descriptions"
  []
  (println "\nAvailable shapes:")
  (doseq [[id shape-info] available-shapes]
    (println (format "\n%s - %s" id (:name shape-info)))
    (println (format "  %s" (:description shape-info)))))

(defn get-user-shape-choice
  "Get user input for shape selection"
  []
  (println "\nEnter the shape ID to render (or 'q' to quit):")
  (let [input (read-line)]
    (when-not (= input "q")
      (if-let [shape-info (get available-shapes input)]
        shape-info
        (do
          (println "Invalid shape ID. Please try again.")
          (recur))))))

(defn -main [& args]
  (if (and (seq args) (not= (first args) "-i"))
    ; Original direct rendering mode with output path
    (let [mesh-data (generate-chair-mesh)
          mesh-json (json/write-str mesh-data)
          output-prefix (first args)]
      (if (= output-prefix "-")
        ; If output is "-", just print to stdout (original behavior)
        (println mesh-json)
        ; Otherwise render the mesh
        (let [result (render/render-mesh mesh-json output-prefix)]
          (if (:success result)
            (println (:message result))
            (do
              (println "Error:" (:message result))
              (System/exit 1))))))
    
    ; Interactive mode
    (do
      (println "\nMeshifier - 3D Shape Generator")
      (println "============================")
      
      (loop []
        (list-shapes)
        (when-let [shape-info (get-user-shape-choice)]
          (let [mesh-data ((:generator shape-info))
                mesh-json (json/write-str mesh-data)
                output-prefix (str "output/" (str/lower-case (:name shape-info)))
                _ (println "\nRendering" (:name shape-info) "...")
                result (render/render-mesh mesh-json output-prefix)]
            (if (:success result)
              (println (:message result))
              (println "Error:" (:message result)))
            (println "\nPress Enter to continue...")
            (read-line)
            (recur))))
      
      (println "\nGoodbye!"))))





