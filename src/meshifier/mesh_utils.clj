(ns meshifier.mesh-utils)

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