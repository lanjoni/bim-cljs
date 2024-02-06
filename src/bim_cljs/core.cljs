(ns bim-cljs.core
  (:require ["three" :as three]
            ["openbim-components" :as obc]))

(def viewer (obc/Components.))
(.add (.-onInitialized viewer) #(prn "Viewer initialized"))

(def sceneComponent (obc/SimpleScene. viewer))
(.setup sceneComponent)
(aset viewer "scene" sceneComponent)

(def viewerContainer (js/document.getElementById "app"))
(def rendererComponent (obc/PostproductionRenderer. viewer viewerContainer))
(aset viewer "renderer" rendererComponent)
  
(def cameraComponent (obc/OrthoPerspectiveCamera. viewer))
(aset viewer "camera" cameraComponent)

(def postproduction (.-postproduction rendererComponent))
(aset postproduction "enabled" true)

(def raycasterComponent (obc/SimpleRaycaster. viewer))
(aset viewer "raycaster" raycasterComponent)
 
(.init viewer)
 
(def grid (obc/SimpleGrid. viewer (three/Color. 0x666666)))
(.push (.-excludedMeshes (.-customEffects postproduction)) (.get grid))
 
(def ifcLoader (obc/FragmentIfcLoader. viewer))
(def highlighter (obc/FragmentHighlighter. viewer))
(def propertiesProcessor (obc/IfcPropertiesProcessor. viewer))
 
(aset ifcLoader "settings.wasm" {:absolute true :path "https://unpkg.com/web-ifc@0.0.44/"})
 
(.setup ifcLoader)
 
(.setup highlighter)
 
(.add (.-onClear (.-select (.-events highlighter))) #(.-cleanPropertiesList propertiesProcessor))
 
(.add (.-onIfcLoaded ifcLoader) (fn [model]
                                  (.process propertiesProcessor model)
                                  (.add (.-onHighlight (.-select (.-events highlighter))) (fn [selection]
                                                                                         (let [fragmentID (first (js-keys selection))
                                                                                               expressID (first (aget selection fragmentID))]
                                                                                           (.renderProperties propertiesProcessor model expressID)
                                                                                           (js/console.log "selection" selection))))
                                  (.update highlighter)))

(def mainToolbar (obc/Toolbar. viewer))
(.addChild mainToolbar (.get (.-uiElement ifcLoader) "main") (.get (.-uiElement propertiesProcessor) "main"))
(.addToolbar (.-ui viewer) mainToolbar)

(defn ^:export init []
  (prn three)
  (prn obc)
  (prn viewer))
