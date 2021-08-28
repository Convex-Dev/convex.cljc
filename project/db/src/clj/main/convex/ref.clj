(ns convex.ref

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Ref
                             RefDirect))
  (:refer-clojure :exclude [resolve]))


;;;;;;;;;; Lifecycle


(defn create

  ""

  ^Ref

  [^ACell cell]

  (Ref/get cell))


;;;;;;;;;; Predicates


(defn direct?

  ""

  [^Ref ref]

  (.isDirect ref))



(defn embedded?

  ""

  [^Ref ref]

  (.isEmbedded ref))



(defn missing?

  ""

  [^Ref ref]

  (.isMissing ref))



(defn persisted?

  ""

  [^Ref ref]

  (.isPersisted ref))


;;;;;;;;;; Potential reads


(defn direct

  ""

  ^RefDirect

  [^Ref ref]

  (.toDirect ref))



(defn resolve

  ""

  ^ACell

  [^Ref ref]

  (.getValue ref))
