(ns convex.gen

  "`test.check` generators for cells."

  {:author "Adam Helinski"}

  (:import (convex.core.lang Symbols))
  (:refer-clojure :exclude [boolean
                            byte
                            char
                            double
                            long
                            keyword
                            list
                            map
                            set
                            string
                            symbol
                            vector])
  (:require [clojure.string]
            [convex.cell                   :as $.cell]
            [clojure.test.check.generators :as TC.gen]))


;;;;;;;;;; Private


(def ^:private -byte

  ;; Generates bytes as longs.

  (TC.gen/choose 0
                 255))



(defn- -vec->blob

  ;; Converts a Clojure vector of bytes to a blob.

  [v]

  (-> v
      byte-array
      $.cell/blob))



(defn- -vec->string

  ;; Converts a Clojure vector of chars to a string cell.

  [v]

  (-> v
      clojure.string/join
      $.cell/string))


;;;;;;;;;; Miscellaneous


(defn quoted

  "Wraps the given `gen` so that the output is wrapped in a `quote` form."

  [gen]

  (TC.gen/fmap (fn [x]
                 ($.cell/list [Symbols/QUOTE
                               x]))
               gen))


;;;;;;;;;; Cells


(def address

  "Address cell."

  (TC.gen/fmap $.cell/address
               (TC.gen/large-integer* {:min 0})))



(defn blob

  "Blob cell.
  
   When length is not given, depends on current size."


  ([]

   (TC.gen/fmap -vec->blob
                (TC.gen/vector -byte)))


  ([n]

   (TC.gen/fmap -vec->blob
                (TC.gen/vector -byte
                               n)))


  ([n-min n-max]

   (TC.gen/fmap -vec->blob
                (TC.gen/vector -byte
                               n-min
                               n-max))))



(def blob-32

  "32-byte blob cell.

   Useful for CVM hashes and keys."

  (blob 32))



(def boolean

  "Boolean cell."

  (TC.gen/fmap $.cell/boolean
               TC.gen/boolean))



(def byte

  "Byte cell."

  (TC.gen/fmap $.cell/byte
               -byte))



(def char

  "Char cell between 0 and 255 inclusive."

  (TC.gen/fmap $.cell/char
               TC.gen/char))



(def char-alphanum

  "Like [[char]] but alphanumeric, hence always printable."

  (TC.gen/fmap $.cell/char
               TC.gen/char-alphanumeric))



(def double

  "Double cell."

  (TC.gen/fmap $.cell/double
               TC.gen/double))



(def long

  "Long cell."

  (TC.gen/fmap $.cell/long
               TC.gen/large-integer))



(def number

  "Either [[double]] or [[long]]."

  (TC.gen/one-of [double
                  long]))



(def nothing

  "Generates nil."

  (TC.gen/return nil))



(defn string

  "String cell containing [[char]]."


  ([]

   (TC.gen/fmap $.cell/string
                TC.gen/string))


  ([n]

   (TC.gen/fmap -vec->string
                (TC.gen/vector char
                               n)))


  ([n-min n-max]

   (TC.gen/fmap -vec->string
                (TC.gen/vector char
                               n-min
                               n-max))))



(defn string-alphanum

  "String cell containing [[char-alphanum]]."


  ([]

   (TC.gen/fmap $.cell/string
                TC.gen/string-alphanumeric))


  ([n]

   (TC.gen/fmap -vec->string
                (TC.gen/vector char-alphanum
                               n)))


  ([n-min n-max]

   (TC.gen/fmap -vec->string
                (TC.gen/vector char-alphanum
                               n-min
                               n-max))))



(def ^:private -string-symbolic

  ;; JVM string for building keyword and symbol cells.

  (TC.gen/fmap clojure.string/join
               (TC.gen/vector TC.gen/char-alphanumeric
                              1
                              64)))



(def keyword

  "Keyword cell."

  (TC.gen/fmap $.cell/keyword
               -string-symbolic))



(def symbol

  "Symbol cell."

  (TC.gen/fmap $.cell/symbol
               -string-symbolic))



(def symbol-quoted

  "Quoted symbol cell."

  (quoted symbol))



(def scalar

  "Anything that is not a collection."

  (TC.gen/one-of [address
                  (blob)
                  boolean
                  byte
                  char-alphanum
                  double
                  keyword
                  long
                  nothing
                  (string-alphanum)
                  symbol-quoted]))
