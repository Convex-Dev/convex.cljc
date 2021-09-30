(ns convex.std

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data AccountKey
                             Address
                             ABlob
                             ABlobMap
                             ACell
                             ACountable
                             ADataStructure
                             AHashMap
                             AHashSet
                             AList
                             AMap
                             ASequence
                             ASet
                             AString
                             AVector
                             INumeric
                             Keyword
                             Ref
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
                            coll?
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
                            hash-map
                            hash-set
                            inc
                            into
                            keys
                            keyword
                            keyword?
                            list
                            list?
                            long
                            map?
                            merge
                            mod
                            name
                            next
                            nth
                            number?
                            reverse
                            set
                            set?
                            str
                            string?
                            symbol
                            symbol?
                            vals
                            vec
                            vector
                            vector?
                            zero?])
  (:require [convex.cell :as $.cell]))


(set! *warn-on-reflection*
      true)


(declare conj
         name)


;;;;;;;;;; Private


(defn- -ensure-numeric

  ;;

  ^INumeric

  [x]

  (if (some? x)
    x
    (throw (IllegalArgumentException. "Argument must be numeric"))))


;;;;;;;;;; Casts


(defn account-key

  ""

  ^AccountKey

  [^ACell x]

  (RT/castAccountKey x))



(defn address

  ""

  ^Address

  [^ACell x]

  (RT/castAddress x))



(defn blob

  ""

  ^ABlob

  [^ACell x]

  (RT/castBlob x))



(defn byte

  ""

  ^CVMByte

  [^ACell x]

  (RT/castByte x))



(defn double

  ""

  ^CVMDouble

  [^ACell x]

  (RT/castDouble x))



(defn keyword

  ""

  ^Keyword

  [^ACell x]

  (RT/castKeyword x))



(defn long

  ""

  ^Long

  [^ACell x]

  (RT/castLong x))



(defn set

  ""

  ^ASet

  [^ACell x]

  (RT/castSet x))



(defn str

  ""

  ^AString

  [& xs]

  (RT/str ^"[Lconvex.core.data.ACell;" (into-array ACell
                                                   xs)))



(defn symbol

  ""

  ^Symbol

  [^ACell x]

  (when-some [nm (name x)]
    ($.cell/symbol (clojure.core/str nm))))



(defn vec

  ""

  ^AVector

  [^ACell x]

  (RT/castVector x))


;;;;;;;;;; Collection constructors


(defn blob-map

  ""

  ^ABlobMap

  [& kvs]

  (if kvs
    (do
      (when-not (even? (clojure.core/count kvs))
        (throw (IllegalArgumentException. "Must provide an even number of arguments")))
      ($.cell/blob-map (partition 2
                                  kvs)))
    ($.cell/blob-map)))



(defn hash-map

  ""

  ^AHashMap

  [& kvs]

  (if kvs
    (do
      (when-not (even? (clojure.core/count kvs))
        (throw (IllegalArgumentException. "Must provide an even number of arguments")))
      ($.cell/map (partition 2
                             kvs)))
    ($.cell/map)))



(defn hash-set

  ""

  ^AHashSet

  [& kvs]

  (if kvs
    ($.cell/set kvs)
    ($.cell/set)))



(defn list

  ""

  ^AList

  [& xs]

  (if xs
    ($.cell/list xs)
    ($.cell/list)))



(defn vector

  ""

  ^AVector

  [& xs]

  (if xs
    ($.cell/vector xs)
    ($.cell/vector)))


;;;;;;;;;; Collection generics


(defn into

  ""


  (^ADataStructure [to from]

   (reduce conj
           to
           from))


  (^ADataStructure [to xform from]

   (transduce xform
              conj
              to
              from)))


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

  ^ACell

  [^ACountable countable index]

  (.get countable
        index))



(defn nth-ref

  ""

  ^Ref

  [^ACountable countable index]

  (.getElementRef countable
                  index))


;;;;;;;;;; Data structure


(defn assoc

  ""

  ^ADataStructure

  [^ADataStructure coll k v]

  (.assoc coll
          k
          v))



(defn conj

  ""


  (^AVector []

   ($.cell/vector))


  (^ADataStructure [coll]

   coll)


  (^ADataStructure [^ADataStructure coll v]

   (.conj coll
          v)))



(defn contains?

  ""

  [^ADataStructure coll k]

  (.containsKey coll
                k))



(defn empty

  ""

  ^ADataStructure

  [^ADataStructure coll]

  (.empty coll))



(defn get

  ""

  (^ACell [^ADataStructure coll ^ACell k]

   (.get coll
         k))


  (^ACell [^ADataStructure coll ^ACell k not-found]

   (.get coll
         k
         not-found)))


;;;;;;;;;; Long


(defn dec

  ""

  ^CVMLong

  [^CVMLong long]

  ($.cell/long (clojure.core/dec (.longValue long))))



(defn mod

  ""

  ^CVMLong

  [a b]

  (-ensure-numeric (RT/mod a
                           b)))



(defn inc

  ""

  ^CVMLong

  [^CVMLong long]

  ($.cell/long (clojure.core/inc (.longValue long))))


;;;;;;;;;; Map


(defn dissoc

  ""

  ^AMap

  [^AMap map k]

  (if map
    (.dissoc map
             k)
    ($.cell/map)))



(defn find

  ""

  ^AMap

  [^AMap map k]

  (when map
    (.getEntry map
               k)))



(defn keys

  ""

  ^AVector

  [^ACell map]

  (or (RT/keys map)
      (throw (IllegalArgumentException. "Must be a map"))))



(defn merge

  ""

  ^AHashMap

  [^AHashMap map-1 ^AHashMap map-2]

  (cond
    (nil? map-1) (or map-2
                     ($.cell/map))
    (nil? map-2) map-1
    :else        (.merge map-1
                         map-2)))



(defn vals

  ""

  ^AVector

  [^AMap map]

  (.values map))


;;;;;;;;;; Math


(defn +

  ""

  ^INumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/plus
      -ensure-numeric))



(defn -

  ""

  ^INumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/minus
      -ensure-numeric))



(defn *

  ""

  ^INumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/times
      -ensure-numeric))



(defn abs

  ""

  ^INumeric

  [x]

  (-ensure-numeric (RT/abs x)))



(defn ceil

  ""

  ^CVMDouble

  [x]

  (-ensure-numeric (RT/ceil x)))



(defn div

  ""

  ^INumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/divide
      -ensure-numeric))



(defn exp

  ""

  ^CVMDouble

  [x]

  (-ensure-numeric (RT/exp x)))



(defn floor

  ""

  ^CVMDouble

  [x]

  (-ensure-numeric (RT/floor x)))



(defn nan?

  ""

  [^ACell x]

  (RT/isNaN x))



(defn pow

  ""

  ^CVMDouble

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

  ^INumeric

  [^ACell x]

  (-ensure-numeric (RT/signum x)))




(defn sqrt

  ""

  ^CVMDouble

  [^ACell x]

  (-ensure-numeric (RT/sqrt x)))



(defn zero?

  ""

  [^ACell x]

  (if-some [^INumeric n (RT/ensureNumber x)]
    (= (.doubleValue n)
       0.0)
    false))


;;;;;;;;;; Sequence


(defn cons

  ""

  ^AList

  [x ^ACell coll]

  (RT/cons x
           (when (some? coll)
             (or (RT/sequence coll)
                 coll))))



(defn concat

  ""

  ^ASequence

  [^ACell x ^ACell y]

  (RT/concat (when (some? x)
               (or (RT/sequence x)
                   x))
             (when (some? y)
               (or (RT/sequence y)
                   y))))



(defn next

  ""

  ^ASequence

  [^ACell coll]

  (when coll
    (.next (or ^ASequence (RT/sequence coll)
               ^ASequence coll))))



(defn reverse

  ""

  ^ASequence

  [^ASequence sq]

  (when sq
    (.reverse sq)))


;;;;;;;;;; Set


(defn difference

  ""

  ^ASet

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) ($.cell/set)
    (nil? set-2) set-1
    :else        (.excludeAll set-1
                              set-2)))



(defn intersection

  ""

  ^ASet

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

  ^ASet

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

  ^AString

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



(defn coll?

  "Is `x` a collection?"

  [x]

  (instance? ADataStructure
             x))


(defn cvm-value?

  "Is `x` a CVM value?

   Returns false if `x` is not accessible to the CVM and meant to be used outside (eg. networking)."

  [^ACell x]

  (.isCVMValue x))



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



(defn number?

  "Is `x` a CVM number?"

  [x]

  (RT/isNumber x))



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
