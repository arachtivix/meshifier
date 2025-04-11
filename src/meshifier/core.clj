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

(defn tetrahedron-mesh
  "Returns mesh data for a tetrahedron in a format compatible with Blender's Mesh.from_pydata().
   Returns a map containing :vertices, :faces, and :normals where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face
   - :normals is a sequence of [x y z] normal vectors for each vertex"
  []
  (let [vertices [[1.0 1.0 1.0]     ; vertex 0
                  [-1.0 -1.0 1.0]    ; vertex 1
                  [-1.0 1.0 -1.0]    ; vertex 2
                  [1.0 -1.0 -1.0]]   ; vertex 3
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


