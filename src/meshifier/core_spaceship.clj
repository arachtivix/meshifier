(ns meshifier.core-spaceship
  (:require [meshifier.mesh-utils :refer [generate-cuboid-mesh]]))

(defn generate-spaceship-mesh
  "Returns mesh data for a spaceship with main body, wings, cockpit, and propulsion elements.
   Returns a map containing :vertices and :faces for the complete spaceship model."
  []
  (let [; Define spaceship dimensions
        ; Main body
        body-width 2.0
        body-height 1.0
        body-depth 4.0
        body-pos [0 0 0]
        
        ; Cockpit (front section)
        cockpit-width 1.2
        cockpit-height 0.8
        cockpit-depth 1.0
        cockpit-pos [0 0.4 1.5]
        
        ; Wings
        wing-width 3.0
        wing-height 0.2
        wing-depth 1.5
        left-wing-pos [-1.0 0 -0.5]
        right-wing-pos [1.0 0 -0.5]
        
        ; Engine pods
        engine-width 0.6
        engine-height 0.6
        engine-depth 1.0
        left-engine-pos [-0.8 0 -2.0]
        right-engine-pos [0.8 0 -2.0]
        
        ; Propulsion nozzles
        nozzle-width 0.4
        nozzle-height 0.4
        nozzle-depth 0.5
        left-nozzle-pos [-0.8 0 -2.5]
        right-nozzle-pos [0.8 0 -2.5]
        
        ; Generate components
        main-body (generate-cuboid-mesh body-width body-height body-depth body-pos)
        cockpit (generate-cuboid-mesh cockpit-width cockpit-height cockpit-depth cockpit-pos)
        left-wing (generate-cuboid-mesh wing-width wing-height wing-depth left-wing-pos)
        right-wing (generate-cuboid-mesh wing-width wing-height wing-depth right-wing-pos)
        left-engine (generate-cuboid-mesh engine-width engine-height engine-depth left-engine-pos)
        right-engine (generate-cuboid-mesh engine-width engine-height engine-depth right-engine-pos)
        left-nozzle (generate-cuboid-mesh nozzle-width nozzle-height nozzle-depth left-nozzle-pos)
        right-nozzle (generate-cuboid-mesh nozzle-width nozzle-height nozzle-depth right-nozzle-pos)
        
        ; Combine all vertices
        all-vertices (vec (concat (:vertices main-body)
                                (:vertices cockpit)
                                (:vertices left-wing)
                                (:vertices right-wing)
                                (:vertices left-engine)
                                (:vertices right-engine)
                                (:vertices left-nozzle)
                                (:vertices right-nozzle)))
        
        ; Calculate vertex count offsets
        body-vertex-count (count (:vertices main-body))
        cockpit-vertex-count (count (:vertices cockpit))
        wing-vertex-count (count (:vertices left-wing))
        engine-vertex-count (count (:vertices left-engine))
        nozzle-vertex-count (count (:vertices left-nozzle))
        
        cockpit-offset body-vertex-count
        left-wing-offset (+ cockpit-offset cockpit-vertex-count)
        right-wing-offset (+ left-wing-offset wing-vertex-count)
        left-engine-offset (+ right-wing-offset wing-vertex-count)
        right-engine-offset (+ left-engine-offset engine-vertex-count)
        left-nozzle-offset (+ right-engine-offset engine-vertex-count)
        right-nozzle-offset (+ left-nozzle-offset nozzle-vertex-count)
        
        offset-faces (fn [faces offset]
                      (map #(mapv (fn [idx] (+ idx offset)) %) faces))
        
        ; Combine all faces with proper offsets
        all-faces (vec (concat (:faces main-body)
                             (offset-faces (:faces cockpit) cockpit-offset)
                             (offset-faces (:faces left-wing) left-wing-offset)
                             (offset-faces (:faces right-wing) right-wing-offset)
                             (offset-faces (:faces left-engine) left-engine-offset)
                             (offset-faces (:faces right-engine) right-engine-offset)
                             (offset-faces (:faces left-nozzle) left-nozzle-offset)
                             (offset-faces (:faces right-nozzle) right-nozzle-offset)))]
    
    {:vertices all-vertices
     :faces all-faces}))
