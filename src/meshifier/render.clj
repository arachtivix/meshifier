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
   - mesh-data: The mesh data as a string (JSON format)
   - output-prefix: The output file prefix (e.g., 'output/render')
   Returns a map with :success and :message keys"
  [mesh-data output-prefix]
  (try
    ; First validate JSON
    (try 
      (json/read-str mesh-data)
      (catch Exception e
        (throw (ex-info "Invalid JSON data provided" {:cause e}))))

    (let [output-dir (str/replace output-prefix #"/[^/]*$" "")
          temp-file (java.io.File/createTempFile "mesh_data" ".json")]
      
      ; Ensure output directory exists
      (ensure-directory output-dir)
      
      ; Write mesh data to temporary file
      (spit temp-file mesh-data)
      
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
           :message (:err result)})))
    (catch Exception e
      {:success false
       :message (if (instance? clojure.lang.ExceptionInfo e)
                 (.getMessage e)
                 (.getMessage e))})))

