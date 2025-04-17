(ns meshifier.render
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.data.json :as json]))

(defn ensure-directory
  "Create directory if it doesn't exist"
  [path]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir))))

(defn get-script-path
  "Get the absolute path of the render_mesh.py script"
  []
  (-> (io/resource "meshifier/render_mesh.py")
      (.getPath)))

(defn render-mesh
  "Render mesh data using Blender.
   Takes:
   - mesh-data: The mesh data as a string (JSON format) or a map with :mesh key
   - output-prefix: The output file prefix (e.g., 'output/render')
   - angles: Optional sequence of angles in radians for rendering views (default: [0 π/2])
   Returns a map with :success and :message keys"
  [mesh-data output-prefix & {:keys [angles]
                             :or {angles [0 (/ Math/PI 2)]}}]
  (try
    ; First validate and prepare JSON
    (let [data (if (string? mesh-data)
                 (try 
                   (json/read-str mesh-data)
                   (catch Exception e
                     (throw (ex-info "Invalid JSON data provided" {:cause e}))))
                 mesh-data)
          ; If data is already a map with :mesh key, use it as is
          ; otherwise treat the entire data as mesh data
          mesh-map (if (:mesh data)
                      data
                      {:mesh data})
          json-data (json/write-str (assoc mesh-map :angles angles))]
      
      (let [output-dir (str/replace output-prefix #"/[^/]*$" "")
            temp-file (java.io.File/createTempFile "mesh_data" ".json")]
        
        ; Ensure output directory exists
        (ensure-directory output-dir)
        
        ; Write mesh data to temporary file
        (spit temp-file json-data)
        
        ; Run Blender with the Python script
        (let [result (shell/sh "blender" "-b" "-P" 
                             (get-script-path) 
                             "--" 
                             (.getAbsolutePath temp-file) 
                             (str output-prefix ".png"))]
          
          ; Clean up temporary file
          (.delete temp-file)
          
          (if (zero? (:exit result))
            {:success true
             :message (format "Renders completed: %s_XX.png (multiple files)" output-prefix)}
            {:success false
             :message (:err result)}))))
    (catch Exception e
      {:success false
       :message (if (instance? clojure.lang.ExceptionInfo e)
                 (.getMessage e)
                 (.getMessage e))})))




