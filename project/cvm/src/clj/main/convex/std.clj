(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             ACountable
                             ADataStructure
                             AHashMap
                             AMap
                             ASequence)
           (convex.core.lang RT))
  (:refer-clojure :exclude [assoc
                            concat
                            conj
                            cons
                            contains?
                            count
                            dissoc
                            empty
                            empty?
                            find
                            get
                            keys
                            merge
                            next
                            nth
                            reverse
                            vals])
  (:require [convex.cell :as $.cell]))


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


;;;;;;;;;; Data structure


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


;;;;;;;;;; Map


(defn dissoc

  ""

  [^AMap map k]

  (if map
    (.dissoc map
             k)
    ($.cell/map)))



(defn find

  ""

  [^AMap map k]

  (when map
    (.getEntry map
               k)))



(defn keys

  ""

  [^ACell map]

  (or (RT/keys map)
      (throw (IllegalArgumentException. "Must be a map"))))



(defn merge

  ""

  [^AHashMap map-1 ^AHashMap map-2]

  (cond
    (nil? map-1) (or map-2
                     ($.cell/map))
    (nil? map-2) map-1
    :else        (.merge map-1
                         map-2)))



(defn vals

  ""

  [^AMap map]

  (.values map))


;;;;;;;;;; Sequence


(defn cons

  ""

  [x ^ACell coll]

  (RT/cons x
           (when (some? coll)
             (or (RT/sequence coll)
                 coll))))



(defn concat

  ""

  [^ACell x ^ACell y]

  (RT/concat (when (some? x)
               (or (RT/sequence x)
                   x))
             (when (some? y)
               (or (RT/sequence y)
                   y))))



(defn next

  ""

  [^ACell coll]

  (when coll
    (.next (or ^ASequence (RT/sequence coll)
               ^ASequence coll))))



(defn reverse

  ""

  [^ASequence sq]

  (when sq
    (.reverse sq)))
