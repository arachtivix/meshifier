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

(defn -main [& args]
  (let [mesh-data (tetrahedron-mesh)
        mesh-json (json/write-str mesh-data)
        output-prefix (if (seq args)
                       (first args)
                       "output/render")]
    (if (= output-prefix "-")
      ; If output is "-", just print to stdout (original behavior)
      (println mesh-json)
      ; Otherwise render the mesh
      (let [result (render/render-mesh mesh-json output-prefix)]
        (if (:success result)
          (println (:message result))
          (do
            (println "Error:" (:message result))
            (System/exit 1)))))))

