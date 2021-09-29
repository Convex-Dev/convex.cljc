(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACountable
                             ADataStructure))
  (:refer-clojure :exclude [count
                            empty?
                            nth]))


;;;;;;;;;;


(defn count

  ""

  [^ACountable countable]

  (.count countable))



(defn empty?

  ""

  [^ACountable countable]

  (if (nil? countable)
    true
    (.isEmpty countable)))



(defn nth

  ""

  [^ACountable countable index]

  (.get countable
        index))



(defn nth-ref

  ""

  [^ACountable countable index]

  (.getElementRef countable
                  index))
