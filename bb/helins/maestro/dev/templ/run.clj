(ns helins.maestro.dev.templ.run
  
  ""

  {:author "Adam Helinski"}

  (:require [babashka.fs              :as bb.fs]
            [helins.maestro.dev.templ :as $.dev.templ]))


;;;;;;;;;;


(defn -root

  ""

  []

  (let [path (first *command-line-args*)]
    (when (nil? path)
      (throw (ex-info "Root directory containing dev source must be provided as argument"
                      {})))
    (when-not (bb.fs/directory? path)
      (throw (ex-info "Given path is not a directory"
                      {::root path})))
    path))


;;;;;;;;;;

(defn copy

  ""

  []

  ($.dev.templ/copy (-root)))



(defn rm

  ""

  []

  ($.dev.templ/rm (-root)))
