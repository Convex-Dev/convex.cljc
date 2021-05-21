(ns convex.lisp.example.schema

  "Using Malli for validating and generating Convex Lisp."

  (:require [convex.lisp.schema]
            [malli.generator     :as malli.gen]))


;;;;;;;;;;


(def registry

  "Malli registry containing everything that is needed."

  (convex.lisp.schema/registry))



(comment


  (malli.gen/generate :convex/vector
                      {:registry registry
                       :size     5})
  )
