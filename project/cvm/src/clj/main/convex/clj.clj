(ns convex.clj
  
  "Convert cells to Clojure types.
  
   Sometimes lossy since some cells do not have equivalents in Clojure. For instance, addresses are converted to long.
   Recursive when it comes to collection.
  
   Mainly useful for a deeper Clojure integration."

  {:author "Adam Helinski"}

  (:import (convex.core.data ABlob
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Address
                             Keyword
                             Symbol
                             Syntax)
           (convex.core.data.prim CVMBool
                                  CVMByte
                                  CVMChar
                                  CVMDouble
                                  CVMLong))
  (:refer-clojure :exclude [boolean
                            byte
                            char
                            double
                            keyword
                            list
                            long
                            map
                            set
                            symbol
                            vector]))


(declare any)


;;;;;;;;;;


(defn address

  "Returns the given `address` as a JVM long."

  [^Address address]

  (.longValue address))



(defn blob

  "Returns the given `blob` as a byte array."

  [^ABlob blob]

  (.getBytes blob))



(defn boolean

  "Returns the given `boolean` cell as a JVM boolean."

  [^CVMBool boolean]

  (.booleanValue boolean))



(defn byte

  "Returns the given `byte` cell as a JVM long."

  [^CVMByte cell]

  (.longValue cell))



(defn char

  "Returns the given `char` cell as a JVM char."

  [^CVMChar char]

  (clojure.core/char (.longValue char)))



(defn double

  "Returns the given `double` cell as a JVM double."

  [^CVMDouble double]

  (.doubleValue double))



(defn keyword

  "Returns the given `keyword` cell as a Clojure keyword."

  [^Keyword keyword]

  (clojure.core/keyword (str (.getName keyword))))



(defn list

  "Returns the given `list` cell as a Clojure list."

  [^AList list]

  (clojure.core/map any
                    list))



(defn long

  "Returns the given `long` cell as a JVM long."

  [^CVMLong long]

  (.longValue long))



(defn map

  "Returns the given `map` cell (hash map or blob map) as a Clojure map.
  
   Attention, in Clojure maps, sequential types containg the same items are equivalent but
   not in Convex. Hence, a clash could happen in the rare case where different sequential types
   are used as keys. For instance, the following is possible in Convex but not in Clojure (would
   complain about duplicate keys:

   ```clojure
   {[:a]  :foo
    '(:a) :foo}
   ```"

  [^AMap map]

  (-> (reduce (fn [acc [k v]]
                (assoc! acc
                        (any k)
                        (any v)))
              (transient {})
              map)
      persistent!))



(defn set

  "Returns the given `set` cell as a Clojure set.
  
   Same comment about sequential types as in [[map]] applies here."

  [^ASet set]

  (into #{}
        (clojure.core/map any)
        set))



(defn string

  "Returns the given `string` cell as a JVM string."

  [^AString string]

  (str string))



(defn symbol

  "Returns the given `symbol` cell as a Clojure symbol."

  [^Symbol symbol]

  (clojure.core/symbol (str (.getName symbol))))



(defn syntax

  "Returns the given `syntax` cell as a Clojure map such as:

   | Key | Value |
   |---|---|
   | `:meta` | Clojure map of metadata |
   | `:value` | Value wrapped, converted as well |"

  [^Syntax syntax]

  {:meta  (any (.getMeta syntax))
   :value (any (.getValue syntax))})


(defn vector

  "Returns the given `vector` cell as a Clojure vector."

  [^AVector vector]

  (mapv any
        vector))


;;;;;;;;;; Protocol


(defprotocol IClojuresque

  "Generic function for converting a cell to a Clojure representation.
  
   Relies all other functions from this namespace.

   ```clojure
   (any (convex.cell/* {:a [:b]}))
   ```"

  (any [cell]))



(extend-protocol
  
  IClojuresque

  nil       (any [cell] nil)
  Address   (any [cell] (address cell))
  ABlob     (any [cell] (blob cell))
  AList     (any [cell] (list cell))
  AMap      (any [cell] (map cell))
  ASet      (any [cell] (set cell))
  AString   (any [cell] (string cell))
  AVector   (any [cell] (vector cell))
  CVMBool   (any [cell] (boolean cell))
  CVMByte   (any [cell] (byte cell))
  CVMChar   (any [cell] (char cell))
  CVMDouble (any [cell] (double cell))
  CVMLong   (any [cell] (long cell))
  Keyword   (any [cell] (keyword cell))
  Syntax    (any [cell] (syntax cell))
  Symbol    (any [cell] (symbol cell)))
