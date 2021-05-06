(ns convex.lisp.test.util

  ""
  {:author "Adam Helinski"}

  (:require [clojure.core]
            [clojure.string]
            [clojure.test.check.results      :as tc.result]
            [convex.lisp                     :as $]
            [convex.lisp.schema              :as $.schema]
            [malli.core                      :as malli]
            [malli.generator                 :as malli.gen])
  (:refer-clojure :exclude [eval]))


(declare eval-source
         registry
         schema-data-without)


;;;;;;;;;; Registry, fetching generators, and validation


(defn generator

  "Returns a generator for the given `schema`."

  [schema]

  (malli.gen/generator schema
                       {:registry registry}))



(defn generator-binding+

  "Returns a generator for bindings: vector of `[Symbol Value]`.
  
   Ensures symbols are unique."

  [min-count]

  (generator [:and
              [:vector
               (when min-count
                 {:min min-count})
               [:tuple
                :convex/symbol
                :convex/data]]
              [:fn
               (fn [x]
                 (= (count x)
                    (count (into #{}
                                 (map first)
                                 x))))]]))



(defn generator-data-without

  "Mix between [[generator]] and [[schema-data-without]]."

  [schema+]

  (generator (schema-data-without schema+)))



(defn schema-data-without

  "Returns the `:convex/data` schema without the schemas provided in the given set."

  [schema+]

  (into [:or]
        (filter #(not (contains? schema+
                                 %)))
        (rest (registry :convex/data))))



(def registry

  "Malli registry for Convex."

  (-> (malli/default-schemas)
      $.schema/registry))



(defn valid?

  "Is `x` valid according to `schema`?"

  [schema x]

  (malli/validate schema
                  x
                  {:registry registry}))


;;;;;;;;;; Working with generative tests


(defn eq

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply =
         (clojure.core/map hash
              			   arg+)))
