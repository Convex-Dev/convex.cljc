(ns convex.cell

  "Constructors for CVX cells and related type predicate functions.
  
   Also constructors for a few common idioms such as creating a [[def]] form."

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

  "Creates a CVX address from a long."

  ^Address

  [long]

  (Address/create (clojure.core/long long)))



(defn blob

  "Creates a CVX blob from a byte array."

  ^Blob

  [byte-array]

  (Blob/create byte-array))



(defn boolean

  "Creates a CVX boolean given a falsy or truthy value."

  ^CVMBool
  
  [x]

  (CVMBool/create (clojure.core/boolean x)))



(defn byte

  "Creates a CVX byte from a value between 0 and 255 inclusive."

  ^CVMByte

  [b]

  (CVMByte/create b))



(defmacro code-std*

  "Given a Clojure keyword, returns the corresponding standard error code (any of the Convex keyword the CVM itself
   uses):
  
   - `:ARGUMENT`
   - `:ARITY`
   - `:ASSERT`
   - `:BOUNDS`
   - `:CAST`
   - `:COMPILE`
   - `:DEPTH`
   - `:EXCEPTION`
   - `:EXPAND`
   - `:FATAL`
   - `:FUNDS`
   - `:HALT`
   - `:JUICE`
   - `:MEMORY`
   - `:NOBODY`
   - `:RECUR`
   - `:REDUCED`
   - `:RETURN`
   - `:ROLLBACK`
   - `:SEQUENCE`
   - `:SIGNATURE`
   - `:STATE`
   - `:TAILCALL`
   - `:TODO`
   - `:TRUST`
   - `:UNDECLARED`
   - `:UNEXPECTED`
  
   Throws if keyword does not match any of those.
  
   Note that in user functions, codes can be anything, any type, using those codes is not at all mandatory."

  [kw]

  (case kw
    :ARGUMENT   'convex.core.ErrorCodes/ARGUMENT
    :ARITY      'convex.core.ErrorCodes/ARITY
    :ASSERT     'convex.core.ErrorCodes/ASSERT
    :BOUNDS     'convex.core.ErrorCodes/BOUNDS
    :CAST       'convex.core.ErrorCodes/CAST
    :COMPILE    'convex.core.ErrorCodes/COMPILE
    :DEPTH      'convex.core.ErrorCodes/DEPTH
    :EXCEPTION  'convex.core.ErrorCodes/EXCEPTION
    :EXPAND     'convex.core.ErrorCodes/EXPAND
    :FATAL      'convex.core.ErrorCodes/FATAL
    :FUNDS      'convex.core.ErrorCodes/FUNDS
    :HALT       'convex.core.ErrorCodes/HALT
    :JUICE      'convex.core.ErrorCodes/JUICE
    :MEMORY     'convex.core.ErrorCodes/MEMORY
    :NOBODY     'convex.core.ErrorCodes/NOBODY
    :RECUR      'convex.core.ErrorCodes/RECUR
    :REDUCED    'convex.core.ErrorCodes/REDUCED
    :RETURN     'convex.core.ErrorCodes/RETURN
    :ROLLBACK   'convex.core.ErrorCodes/ROLLBACK
    :SEQUENCE   'convex.core.ErrorCodes/SEQUENCE
    :SIGNATURE  'convex.core.ErrorCodes/SIGNATURE
    :STATE      'convex.core.ErrorCodes/STATE
    :TAILCALL   'convex.core.ErrorCodes/TAILCALL
    :TODO       'convex.core.ErrorCodes/TODO
    :TRUST      'convex.core.ErrorCodes/TRUST
    :UNDECLARED 'convex.core.ErrorCodes/UNDECLARED
    :UNEXPECTED 'convex.core.ErrorCodes/UNEXPECTED
    (throw (ex-info (str "There is no official exception code for: "
                         kw)
                    {:convex.cell/code kw}))))



(defn char

  "Creates a CVX character from a regular character."

  ^CVMChar

  [ch]

  (CVMChar/create (clojure.core/long ch)))



(defn double

  "Creates a CVX double."

  ^CVMDouble

  [x]

  (CVMDouble/create x))



(defn key

  "Creates an account key from a 32-byte [[blob]].

   Returns nil if the given [[blob]] is of wrong size."

  ^AccountKey

  [^ABlob blob]

  (AccountKey/create blob))



(def key-fake

  "Zeroed [[key]] that can be used during dev and testing so that an account is considered as a user, not an actor."

  (key (blob (byte-array 32))))



(defn keyword

  "Creates a CVX keyword from a string."

  ^Keyword

  [^String string]

  (Keyword/create string))



(let [kw-message (keyword "message")]

  (defn ^AMap error
  
    "An error value as Convex data.

     `code` is often a CVX keyword (`:ASSERT` by default), `message` could be any CVX value (albeit often a human-readable
     string), and `trace` is an optional stacktrace (CVX vector of CVX strings)."
  
  
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

  "Creates a CVX list from a collection of CVX items."


  (^AList []

   (list []))


  (^AList [x]

   (Lists/create x)))



(defn long

  "Creates a CVX long."

  ^CVMLong

  [x]

  (CVMLong/create x))



(defn map

  "Creates a CVX map from a collection of `[key value]`."


  (^AMap []

   (map []))


  (^AMap [x]

   (Maps/create ^List (clojure.core/map (fn [[k v]]
                                          (MapEntry/create k
                                                           v))
                                        x))))



(defn set

  "Creates a CVX set from a collection of CVX items."


  (^ASet []

   (set []))


  (^ASet [^Collection x]

   (Sets/fromCollection x)))



(defn string

  "Creates a CVX string from a regular string."

  ^AString

  [string]

  (Strings/create string))



(defn symbol

  "Creates a CVX symbol from a string."

  ^Symbol

  [^String string]

  (Symbol/create string))



(defn vector

  "Creates a CVX vector from a collection of CVX items."


  (^AVector []

   (vector []))


  (^AVector [^Collection x]

   (Vectors/create x)))


;;;;;;;;;; Common form


(defn- -sym

  ;; Casts `sym` to a CVX symbol if it is a CLJ one.

  [sym]

  (if (clojure.core/symbol? sym)
    (symbol (name sym))
    sym))



(defn def

  "Creates a `def` form which interns `x` under `sym`."

  [sym x]

  (list [Symbols/DEF
         (-sym sym)
         x]))



(defn deploy

  "Creates a `deploy` form which deploys `code`.
  
   If `sym` is provided, the deploy form is embedded in a [[def]]."


  ([code]

   (list [Symbols/DEPLOY
          (convex.cell/quote code)]))


  ([sym code]

   (let [sym-2 (-sym sym)]
     (convex.cell/do [(convex.cell/def sym-2
                                       (deploy code))
                      (convex.cell/import (list [(symbol "address")
                                                    sym-2])
                                             sym-2)]))))



(defn do

  "Creates a `do` form embedded the given cells."
  
  [cell+]

  (list (cons Symbols/DO
              cell+)))



(defn import

  "Creates an `import` form which imports `x` as `as`."

  [x as]

  (list [(symbol "import")
         x
         (keyword "as")
         as]))



(defn quote

  "Creates form which quotes `x`."

  [x]

  (list [Symbols/QUOTE
         x]))



(defn undef

  "Opposite of [[def]]."

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

  "Is `x` a CVX boolean?"

  [x]

  (instance? CVMBool
             x))



(defn byte?

  "Is `x` a CVX byte?"

  [x]

  (instance? CVMByte
             x))



(defn char?

  "Is `x` a CVX char?"

  [x]

  (instance? CVMChar
             x))



(defn double?

  "Is `x` a CVX double?"

  [x]

  (instance? CVMDouble
             x))



(defn keyword?

  "Is `x` a CVX keyword?"

  [x]

  (instance? Keyword
             x))



(defn list?

  "Is `x` a CVX list?"

  [x]

  (instance? AList
             x))



(defn long?

  "Is `x` a CVX long?"

  [x]

  (instance? CVMLong
             x))



(defn map?

  "Is `x` a CVX map?"

  [x]

  (instance? AMap
             x))



(defn set?

  "Is `x` a CVX set?"

  [x]

  (instance? ASet
             x))



(defn string?

  "Is `x` a CVX string?"

  [x]

  (instance? AString
             x))



(defn symbol?

  "Is `x` a CVX symbol?"

  [x]

  (instance? Symbol
             x))



(defn vector?

  "Is `x` a CVX vector?"

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
