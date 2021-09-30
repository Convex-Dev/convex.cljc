(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data Address
                             ABlob
                             ACell
                             ACountable
                             ADataStructure
                             AHashMap
                             AList
                             AMap
                             ASequence
                             ASet
                             AString
                             AVector
                             Keyword
                             Symbol)
           (convex.core.data.prim CVMBool
                                  CVMByte
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (convex.core.lang RT))
  (:refer-clojure :exclude [+
                            -
                            *
                            /
                            <
                            <=
                            ==
                            >=
                            >
                            assoc
                            byte
                            boolean?
                            char?
                            concat
                            conj
                            cons
                            contains?
                            count
                            dec
                            dissoc
                            double
                            double?
                            empty
                            empty?
                            find
                            get
                            inc
                            keys
                            keyword
                            keyword?
                            list?
                            long
                            map?
                            merge
                            mod
                            name
                            next
                            nth
                            reverse
                            set
                            set?
                            string?
                            symbol
                            symbol?
                            vals
                            vec
                            vector?])
  (:require [convex.cell :as $.cell]))


(set! *warn-on-reflection*
      true)


(declare name)


;;;;;;;;;; Private


(defn- -ensure-numeric

  ;;

  [x]

  (if (some? x)
    x
    (throw (IllegalArgumentException. "Argument must be numeric"))))


;;;;;;;;;; Casts


(defn account-key

  ""

  [^ACell x]

  (RT/castAccountKey x))



(defn address

  ""

  [^ACell x]

  (RT/castAddress x))



(defn blob

  ""

  [^ACell x]

  (RT/castBlob x))



(defn byte

  ""

  [^ACell x]

  (RT/castByte x))



(defn double

  ""

  [^ACell x]

  (RT/castDouble x))



(defn keyword

  ""

  [^ACell x]

  (RT/castKeyword x))



(defn long

  ""

  [^ACell x]

  (RT/castLong x))



(defn set

  ""

  [^ACell x]

  (RT/castSet x))



(defn symbol

  ""

  [^ACell x]

  (when-some [nm (name x)]
    ($.cell/symbol (str nm))))



(defn vec

  ""

  [^ACell x]

  (RT/castVector x))


;;;;;;;;;; Comparators


(defn <

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/lt
      -ensure-numeric))



(defn <=

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/le
      -ensure-numeric))



(defn ==

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/eq
      -ensure-numeric))



(defn >=

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/ge
      -ensure-numeric))



(defn >

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/gt
      -ensure-numeric))


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


  ([]

   ($.cell/vector))


  ([coll]

   coll)


  ([^ADataStructure coll v]

   (.conj coll
          v)))



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


;;;;;;;;;; Long


(defn dec

  ""

  [^CVMLong long]

  ($.cell/long (clojure.core/dec (.longValue long))))



(defn mod

  ""

  [a b]

  (-ensure-numeric (RT/mod a
                           b)))



(defn inc

  ""

  [^CVMLong long]

  ($.cell/long (clojure.core/inc (.longValue long))))


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


;;;;;;;;;; Math


(defn +

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/plus
      -ensure-numeric))



(defn -

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/minus
      -ensure-numeric))



(defn *

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/times
      -ensure-numeric))



(defn abs

  ""

  [x]

  (-ensure-numeric (RT/abs x)))



(defn ceil

  ""

  [x]

  (-ensure-numeric (RT/ceil x)))



(defn div

  ""

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/divide
      -ensure-numeric))



(defn exp

  ""

  [x]

  (-ensure-numeric (RT/exp x)))



(defn floor

  ""

  [x]

  (-ensure-numeric (RT/floor x)))



(defn pow

  ""

  [^ACell x ^ACell y]

  (-> (RT/pow (doto ^"[Lconvex.core.data.ACell;" (make-array ACell
                                                             2)
                (aset 0
                      x)
                (aset 1
                      y)))
      -ensure-numeric))



(defn signum

  ""

  [^ACell x]

  (-ensure-numeric (RT/signum x)))




(defn sqrt

  ""

  [^ACell x]

  (-ensure-numeric (RT/sqrt x)))


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


;;;;;;;;;; Set


(defn difference

  ""

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) ($.cell/set)
    (nil? set-2) set-1
    :else        (.excludeAll set-1
                              set-2)))



(defn intersection

  ""

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) ($.cell/set)
    (nil? set-2) ($.cell/set)
    :else        (.intersectAll set-1
                                set-2)))



(defn subset?

  ""

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) true
    (nil? set-2) false
    :else        (.isSubset set-1
                            set-2)))



(defn union

  ""

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) (or set-2
                     ($.cell/set))
    (nil? set-2) set-1
    :else        (.includeAll set-1
                              set-2)))


;;;;;;;;;; Symbolic


(defn name

  ""

  [^ACell symbolic]

  (or (RT/name symbolic)
      (throw (IllegalArgumentException. "Must be symbolic"))))


;;;;;;;;; Type predicates


(defn address?

  "Is `x` an address?"

  [x]

  (instance? Address
             x))



(defn blob?

  "Is `x` a blob?"

  [x]

  (instance? ABlob
             x))



(defn boolean?

  "Is `x` a CVM boolean?"

  [x]

  (instance? CVMBool
             x))



(defn byte?

  "Is `x` a CVM byte?"

  [x]

  (instance? CVMByte
             x))



(defn char?

  "Is `x` a CVM char?"

  [x]

  (instance? CVMChar
             x))



(defn cvm-value?

  "Is `cell` a CVM value?

   Returns false if `x` is not accessible in the CVM and meant to be used outside (eg. networking)."

  [^ACell cell]

  (.isCVMValue cell))



(defn double?

  "Is `x` a CVM double?"

  [x]

  (instance? CVMDouble
             x))



(defn keyword?

  "Is `x` a CVM keyword?"

  [x]

  (instance? Keyword
             x))



(defn list?

  "Is `x` a CVM list?"

  [x]

  (instance? AList
             x))



(defn long?

  "Is `x` a CVM long?"

  [x]

  (instance? CVMLong
             x))



(defn map?

  "Is `x` a CVM map?"

  [x]

  (instance? AMap
             x))



(defn set?

  "Is `x` a CVM set?"

  [x]

  (instance? ASet
             x))



(defn string?

  "Is `x` a CVM string?"

  [x]

  (instance? AString
             x))



(defn symbol?

  "Is `x` a CVM symbol?"

  [x]

  (instance? Symbol
             x))



(defn vector?

  "Is `x` a CVM vector?"

  [x]

  (instance? AVector
             x))
