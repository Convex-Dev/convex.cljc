(ns convex.data

  "Constructors for CVM objects and predicate functions for those objects."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.data AccountKey
                             Address
                             ABlob
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Blob
                             Keyword
                             Keywords
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
           (convex.core.lang Symbols)
           (java.util Collection
                      List))
  (:refer-clojure :exclude [boolean
                            boolean?
                            byte
                            char
                            char?
                            def
                            do
                            double
                            double?
                            import
                            key
                            keyword
                            keyword?
                            list
                            list?
                            long
                            map
                            map?
                            set
                            set?
                            string?
                            symbol
                            symbol?
                            quote
                            vector
                            vector?])
  (:require [clojure.core]))


(set! *warn-on-reflection*
      true)


(declare do
         import
         keyword
         map
         quote
         vector)


;;;;;;;;;; Creating values


(defn address

  "Creates a CVM address from a long."

  ^Address

  [long]

  (Address/create (clojure.core/long long)))



(defn blob

  "Creates a CVM blob from a byte array."

  ^Blob

  [byte-array]

  (Blob/create byte-array))



(defn boolean

  "Creates a CVM boolean given a falsy or truthy value."

  ^CVMBool
  
  [x]

  (CVMBool/create (clojure.core/boolean x)))



(defn byte

  "Creates a CVM byte from a value between 0 and 255 inclusive."

  ^CVMByte

  [b]

  (CVMByte/create b))



(defn char

  "Creates a CVM character from a regular characer."

  ^CVMChar

  [ch]

  (CVMChar/create (clojure.core/long ch)))



(defn double

  "Creates a CVM double."

  ^CVMDouble

  [x]

  (CVMDouble/create x))



(defn key

  "Creates an account key from a 32-byte `blob`.

   Returns nil if the given `blob` is of wrong size."

  ^AccountKey

  [^ABlob blob]

  (AccountKey/create blob))



(defn keyword

  "Creates a CVM keyword from a Clojure keyword."

  ^Keyword

  [^String string]

  (Keyword/create string))



(let [kw-message (keyword "message")]

  (defn ^AMap error
  
    "An error value as Convex data.

     `code` is often a CVM keyword (`:ASSERT` by default), `message` could be any CVM value, and `trace` is
     an optional stacktrace (CVM vector of CVM strings)."
  
  
    ([message]
  
     (map [[Keywords/CODE ErrorCodes/ASSERT]
           [kw-message    message]]))
  

    ([code message]

     (map [[Keywords/CODE code]
           [kw-message    message]]))
  

    ([code message trace]

     (map [[Keywords/CODE  code]
           [kw-message     message]
           [Keywords/TRACE trace]]))))
  


(defn list

  "Creates a CVM list from a collection of CVM items."


  (^AList []

   (list []))


  (^AList [x]

   (Lists/create x)))



(defn long

  "Creates a CVM long."

  ^CVMLong

  [x]

  (CVMLong/create x))



(defn map

  "Creates a CVM map from a collection of `[key value]`."


  (^AMap []

   (map []))


  (^AMap [x]

   (Maps/create ^List (clojure.core/map (fn [[k v]]
                                          (MapEntry/create k
                                                           v))
                                        x))))



(defn set

  "Creates a CVM set from a collection of CVM items."


  (^ASet []

   (set []))


  (^ASet [x]

   (Sets/create (vector x))))



(defn string

  "Creates a CVM string from a regular string."

  ^AString

  [string]

  (Strings/create string))



(defn symbol

  "Creates a CVM symbol from a string."

  ^Symbol

  [^String string]

  (Symbol/create string))



(defn vector

  "Creates a CVM vector from a collection of CVM items."


  (^AVector []

   (vector []))


  (^AVector [^Collection x]

   (Vectors/create x)))


;;;;;;;;;; Common form


(defn- -sym

  ;;

  [sym]

  (if (clojure.core/symbol? sym)
    (symbol (name sym))
    sym))



(defn def

  ""

  [sym x]

  (list [Symbols/DEF
         (-sym sym)
         x]))



(defn deploy

  ""


  ([code]

   (list [Symbols/DEPLOY
          (convex.data/quote code)]))


  ([sym code]

   (let [sym-2 (-sym sym)]
     (convex.data/do [(convex.data/def sym-2
                                       (deploy code))
                      (convex.data/import (list [(symbol "address")
                                                    sym-2])
                                             sym-2)]))))



(defn do

  ""
  
  [form+]

  (list (cons Symbols/DO
              form+)))



(defn import

  ""

  [x as]

  (list [(symbol "import")
         x
         (keyword "as")
         as]))



(defn quote

  ""

  [x]

  (list [Symbols/QUOTE
         x]))



(defn undef

  ""

  [sym]

  (list [Symbols/UNDEF
         sym]))


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


;;;;;;;;;; Miscellaneous predicates


(defn call?

  "Is `x` a call for `form` such as `(form ...)`?"

  [x form]

  (and (list? x)
       (= (first x)
          form)))
