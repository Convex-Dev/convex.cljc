(ns convex.lisp.test.schema

  "Creating Malli schemas and using the prepared registry."

  {:author "Adam Helinski"}

  (:require [convex.lisp.schema :as $.schema]
            [malli.core         :as malli]
            [malli.generator    :as malli.gen]))


(declare registry)


;;;;;;;;;; Prepared registry


(defn generator

  "Returns a generator for the given `schema`."

  [schema]

  (malli.gen/generator schema
                       {:registry registry}))



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


;;;;;;;;;; Creating custom schemas


(defn binding+

  "Returns a generator for bindings: vector of `[Symbol Value]`.
  
   Ensures symbols are unique."

  [min-count]

  [:and
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
                      x))))]])



(defn data-without

  "Returns the `:convex/data` schema without the schemas provided in the given set."

  [schema+]

  (into [:or]
        (filter #(not (contains? schema+
                                 %)))
        (rest (registry :convex/data))))
