(ns convex.lisp.test.util

  ""
  {:author "Adam Helinski"}

  (:require [clojure.core]
            [convex.lisp                     :as $]
            [convex.lisp.schema              :as $.schema]
            [malli.core                      :as malli]
            [malli.generator                 :as malli.gen]))


(declare registry)


;;;;;;;;;; Registry and fetching generators


(defn generator

  [k]

  (malli.gen/generator k
                       {:registry registry}))



(def registry
     (-> (malli/default-schemas)
         $.schema/registry))


;;;;;;;;;; Helpers


(defn eq

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply =
         (clojure.core/map hash
              			   arg+)))



(defn source->clojure

  "Reads Convex Lisp source, evals it and converts the result to a Clojure value."

  [source]

  (-> source
      $/read
      $/eval
      $/result
      $/to-clojure))
