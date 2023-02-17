(ns convex.std

  "Provides an API for cells with classic `convex.core`-like functions.

   All `clojure.core` functions related to sequences usually understand Convex collections, making them
   easy to handle. Some of those (eg. `cons`, `next`) have counterparts in this namespace in case the return
   value must be a cell instead of a Clojure sequence.

   Functions take and return cells unless specified otherwise. Predicates return JVM booleans.

   Sometimes, it can be useful converting cells to Clojure data, such as unwrapping blob to byte arrays,
   which is the purpose of the [[convex.clj]] namespace.

   Lastly, in the rare cases where all of this would not be enough, [Java interop can be used](https://www.javadoc.io/doc/world.convex/convex-core/latest/convex/core/data/package-summary.html);"

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.data AccountKey
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
                             Keyword
                             Refs
                             Refs$RefTreeStats
                             Symbol
                             Syntax)
           (convex.core.data.prim AInteger
                                  ANumeric
                                  CVMBigInteger
                                  CVMBool
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (convex.core.lang IFn
                             RT)
           (convex.core.transactions ATransaction))
  (:refer-clojure :exclude [+
                            -
                            *
                            /
                            <
                            <=
                            ==
                            >=
                            >
                            abs
                            assoc
                            byte
                            boolean?
                            char
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
                            false?
                            fn?
                            find
                            get
                            hash-map
                            hash-set
                            inc
                            integer?
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
                            true?
                            update
                            vals
                            vec
                            vector
                            vector?
                            zero?])
  (:require [convex.cell :as $.cell]
            [convex.clj  :as $.clj]))


(set! *warn-on-reflection*
      true)


(declare conj
         name)


;;;;;;;;;; Private


(defn- -ensure-numeric-success

  ;; Used by functions that takes numeric cells as input.
  ;; Nil means failure to cast arguments.

  [x]

  (if (some? x)
    x
    (throw (IllegalArgumentException. "Argument must be numeric"))))


;;;;;;;;;; Casts


(defn account-key

  "Coerces the given `cell` to an account key or return nil.
  
   Works with:

   - 64-char hex-string cell
   - 32-byte blob"

  ^AccountKey

  [^ACell cell]

  (RT/castAccountKey cell))



(defn address

  "Coerces the given `cell` to an address or return nil.
  
   Works with:

   - Long cell
   - 16-char hex-string cell
   - 8-byte blob"

  ^Address

  [^ACell cell]

  (RT/castAddress cell))



(defn blob

  "Coerces the given `cell` to a blob or return nil.
  
   Works with:

   - Any kind of blob (eg. hash)
   - Long cell
   - Hex-string cell"

  ^ABlob

  [^ACell cell]

  (RT/castBlob cell))



(defn char

  "Coerces the given `cell` to a char or return nil."

  ^CVMChar

  [^ACell cell]

  (cond
    (instance? CVMChar
               cell)
    cell
    ;;
    (instance? CVMLong
               cell)
    (CVMChar/create (.longValue ^CVMLong cell))
    ;;
    :else
    nil))



(defn double

  "Coerces the given `cell` to a double or return nil."

  ^CVMDouble

  [^ACell cell]

  (RT/castDouble cell))



(defn keyword

  "Coerces the given `cell` to a keyword or return nil.
  
   Works with:

   - Max 64-char string cell
   - Symbol"

  ^Keyword

  [^ACell cell]

  (RT/castKeyword cell))



(defn long

  "Coerces the given `cell` to a long or return nil."

  ^Long

  [^ACell cell]

  (RT/castLong cell))



(defn set

  "Coerces the given `cell` to a set or return nil.
  
   Works with any collection."

  ^ASet

  [^ACell cell]

  (RT/castSet cell))



(defn str

  "Stringifies the given cell(s)."

  ^AString

  [& cell+]

  (RT/str ^"[Lconvex.core.data.ACell;" (into-array ACell
                                                   cell+)))



(defn symbol

  "Coerces the given `cell` to a symbol or return nil.

   Works with:

   - Max 64-char string cell
   - Symbol"

  ^Symbol

  [^ACell cell]

  (when-some [nm (name cell)]
    ($.cell/symbol (clojure.core/str nm))))



(defn vec

  "Coerces the given `cell` to a vector or return nil.
  
   Works with any countable (see [[count]])."

  ^AVector

  [^ACell cell]

  (RT/castVector cell))


;;;;;;;;;; Collection constructors


(defn blob-map

  "Builds a blob map from key-values (keys must be blobs)."

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

  "Builds a map from key-values."

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

  "Builds a set from the given cells."

  ^AHashSet

  [& cell+]

  (if cell+
    ($.cell/set cell+)
    ($.cell/set)))



(defn list

  "Buildsa list from the given cells."

  ^AList

  [& cell+]

  (if cell+
    ($.cell/list cell+)
    ($.cell/list)))



(defn vector

  "Builds a vector from the given cells."

  ^AVector

  [& cell+]

  (if cell+
    ($.cell/vector cell+)
    ($.cell/vector)))


;;;;;;;;;; Collection generics


(defn into

  "Like classic `into` but `to` is a collection cell."


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

  "Like classic `<` but with numeric cells."

  [& xs]

  (-> (into-array ACell
                  xs)
      (RT/lt)
      (-ensure-numeric-success)
      ($.clj/boolean)))



(defn <=

  "Like classic `<=` but with numeric cells."

  [& xs]

  (-> (into-array ACell
                  xs)
      (RT/le)
      (-ensure-numeric-success)
      ($.clj/boolean)))



(defn ==

  "Like classic `==` but with numeric cells."

  [& xs]

  (-> (into-array ACell
                  xs)
      (RT/eq)
      (-ensure-numeric-success)
      ($.clj/boolean)))



(defn >=

  "Like classic `>=` but with numeric cells."

  [& xs]

  (-> (into-array ACell
                  xs)
      (RT/ge)
      (-ensure-numeric-success)
      ($.clj/boolean)))



(defn >

  "Like classic `>` but with numeric cells."

  [& xs]

  (-> (into-array ACell
                  xs)
      (RT/gt)
      (-ensure-numeric-success)
      ($.clj/boolean)))


;;;;;;;;;; Countable


(defn count

  "Returns a JVM long representing the number of itms in the given cell.
  
   A countable is either:

   - Blob
   - Blob map
   - Map
   - List
   - Set
   - String
   - Vector"

  [^ACountable countable]

  (.count countable))



(defn empty?

  "Is the given `countable` empty?
  
   See [[count]]."

  [^ACountable countable]

  (if (nil? countable)
    true
    (.isEmpty countable)))



(defn nth

  "Like classic `nth` but for countables.

   Index must be a JVM long.

   See [[count]]."

  ^ACell

  [^ACountable countable index]

  (.get countable
        index))


;;;;;;;;;; Data structure


(defn assoc

  "Like classic `assoc` but for collection cells."

  ^ADataStructure

  [^ADataStructure coll k v]

  (.assoc coll
          k
          v))



(defn conj

  "Akin to classic `conj` but for collection cells."


  (^AVector []

   ($.cell/vector))


  (^ADataStructure [coll]

   coll)


  (^ADataStructure [^ADataStructure coll v]

   (.conj coll
          v)))



(defn contains?

  "Like classic `contains?` but for collection cells."

  [^ADataStructure coll k]

  (.containsKey coll
                k))



(defn empty

  "Like classic `empty` but for collection cells."

  ^ADataStructure

  [^ADataStructure coll]

  (.empty coll))



(defn get

  "Like classic `get` but for collection cells."

  (^ACell [^ADataStructure coll ^ACell k]

   (.get coll
         k))


  (^ACell [^ADataStructure coll ^ACell k not-found]

   (.get coll
         k
         not-found)))



(defn update

  "Akin to classic `update` but for collection cell."

  ^ACell

  [^ADataStructure coll ^ACell k f]

  (assoc coll
         k
         (f (get coll
                 k))))


;;;;;;;;;; Long


(defn dec

  "Like classic `dec` but for long cells."

  ^CVMLong

  [^CVMLong long]

  ($.cell/long (clojure.core/dec (.longValue long))))



(defn mod

  "Returns the integer modulus of a numerator divided by a divisor.
  
   Result will always be positive and consistent with Euclidean Divsion."

  ^CVMLong

  [a b]

  (-ensure-numeric-success (RT/mod a
                           b)))



(defn inc

  "Like classic `inc` but for long cells."

  ^CVMLong

  [^CVMLong long]

  ($.cell/long (clojure.core/inc (.longValue long))))


;;;;;;;;;; Map


(defn dissoc

  "Like classic `dissoc` but for map cells."

  ^AMap

  [^AMap map k]

  (if map
    (.dissoc map
             k)
    ($.cell/map)))



(defn find

  "Like classic `find`` but for map cells."

  ^AMap

  [^AMap map k]

  (when map
    (.getEntry map
               k)))



(defn keys

  "Like classic `keys` but for map cells.

   Returns an eager vector cell."

  ^AVector

  [^ACell map]

  (or (RT/keys map)
      (throw (IllegalArgumentException. "Must be a map"))))



(defn merge

  "Like classic `merge` but for hash map cells (not blob maps)."

  ^AHashMap

  [^AHashMap map-1 ^AHashMap map-2]

  (cond
    (nil? map-1) (or map-2
                     ($.cell/map))
    (nil? map-2) map-1
    :else        (.merge map-1
                         map-2)))



(defn vals

  "Like classic `vals` but for map cells.

   Returns an eager vector cell."

  ^AVector

  [^AMap map]

  (.values map))


;;;;;;;;;; Math


(defn +

  "Like classic `+` but for numeric cells."

  ^ANumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/plus
      -ensure-numeric-success))



(defn -

  "Like classic `-` but for numeric cells."

  ^ANumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/minus
      -ensure-numeric-success))



(defn *

  "Like classic `*` but for numeric cells."

  ^ANumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/times
      -ensure-numeric-success))



(defn abs

  "Returns the absolute value of `x`.
  
   Same type as `x`."

  ^ANumeric

  [number]

  (-ensure-numeric-success (RT/abs number)))



(defn ceil

  "Returns a double cell ceiling the value of `number`."

  ^CVMDouble

  [number]

  (-ensure-numeric-success (RT/ceil number)))



(defn div

  "Like classic `/` but for numeric cells."

  ^ANumeric

  [& xs]

  (-> (into-array ACell
                  xs)
      RT/divide
      -ensure-numeric-success))



(defn exp

  "Returns `e` raised to the power of the given numeric cell."

  ^CVMDouble

  [number]

  (-ensure-numeric-success (RT/exp number)))



(defn floor

  "Returns a double cell flooring the value of `x`."

  ^CVMDouble

  [x]

  (-ensure-numeric-success (RT/floor x)))



(defn nan?

  "Is the given `cell` NaN?"

  [^ACell cell]

  (RT/isNaN cell))



(defn pow

  "Returns a CVM double, `x` raised to the power of `y`."

  ^CVMDouble

  [^ACell x ^ACell y]

  (-> (RT/pow (doto ^"[Lconvex.core.data.ACell;" (make-array ACell
                                                             2)
                (aset 0
                      x)
                (aset 1
                      y)))
      -ensure-numeric-success))



(defn signum

  "Returns the sign of the number.

   More precisely:
  
   - `-1` if negative
   - `0` if 0
   - `1` if positive
  
   As a long cell if input is a long, double cell if it is a double."

  ^ANumeric

  [^ACell number]

  (-ensure-numeric-success (RT/signum number)))




(defn sqrt

  "Returns a double cell, the square root of the given `number` cell."

  ^CVMDouble

  [^ANumeric number]

  (-ensure-numeric-success (RT/sqrt number)))



(defn zero?

  "Like classic `zero?` but for cells."

  [^ACell x]

  (if-some [^ANumeric n (RT/ensureNumber x)]
    (= (.doubleValue n)
       0.0)
    false))


;;;;;;;;;; Sequence


(defn cons

  "Like classic `cons` but for collection cells.
  
   Returns a list cell."

  ^AList

  [x ^ACell coll]

  (RT/cons x
           (when (some? coll)
             (or (RT/sequence coll)
                 coll))))



(defn concat

  "Like classic `concat` but for collection cells.

   Return type is the same as `x`."

  ^ASequence

  [^ACell x ^ACell y]

  (RT/concat (when (some? x)
               (or (RT/sequence x)
                   x))
             (when (some? y)
               (or (RT/sequence y)
                   y))))



(defn next

  "Like classic `next` but for collection cells.
  
   Return type is a list cell if `coll` is a list, a vector cell otherwise."

  ^ASequence

  [^ACell coll]

  (when coll
    (.next (or ^ASequence (RT/sequence coll)
               ^ASequence coll))))



(defn reverse

  "Like classic `reverse` but for sequential cells (list or vector cells)."

  ^ASequence

  [^ASequence sq]

  (when sq
    (.reverse sq)))


;;;;;;;;;; Set


(defn difference

  "Like `clojure.set/difference` but for set cells."

  ^ASet

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) ($.cell/set)
    (nil? set-2) set-1
    :else        (.excludeAll set-1
                              set-2)))



(defn intersection

  "Like `clojure.set/intersection` but for set cells."

  ^ASet

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) ($.cell/set)
    (nil? set-2) ($.cell/set)
    :else        (.intersectAll set-1
                                set-2)))



(defn subset?

  "Like `clojure.set/subset?` but for set cells."

  [^ASet set-1 ^ASet set-2]

  (cond
    (nil? set-1) true
    (nil? set-2) false
    :else        (.isSubset set-1
                            set-2)))



(defn union

  "Like `clojure.set/union` but for set cells."

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

  "Like classic `name` but for keyword and symbol cells.
  
   Returns a string cell."

  ^AString

  [^ACell symbolic]

  (or (RT/name symbolic)
      (throw (IllegalArgumentException. "Must be symbolic"))))


;;;;;;;;; Other predicates


(defn address?

  "Is `x` an address?"

  [x]

  (instance? Address
             x))



(defn bigint?

  "Is `x` a bigint?"

  [x]

  (instance? CVMBigInteger
             x))



(defn blob?

  "Is `x` a blob?"

  [x]

  (instance? ABlob
             x))



(defn blob-map?

  "Is `x` a blob map?"

  [x]

  (instance? ABlobMap
             x))



(defn boolean?

  "Is `x` a CVM boolean?"

  [x]

  (instance? CVMBool
             x))



(defn char?

  "Is `x` a char cell?"

  [x]

  (instance? CVMChar
             x))



(defn cell?

  "Is `x` a cell?"

  [x]

  (instance? ACell
             x))



(defn coll?

  "Is `x` a collection cell?"

  [x]

  (instance? ADataStructure
             x))


(defn cvm-value?

  "Is `x` a CVM value?

   Returns false if `x` is not accessible to the CVM and meant to be used outside (eg. networking)."

  [^ACell x]

  (.isCVMValue x))



(defn double?

  "Is `x` a double cell?"

  [x]

  (instance? CVMDouble
             x))



(defn false?

  "Is `x` a `false` cell?"

  [x]

  (= x
     CVMBool/FALSE))



(defn fn?

  "Is `x` a CVM function?"

  [x]

  (instance? IFn
             x))



(defn hash-map?

  "Is `x` a hash map cell?"

  [x]

  (instance? AHashMap
             x))



(defn hash-set?

  "Is `x` a hash set cell?
  
   Currently at least, hast sets are the only kind of available sets."

  [x]

  (instance? AHashSet
             x))



(defn integer?

  "Is `x` an integer cell (either a bigint or a long)?"

  [x]

  (instance? AInteger
             x))



(defn keyword?

  "Is `x` a keyword cell?"

  [x]

  (instance? Keyword
             x))



(defn list?

  "Is `x` a list cell?"

  [x]

  (instance? AList
             x))



(defn long?

  "Is `x` a long cell?"

  [x]

  (instance? CVMLong
             x))



(defn map?

  "Is `x` a map cell?"

  [x]

  (instance? AMap
             x))



(defn number?

  "Is `x` a numeric cell?
  
   Either a long or a double."

  [x]

  (RT/isNumber x))



(defn set?

  "Is `x` a set cell?

   Currently at least, hast sets are the only kind of available sets."

  [x]

  (instance? ASet
             x))



(defn state?

  "Is `x` a state cell?"

  [x]

  (instance? State
             x))



(defn string?

  "Is `x` a string cell?"

  [x]

  (instance? AString
             x))



(defn symbol?

  "Is `x` a symbol cell?"

  [x]

  (instance? Symbol
             x))



(defn syntax?

  "Is `x` a syntax cell?"

  [x]

  (instance? Syntax
             x))



(defn transaction?

  "Is `x` a transaction?"

  [x]

  (instance? ATransaction
             x))



(defn true?

  "Is `x` a `true` cell?"

  [x]

  (= x
     CVMBool/TRUE))



(defn vector?

  "Is `x` a vector cell?"

  [x]

  (instance? AVector
             x))


;;;;;;;;;; Cell internals


(defn memory-size

  "Returns the total memory size of `cell` (cannot be `nil`).

   In other words, the number of bytes accounting for the encoding of the cell
   as well as all its children (if any)."

  [^ACell cell]

  (let [size (.getMemorySize cell)]
    (if (clojure.core/zero? size)
      (.getEncodingLength cell)
      size)))



(defn ref-stat 

  "Given a `cell` (cannot be `nil`), returns a map:
  
   | Key          | Value                              |
   |--------------|------------------------------------|
   | `:direct`    | Number of direct refs              |
   | `:embedded`  | Number of embedded refs            |
   | `:persisted` | Number of refs marked as persisted |
   | `:soft`      | Number of soft refs                |
   | `:total`     | Total number of refs               |

   This is for CVM developers familiar with the notion of cell references."

  [^ACell cell]

  (let [^Refs$RefTreeStats stat+ (Refs/getRefTreeStats (.getRef cell))
        direct                   (.-direct stat+)
        total                    (.-total stat+)]
    {:direct    direct
     :embedded  (.-embedded stat+)
     :persisted (.-persisted stat+)
     :soft      (clojure.core/- total
                                direct)
     :total     total}))
