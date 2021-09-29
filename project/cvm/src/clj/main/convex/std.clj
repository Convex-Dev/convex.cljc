(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             ACountable
                             ADataStructure
                             ASequence)
           (convex.core.lang RT))
  (:refer-clojure :exclude [assoc
                            concat
                            conj
                            cons
                            contains?
                            count
                            empty
                            empty?
                            get
                            next
                            nth
                            reverse]))


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
