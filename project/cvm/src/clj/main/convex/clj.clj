(ns convex.clj
  
  ""

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

  ""

  [^Address cell]

  (.longValue cell))



(defn blob

  ""

  [^ABlob cell]

  (.getBytes cell))



(defn boolean

  ""

  [^CVMBool cell]

  (.booleanValue cell))



(defn byte

  ""

  [^CVMByte cell]

  (.longValue cell))



(defn char

  ""

  [^CVMChar cell]

  (clojure.core/char (.longValue cell)))



(defn double

  ""

  [^CVMDouble cell]

  (.doubleValue cell))



(defn keyword

  ""

  [^Keyword cell]

  (clojure.core/keyword (.getName cell)))



(defn list

  ""

  [^AList cell]

  (clojure.core/map any
                    cell))



(defn long

  ""

  [^CVMLong cell]

  (.longValue cell))



(defn map

  ""

  [^AMap cell]

  (-> (reduce (fn [acc [k v]]
                (assoc! acc
                        (any k)
                        (any v)))
              (transient {})
              cell)
      persistent!))



(defn set

  ""

  [^ASet cell]

  (into #{}
        (clojure.core/map any)
        cell))



(defn string

  ""

  [^AString cell]

  (str cell))



(defn symbol

  ""

  [^Symbol cell]

  (clojure.core/symbol (.getName cell)))



(defn syntax

  ""

  [^Syntax cell]

  {:meta  (any (.getMeta cell))
   :value (any (.getValue cell))})


(defn vector

  ""

  [^AVector cell]

  (mapv any
        cell))


;;;;;;;;;; Protocol


(defprotocol IClojuresque

  ""

  (any [cell]

   ""))



(extend-protocol
  
  IClojuresque

  Address   (any [cell] (address cell))
  ABlob     (any [cell] (blob cell))
  AList     (any [cell] (list cell))
  AMap      (any [cell] (map cell))
  ASet      (any [cell] (set cell))
  AString   (any [cell] (string cell))
  CVMBool   (any [cell] (boolean cell))
  CVMByte   (any [cell] (byte cell))
  CVMChar   (any [cell] (char cell))
  CVMDouble (any [cell] (double cell))
  CVMLong   (any [cell] (long cell))
  Keyword   (any [cell] (keyword cell))
  Syntax    (any [cell] (syntax cell))
  Symbol    (any [cell] (symbol cell)))
