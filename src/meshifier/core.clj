(ns meshifier.core
  (:gen-class))

(defn tetrahedron-mesh
  "Returns mesh data for a tetrahedron in a format compatible with Blender's Mesh.from_pydata().
   Returns a map containing :vertices and :faces where:
   - :vertices is a sequence of [x y z] coordinates for each vertex
   - :faces is a sequence of vertex indices forming each triangular face"
  []
  (let [vertices [[1.0 1.0 1.0]     ; vertex 0
                  [-1.0 -1.0 1.0]    ; vertex 1
                  [-1.0 1.0 -1.0]    ; vertex 2
                  [1.0 -1.0 -1.0]]   ; vertex 3
        faces [[0 1 2]              ; face 0
               [0 1 3]              ; face 1
               [0 2 3]              ; face 2
               [1 2 3]]]            ; face 3
    {:vertices vertices
     :faces faces}))


