(ns meshifier.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [meshifier.render :as render]
            [meshifier.mesh-utils :refer [generate-cuboid-mesh custom-tetrahedron-mesh point-in-tetrahedron?]]
            [meshifier.core-spaceship :refer [generate-spaceship-mesh]]))





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



(defn generate-cat-mesh
  "Returns mesh data for a stylized cat.
   Returns a map containing :vertices and :faces for the complete cat model."
  []
  (let [; Define cat dimensions
        ; Body
        body-width 1.5
        body-height 1.0
        body-depth 2.5
        body-pos [0 1.0 0]
        
        ; Head
        head-width 1.0
        head-height 1.0
        head-depth 1.0
        head-pos [0 1.5 1.5]
        
        ; Ears (triangular prisms approximated with small cuboids)
        ear-width 0.2
        ear-height 0.4
        ear-depth 0.2
        ear-spacing 0.4
        left-ear-pos [(- ear-spacing) 2.2 1.5]
        right-ear-pos [ear-spacing 2.2 1.5]
        
        ; Tail
        tail-width 0.2
        tail-height 0.2
        tail-depth 1.5
        tail-pos [0 1.5 -1.5]
        
        ; Legs
        leg-width 0.2
        leg-height 0.8
        leg-depth 0.2
        leg-offset-x 0.5
        leg-offset-z 0.8
        leg-pos-y 0.4
        
        ; Generate components
        body (generate-cuboid-mesh body-width body-height body-depth body-pos)
        head (generate-cuboid-mesh head-width head-height head-depth head-pos)
        left-ear (generate-cuboid-mesh ear-width ear-height ear-depth left-ear-pos)
        right-ear (generate-cuboid-mesh ear-width ear-height ear-depth right-ear-pos)
        tail (generate-cuboid-mesh tail-width tail-height tail-depth tail-pos)
        front-left-leg (generate-cuboid-mesh leg-width leg-height leg-depth [(- leg-offset-x) leg-pos-y leg-offset-z])
        front-right-leg (generate-cuboid-mesh leg-width leg-height leg-depth [leg-offset-x leg-pos-y leg-offset-z])
        back-left-leg (generate-cuboid-mesh leg-width leg-height leg-depth [(- leg-offset-x) leg-pos-y (- leg-offset-z)])
        back-right-leg (generate-cuboid-mesh leg-width leg-height leg-depth [leg-offset-x leg-pos-y (- leg-offset-z)])
        
        ; Combine all vertices
        all-vertices (vec (concat (:vertices body)
                                (:vertices head)
                                (:vertices left-ear)
                                (:vertices right-ear)
                                (:vertices tail)
                                (:vertices front-left-leg)
                                (:vertices front-right-leg)
                                (:vertices back-left-leg)
                                (:vertices back-right-leg)))
        
        ; Calculate vertex count offsets
        body-vertex-count (count (:vertices body))
        head-vertex-count (count (:vertices head))
        ear-vertex-count (count (:vertices left-ear))
        tail-vertex-count (count (:vertices tail))
        leg-vertex-count (count (:vertices front-left-leg))
        
        head-offset body-vertex-count
        left-ear-offset (+ head-offset head-vertex-count)
        right-ear-offset (+ left-ear-offset ear-vertex-count)
        tail-offset (+ right-ear-offset ear-vertex-count)
        front-left-leg-offset (+ tail-offset tail-vertex-count)
        front-right-leg-offset (+ front-left-leg-offset leg-vertex-count)
        back-left-leg-offset (+ front-right-leg-offset leg-vertex-count)
        back-right-leg-offset (+ back-left-leg-offset leg-vertex-count)
        
        offset-faces (fn [faces offset]
                      (map #(mapv (fn [idx] (+ idx offset)) %) faces))
        
        ; Combine all faces with proper offsets
        all-faces (vec (concat (:faces body)
                             (offset-faces (:faces head) head-offset)
                             (offset-faces (:faces left-ear) left-ear-offset)
                             (offset-faces (:faces right-ear) right-ear-offset)
                             (offset-faces (:faces tail) tail-offset)
                             (offset-faces (:faces front-left-leg) front-left-leg-offset)
                             (offset-faces (:faces front-right-leg) front-right-leg-offset)
                             (offset-faces (:faces back-left-leg) back-left-leg-offset)
                             (offset-faces (:faces back-right-leg) back-right-leg-offset)))]
    
    {:vertices all-vertices
     :faces all-faces}))

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
            :generator generate-chair-mesh}
   "cat" {:name "Stylized Cat"
          :description "A stylized cat model with body, head, ears, tail, and legs"
          :generator generate-cat-mesh}
   "spaceship" {:name "Space Ship"
                :description "A space ship with main body, wings, cockpit, and propulsion elements"
                :generator generate-spaceship-mesh}})

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














