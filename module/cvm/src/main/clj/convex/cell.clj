(ns convex.cell

  "Constructors for CVM cells."

  {:author "Adam Helinski"}

  (:import (clojure.lang IDeref)
           (convex.core ErrorCodes)
           (convex.core.data AccountKey
                             Address
                             ABlob
                             ABlobMap
                             ACell
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Blob
                             BlobMaps
                             Blobs
                             Format
                             Hash
                             Keyword
                             Keywords
                             Lists
                             MapEntry
                             Maps
                             Sets
                             Strings
                             Symbol
                             Syntax
                             Vectors)
           (convex.core.data.prim CVMBigInteger
                                  CVMBool
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (convex.core.lang RT)
           (convex.core.transactions ATransaction
                                     Call
                                     Invoke
                                     Multi
                                     Transfer)
           (java.util Collection
                      List))
  (:refer-clojure :exclude [*
                            bigint
                            boolean
                            byte
                            char
                            double
                            hash
                            key
                            keyword
                            list
                            long
                            map
                            set
                            symbol
                            vector])
  (:require [clojure.core]
            [convex.write  :as $.write]))


(set! *warn-on-reflection*
      true)


(declare keyword
         map
         vector)


;;;;;;;;;; Printing cells


(defmethod print-method ACell

  [cell ^java.io.Writer w]

  (.write w
          (str "#cvx "
               ($.write/string cell))))



(defmethod print-method Syntax

  [^Syntax syntax ^java.io.Writer w]

  (.write w
          (str "#cvx ^"
               ($.write/string (.getMeta syntax))
               " "
               ($.write/string (.getValue syntax)))))


;;;


(prefer-method print-method
               ACell
               IDeref)

(prefer-method print-method
               ACell
               java.util.List)

(prefer-method print-method
               ACell
               java.util.Map)

(prefer-method print-method
               ACell
               java.util.Set)


;;;;;;;;;; Creating values


(defn address

  "Creates an address from a long."

  ^Address

  [long]

  (Address/create (clojure.core/long long)))



(defn bigint

  "Creates a big integer cell from the given Clojure or Java a big integer."

  [n]

  (CVMBigInteger/wrap (biginteger n)))



(defn blob

  "Creates a blob from a byte array."

  ^Blob

  [^bytes byte-array]

  (Blob/create byte-array))



(defn blob<-hex

  "Creates a blob from a hex string."

  ^Blob

  [^String hex-string]

  (Blobs/fromHex hex-string))



(defn blob-map

  "Creates a blob map from a collection of `[blob value]`."


  (^ABlobMap []

   (BlobMaps/empty))


  (^ABlobMap [kvs]

   (reduce (fn [^ABlobMap bm [^ACell k ^ACell v]]
             (let [bm-2 (.assoc bm
                                k
                                v)]
               (or bm-2
                   (throw (IllegalArgumentException. "Key must be a blob")))))
           (blob-map)
           kvs)))



(defn boolean

  "Creates a boolean cell given a falsy or truthy value."

  ^CVMBool
  
  [x]

  (CVMBool/create (clojure.core/boolean x)))



(defn call

  "Creates a transaction for invoking a callable function."


  (^Call [address sequence address-callable function-name arg+]

  (Call/create address
               sequence
               address-callable
               function-name
               arg+))


  (^Call [address sequence address-callable offer function-name arg+]

  (Call/create address
               sequence
               address-callable
               offer
               function-name
               arg+)))



(defmacro code-std*

  "Given a Clojure keyword, returns the corresponding standard error code.
 
   Those are errors codes used by the CVM itself:
  
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

  "Creates a character cell from a regular character."

  ^CVMChar

  [ch]

  (CVMChar/create (clojure.core/long ch)))



(defn double

  "Creates a double cell."

  ^CVMDouble

  [x]

  (CVMDouble/create x))



(defn encoding

  "Returns a blob representing the encoding of the given `cell`.

   This encoding is meant for incremental updates."

  ^Blob

  [cell]

  (Format/encodedBlob cell))



(defn hash

  "Returns the hash of the given `cell`.
  
   A hash is a specialized 32-byte [[blob]]."

  ^Hash

  [^ACell cell]

  (Hash/compute cell))



(defn hash<-blob

  "Converts a 32-byte blob to a hash.
  
   See [[hash]]."

  ^Hash

  [^ABlob blob]

  (RT/ensureHash blob))



(defn hash<-hex

  "Creates a hash from a hex string.

   See [[hash]].
  
   Returns nil if hex string is of wrong format."

  ^Hash

  [^String hex-string]

  (Hash/fromHex hex-string))



(defn invoke

  "Creates a transaction for invoking code (a cell)."

  ^Invoke

  [^Address address ^Long sequence-id ^ACell cell]

  (Invoke/create address
                 sequence-id
                 cell))



(defn key

  "Creates an account key from a 32-byte blob.

   Returns nil if the given [[blob]] is of wrong size."

  ^AccountKey

  [^ABlob blob]

  (AccountKey/create blob))



(def key-fake

  "Zeroed account key for testing purposes.

   See [[key]]."

  (key (blob (byte-array 32))))



(defn keyword

  "Creates a keyword cell from a string."

  ^Keyword

  [^String string]

  (Keyword/create string))



(let [kw-message (keyword "message")]

  (defn error
  
    "An error value as Convex data.

     `code` is often a keyword cell (`:ASSERT` by default), `message` could be any cell (albeit often a human-readable
     string), and `trace` is an optional stacktrace (vector cell of string cells)."
  
  
    (^AMap [message]
  
     (map [[Keywords/CODE ErrorCodes/ASSERT]
           [kw-message    message]]))
  

    (^AMap [code message]

     (map [[Keywords/CODE code]
           [kw-message    message]]))
  

    (^AMap [code message trace]

     (map [[Keywords/CODE  code]
           [kw-message     message]
           [Keywords/TRACE trace]]))))
  


(defn list

  "Creates a list cell from a collection of cells."


  (^AList []

   (list []))


  (^AList [x]

   (Lists/create x)))



(defn long

  "Creates a long cell."

  ^CVMLong

  [^long x]

  (CVMLong/create x))



(defn map

  "Creates a map cell from a collection of `[key value]`."


  (^AMap []

   (map []))


  (^AMap [kvs]

   (Maps/create ^List (clojure.core/map (fn [[k v]]
                                          (MapEntry/create k
                                                           v))
                                        kvs))))


(defn- -multitrx

  ;; Used by [[multitrx-*]] functions.

  [^Address origin ^Long sequence-id mode trx+]

  (Multi/create origin
                sequence-id
                mode
                (into-array ATransaction
                            trx+)))



(defn multitrx-all

  "Creates a multi-transaction from a Vector of transactions to be executed
   according to a mode.

   The \"ALL\" mode is all or nothing: either all transactions succeeded or
   the state is reverted.

   Child transactions must have either the same origin as the parent multi-
   transaction or be controlled by the parent origin. Individual sequence IDs
   are ignored."

  ^Multi

  [^Address origin ^Long sequence-id trx+]

  (-multitrx origin
             sequence-id
             Multi/MODE_ALL
             trx+))



(defn multitrx-any

  "Like [[multitrx-all]] but the \"ANY\" mode executes all transactions
   regardless of individual results."

  ^Multi

  [^Address origin ^Long sequence-id trx+]

  (-multitrx origin
             sequence-id
             Multi/MODE_ANY
             trx+))



(defn multitrx-first

  "Like [[multitrx-all]] but the \"FIRST\" mode executes transactions until
   one succeeds."

  ^Multi

  [^Address origin ^Long sequence-id trx+]

  (-multitrx origin
             sequence-id
             Multi/MODE_FIRST
             trx+))



(defn multitrx-until

  "Like [[multitrx-until]] but the \"UNTIL\" mode executes transactions until
   one fails."

  ^Multi

  [^Address origin ^Long sequence-id trx+]

  (-multitrx origin
             sequence-id
             Multi/MODE_UNTIL
             trx+))



(defn set

  "Creates a set cell from a collection of items cell."


  (^ASet []

   (set []))


  (^ASet [^Collection x]

   (Sets/fromCollection x)))



(defn string

  "Creates a string cell from a regular string."

  ^AString

  [^String string]

  (Strings/create string))



(defn symbol

  "Creates a symbol cell from a string."

  ^Symbol

  [^String string]

  (Symbol/create string))



(defn syntax

  "Creates a syntax cell.

   It wraps the given `cell` and allow attaching a metadata [[map]]."


  (^Syntax [^ACell cell]

   (Syntax/create cell))


  (^Syntax [^ACell cell ^AMap metadata]

   (Syntax/create cell
                  metadata)))
  


(defn transfer

  "Creates a transaction for transferring Convex Coins."

  ^Transfer

  [address sequence address-receiver amount]

  (Transfer/create address
                   sequence
                   address-receiver
                   amount))



(defn vector

  "Creates a vector cell from a collection of items cell."


  (^AVector []

   (vector []))


  (^AVector [^Collection x]

   (Vectors/create x)))


;;;;;;;;;; Miscellaneous


(defn quoted

  "Wraps `x` in `quote`."

  [x]

  (list [(symbol "quote")
         x]))


;;;;;;;;;; Generic conversion Clojure -> Convex


(defprotocol ^:no-doc IEquivalent

  "Translates Clojure types to equivalent Convex types. Other objects remain as they are.

   However, the [[*]] macro is usually preferred for performance.

   ```clojure
   (any {:a ['b]})
   ```"

  (any [data]))



(extend-protocol IEquivalent

  
  nil

    (any [_]
      nil)


  Object

    (any [x]
      x)


  clojure.lang.ASeq

    (any [s]
      (list (clojure.core/map any
                              s)))


  clojure.lang.BigInt

    (any [n]
      (bigint n))


  clojure.lang.Keyword

    (any [k]
      (keyword (name k)))


  clojure.lang.IPersistentList

    (any [l]
      (list (clojure.core/map any
                              l)))


  clojure.lang.IPersistentMap

    (any [m]
      (map (clojure.core/map (fn [[k v]]
                               [(any k)
                                (any v)])
                             m)))


  clojure.lang.IPersistentSet

    (any [s]
      (set (clojure.core/map any
                             s)))


  clojure.lang.IPersistentVector

    (any [v]
      (vector (clojure.core/map any
                                v)))


  clojure.lang.Symbol

    (any [s]
      (symbol (name s)))


  java.lang.Boolean

    (any [b]
      (boolean b))


  java.lang.Character

    (any [c]
      (char c))


  java.lang.Double

    (any [d]
      (double d))


  java.lang.Long

    (any [i]
      (long i))


  java.lang.String

    (any [s]
      (string s))


  java.math.BigInteger

    (any [n]
      (bigint n)))



(declare ^:no-doc -*)



(defn ^:no-doc -splice

  ;; Offers unquote splicing support for [[-*]].

  [x+]
  
  (list* 'clojure.core/concat
         (clojure.core/map (fn [x]
                             (if (and (seq? x)
                                      (clojure.core/= (first x)
                                                      'clojure.core/unquote-splicing))
                               (second x)
                               [(-* x)]))
                           x+)))



(defn- ^:no-doc -*

  ;; Helper for [[*]].

  [form]

  (cond
    (clojure.core/seq? form)    (condp clojure.core/=
                                       (first form)
                                  'clojure.core/unquote          (second form)
                                  'clojure.core/unquote-splicing (throw (ex-info "Can only splice inside of a collection"
                                                                                 {::form form}))
                                  `(list ~(-splice form)))
    (clojure.core/map? form)    `(map ~(clojure.core/mapv (fn [[k v]]
                                                            [(-* k)
                                                             (-* v)])
                                                          form))
    (clojure.core/set? form)    `(set ~(-splice form))
    (clojure.core/vector? form) `(vector ~(-splice form))
    (clojure.core/symbol? form) (let [nmspace (namespace form)
                                      nm      (name form)]
                                  (if nmspace
                                    `(list [(symbol "lookup")
                                            (symbol ~nmspace)
                                            (symbol ~nm)])
                                    `(symbol ~nm)))
    :else                       `(any ~form)))



(defmacro *

  "Macro for translating Clojure types to Convex types.
  
   Convex types can be inserted using `~`, especially useful for inserting values dynamically or inserting types
   that have no equivalent in Clojure (eg. `address`).

   Also understands `~@` (aka unquote splicing).
   

   ```clojure
   ;; Cell for `(transfer #42 500000)`
   ;;
   (* (transfer ~(address 42)
                500000))
   ```"

  [x]

  (-* x))


;;;;;;;;;; Fake cells


(def ^:private ^Keyword -deref-me

  ;; See [[fake]].

  (keyword "DEREF-ME"))



(defn fake

  "Returns a proxy class which looks like a cell, wrapping `x` which can be
   any JVM value.
  
   For expert users only!
   Primarily useful for allowing actual cells to store any arbitrary JVM values.

   `deref` will return `x`.
  
   Prints as the keyword cell `DEREF-ME` but is not an actual symbol cell.
   Similarly, writing this fake cell to Etch writes the symbol cell `DEREF-ME`.
   Obviously, reading from Etch will return that actual symbol cell since only
   real cells can be serialized and deserialized (see [[convex.db]])."

  ^ACell

  [x]

  (proxy

    [ACell
     IDeref]

    []

    ;; IDeref

    (deref []
      x)

    ;; AObject

    (print [blob-builder limit]
      (.print -deref-me
              blob-builder
              limit))

   ;; ACell

    (encode [byte+ pos]
      (.encode -deref-me
               byte+
               pos))

    (estimatedEncodingSize []
      (.estimatedEncodingSize -deref-me))

    (getRefCount []
      (.getRefCount -deref-me))

    (getTag []
      (.getTag -deref-me))

    (isCanonical []
      true)

    (toCanonical []
      this)))



(defn fake?

  "Returns true if `cell` has been produced by [[fake]]."

  [x]

  (and (instance? ACell
                  x)
       (instance? IDeref
                  x)))
