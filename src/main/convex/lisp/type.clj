(ns convex.lisp.type

  "Constructing CVM objects."

  {:author "Adam Helinski"}

  (:import (convex.core.data Address
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Blob
                             Keyword
                             Lists
                             MapEntry
                             Maps
                             Sets
                             Strings
                             Symbol
                             Vectors)
           (convex.core.data.prim CVMBool
                                  CVMByte
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (java.util Collection
                      List))
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
                            vector])
  (:require [clojure.core]))


(set! *warn-on-reflection*
      true)


(declare vector)


;;;;;;;;;;


(defn address

  ""

  ^Address

  [long]

  (Address/create (clojure.core/long long)))



(defn blob

  ""

  ^Blob

  [byte-array]

  (Blob/create byte-array))



(defn boolean

  ""

  ^CVMBool
  
  [x]

  (CVMBool/create (clojure.core/boolean x)))



(defn byte

  ""

  ^CVMByte

  [b]

  (CVMByte/create b))



(defn char

  ""

  ^CVMChar

  [ch]

  (CVMChar/create (clojure.core/long ch)))



(defn double

  ""

  ^CVMDouble

  [x]

  (CVMDouble/create x))



(defn keyword

  ""

  ^Keyword

  [kw]

  (Keyword/create (name kw)))



(defn list

  ""

  ^AList

  [x]

  (Lists/create x))



(defn long

  ""

  ^CVMLong

  [x]

  (CVMLong/create x))


(defn map

  ""

  ^AMap

  [x]

  (Maps/create ^List (clojure.core/map (fn [[k v]]
                                         (MapEntry/create k
                                                          v))
                                       x)))



(defn set

  ""

  ^ASet

  [x]

  (Sets/create (vector x)))



(defn string

  ""

  ^AString

  [string]

  (Strings/create string))



(defn symbol

  ""


  (^Symbol [sym]

   (let [name- (name sym)]
     (if-some [ns- (namespace sym)]
       (Symbol/create (Symbol/create ns-)
                      (string name-))
       (Symbol/create ^String name-))))


  (^Symbol [namespace name]

   (let [name-2 (string (clojure.core/name name))]
     (if (int? namespace)
       (Symbol/create ^Address (address namespace)
                      name-2)
       (Symbol/create (Symbol/create ^String namespace)
                      name-2)))))



(defn vector

  ""

  ^AVector

  [^Collection x]

  (Vectors/create x))
