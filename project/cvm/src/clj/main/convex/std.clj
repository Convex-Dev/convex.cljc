(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             ACountable
                             ADataStructure))
  (:refer-clojure :exclude [assoc
                            conj
                            contains?
                            count
                            empty
                            empty?
                            get
                            nth]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Countable


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


;;;;;;;;;; Data structures


(defn assoc

  ""

  [^ADataStructure coll k v]

  (.assoc coll
          k
          v))



(defn conj

  ""

  [^ADataStructure coll v]

  (.conj coll
         v))



(defn contains?

  ""

  [^ADataStructure coll k]

  (.containsKey coll
                k))



(defn empty

  ""

  [^ADataStructure coll]

  (.empty coll))



(defn get

  ""

  ([^ADataStructure coll ^ACell k]

   (.get coll
         k))


  ([^ADataStructure coll ^ACell k not-found]

   (.get coll
         k
         not-found)))
