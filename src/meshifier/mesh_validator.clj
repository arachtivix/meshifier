(ns meshifier.mesh-validator
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn load-schema
  "Load the JSON schema from resources"
  []
  (-> (io/resource "meshifier/mesh_schema.json")
      slurp
      (json/read-str :key-fn keyword)))

(defn validate-vertex
  "Validate a single vertex"
  [vertex]
  (and (vector? vertex)
       (= 3 (count vertex))
       (every? number? vertex)))

(defn validate-face
  "Validate a single face"
  [face vertex-count]
  (and (vector? face)
       (>= (count face) 3)  ; Face must have at least 3 vertices
       (every? integer? face)
       (every? #(and (>= % 0) (< % vertex-count)) face)))

(defn validate-mesh-structure
  "Validate the mesh structure against our requirements"
  [mesh]
  (let [vertices (:vertices mesh)
        faces (:faces mesh)]
    (cond
      (nil? vertices)
      {:valid false :error "Missing required property: vertices"}
      
      (nil? faces)
      {:valid false :error "Missing required property: faces"}
      
      (not (vector? vertices))
      {:valid false :error "Vertices must be an array"}
      
      (not (vector? faces))
      {:valid false :error "Faces must be an array"}
      
      (not (every? validate-vertex vertices))
      {:valid false :error "Invalid vertex format. Each vertex must be an array of 3 numbers"}
      
      (not (every? #(validate-face % (count vertices)) faces))
      {:valid false :error "Invalid face format. Each face must be an array of at least 3 valid vertex indices"}
      
      :else
      {:valid true})))

(defn validate-mesh
  "Validate a mesh data structure against the schema"
  [mesh]
  (validate-mesh-structure mesh))

(defn validate-mesh-json
  "Validate a JSON string containing mesh data"
  [json-str]
  (try
    (let [mesh (json/read-str json-str :key-fn keyword)]
      (validate-mesh mesh))
    (catch Exception e
      {:valid false :error (str "Invalid JSON: " (.getMessage e))})))

(defn validate-mesh-file
  "Validate a JSON file containing mesh data"
  [file-path]
  (try
    (let [json-str (slurp file-path)]
      (validate-mesh-json json-str))
    (catch java.io.FileNotFoundException e
      {:valid false :error (str "File not found: " file-path)})
    (catch Exception e
      {:valid false :error (str "Error reading file: " (.getMessage e))})))

(defn validate-all-mesh-types
  "Validate all available mesh types in the project.
   Takes a map of mesh generators where each key is a mesh name and value is a function that generates the mesh.
   Returns a map where keys are mesh names and values are validation results."
  [mesh-generators]
  (reduce (fn [results [mesh-name generator-fn]]
            (let [mesh (generator-fn)
                  json-str (json/write-str mesh)
                  validation-result (validate-mesh-json json-str)]
              (assoc results mesh-name validation-result)))
          {}
          mesh-generators))


