(ns meshifier.core
  (:gen-class))

(defn normalize-vector [v]
  (let [magnitude (Math/sqrt (reduce + (map #(* % %) v)))]
    (if (zero? magnitude)
      v
      (mapv #(/ % magnitude) v))))

(defn cross-product [[x1 y1 z1] [x2 y2 z2]]
  [(- (* y1 z2) (* z1 y2))
   (- (* z1 x2) (* x1 z2))
   (- (* x1 y2) (* y1 x2))])

(defn subtract-vectors [v1 v2]
  (mapv - v1 v2))

(defn calculate-face-normal [vertices face-indices]
  (let [[i1 i2 i3] face-indices
        v1 (nth vertices i1)
        v2 (nth vertices i2)
        v3 (nth vertices i3)
        edge1 (subtract-vectors v2 v1)
        edge2 (subtract-vectors v3 v1)]
    (normalize-vector (cross-product edge1 edge2))))

(defn custom-tetrahedron-mesh
  "Returns mesh data for a tetrahedron in a format compatible with Blender's Mesh.from_pydata().
   Takes four 3D points as input to form the tetrahedron vertices.
   Returns a map containing :vertices, :faces, and :normals where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face
   - :normals is a sequence of [x y z] normal vectors for each vertex"
  [p1 p2 p3 p4]
  (let [vertices [p1 p2 p3 p4]
        faces [[0 1 2]              ; face 0
               [0 1 3]              ; face 1
               [0 2 3]              ; face 2
               [1 2 3]]             ; face 3
        face-normals (mapv #(calculate-face-normal vertices %) faces)
        ; Calculate vertex normals by averaging the normals of faces that share each vertex
        vertex-normals (vec (for [vertex-idx (range (count vertices))]
                             (let [connected-faces (keep-indexed #(when ((set %2) vertex-idx) %1) faces)
                                   connected-normals (mapv face-normals connected-faces)]
                               (normalize-vector (reduce #(mapv + %1 %2) [0 0 0] connected-normals)))))]
    {:vertices vertices
     :faces faces
     :normals vertex-normals}))

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
   Returns a map containing :vertices, :faces, and :normals where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face
   - :normals is a sequence of [x y z] normal vectors for each vertex"
  []
  (custom-tetrahedron-mesh [1.0 1.0 1.0]     ; vertex 0
                          [-1.0 -1.0 1.0]    ; vertex 1
                          [-1.0 1.0 -1.0]    ; vertex 2
                          [1.0 -1.0 -1.0]))  ; vertex 3


