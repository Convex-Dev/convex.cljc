(ns convex.ref

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Hash
                             Ref
                             RefDirect
                             RefSoft))
  (:refer-clojure :exclude [resolve]))


;;;;;;;;;; Creating refs


(defn create-direct

  ""

  ^RefDirect

  [^ACell cell]

  (RefDirect/create cell))



(defn create-soft

  ""

  ^Ref

  [^Hash hash]

  (RefSoft/createForHash hash))


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
