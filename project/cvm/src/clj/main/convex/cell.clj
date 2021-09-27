(ns convex.cell

  "Constructors for CVM cells and related type predicate functions."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.data AccountKey
                             Address
                             ABlob
                             ACell
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Blob
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
                             Vectors)
           (convex.core.data.prim CVMBool
                                  CVMByte
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (convex.core.lang RT)
           (convex.core.transactions Call
                                     Invoke
                                     Transfer)
           (java.util Collection
                      List))
  (:refer-clojure :exclude [*
                            boolean
                            boolean?
                            byte
                            char
                            char?
                            double
                            double?
                            hash
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
                            vector
                            vector?])
  (:require [clojure.core]))


(set! *warn-on-reflection*
      true)


(declare keyword
         map
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

  [^bytes byte-array]

  (Blob/create byte-array))



(defn blob<-hex

  "Creates a CVM blob from a hex string."

  ^Blob

  [hex-string]

  (Blobs/fromHex hex-string))



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



(defn call

  "Creates a transaction for invoking a callable function."


  (^Call [address sequence address-callable function-name args]

  (Call/create address
               sequence
               address-callable
               function-name
               args))


  (^Call [address sequence address-callable offer function-name args]

  (Call/create address
               sequence
               address-callable
               offer
               function-name
               args)))



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

  "Creates a CVM character from a regular character."

  ^CVMChar

  [ch]

  (CVMChar/create (clojure.core/long ch)))



(defn double

  "Creates a CVM double."

  ^CVMDouble

  [x]

  (CVMDouble/create x))



(defn encoding

  "Returns a [[blob]] representing the encoding of the given `cell`."

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

  "Converts a 32-byte [[blob]] to a hash."

  ^Hash

  [^ABlob blob]

  (RT/ensureHash blob))



(defn hash<-hex

  "Creates a [[hash]] from a hex string.
  
   Returns nil if hex string is of wrong format."

  ^Hash

  [^String hex-string]

  (Hash/fromHex hex-string))



(defn invoke

  "Creates a transaction for invoking code (a cell)."

  ^Invoke

  [^Address address ^long sequence ^ACell cell]

  (Invoke/create address
                 sequence
                 cell))



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

  "Creates a CVM keyword from a string."

  ^Keyword

  [^String string]

  (Keyword/create string))



(let [kw-message (keyword "message")]

  (defn ^AMap error
  
    "An error value as Convex data.

     `code` is often a CVM keyword (`:ASSERT` by default), `message` could be any CVM value (albeit often a human-readable
     string), and `trace` is an optional stacktrace (CVM vector of CVM strings)."
  
  
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


  (^ASet [^Collection x]

   (Sets/fromCollection x)))



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



(defn transfer

  "Creates a transaction for transferring Convex Coins."

  ^Transfer

  [address sequence address-receiver amount]

  (Transfer/create address
                   sequence
                   address-receiver
                   amount))



(defn vector

  "Creates a CVM vector from a collection of CVM items."


  (^AVector []

   (vector []))


  (^AVector [^Collection x]

   (Vectors/create x)))



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


;;;;;;;;;; Generic conversion Clojure -> Convex


(defprotocol ^:no-doc IEquivalent

  ;; Translates Clojure -> Convex
  ;;
  ;; Hidden because it should only be used at compile time.

  (-* [data]))



(extend-protocol IEquivalent


  Object

    (-* [x]
      x)


  clojure.lang.ASeq

    (-* [s]
      (list (clojure.core/map -*
                              s)))


  clojure.lang.Keyword

    (-* [k]
      (keyword (name k)))


  clojure.lang.IPersistentList

    (-* [l]
      (list (clojure.core/map -*
                              l)))


  clojure.lang.IPersistentMap

    (-* [m]
      (map (clojure.core/map (fn [[k v]]
                               [(-* k)
                                (-* v)])
                             m)))


  clojure.lang.IPersistentSet

    (-* [s]
      (set (clojure.core/map -*
                             s)))


  clojure.lang.IPersistentVector

    (-* [v]
      (vector (clojure.core/map -*
                                v)))


  clojure.lang.Symbol

    (-* [s]
      (symbol (name s)))


  java.lang.Boolean

    (-* [b]
      (boolean b))


  java.lang.Character

    (-* [c]
      (char c))


  java.lang.Double

    (-* [d]
      (double d))


  java.lang.Long

    (-* [i]
      (long i))


  java.lang.String

    (-* [s]
      (string s)))



(declare ^:no-doc -templ*)



(defn ^:no-doc -splice

  ;; Helper for [[-templ]].

  [x+]
  
  (apply concat
         (clojure.core/map (fn [x]
                             (if (and (seq? x)
                                      (clojure.core/= (first x)
                                                      'clojure.core/unquote-splicing))
                               (clojure.core/map -templ*
                                                 (second x))
                               [(-templ* x)]))
                           x+)))



(defn- ^:no-doc -templ*

  ;; Helper for [[templ*]].

  [form]

  (cond
    (clojure.core/seq? form)    (condp clojure.core/=
                                       (first form)
                                  'clojure.core/unquote          (eval (second form))
                                  'clojure.core/unquote-splicing (throw (ex-info "Can only splice inside of a collection"
                                                                                 {::form form}))
                                  (list (-splice form)))
    (clojure.core/map? form)    (map (clojure.core/map (fn [[k v]]
                                                         [(-templ* k)
                                                          (-templ* v)])
                                                       form))
    (clojure.core/set? form)    (set (-splice form))
    (clojure.core/vector? form) (vector (-splice form))
    :else                       (-* form)))



(defmacro *

  "Macro for templating Convex Lisp Code.
  
   Ressembles Clojure's syntax quote but does not namespace anything.

   Unquoting and unquote-splicing for inserting Clojure values are done through the literal notation (respectively
   **~** and **~@**) whereas those same features as Convex are written via forms (respecively `(unquote x)` and
   `(unquote-splicing x)`.
  
   For example:

   ```clojure
   (let [kw :foo
         xs [2 3]
         y  42]
     (templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing vs)]))
   ```

   Produces the following vector:

   ```clojure
   [:foo 1 2 3 4 42 y (unquote y) (unquote-splicing y)]
   ```"

  ;; Inspired by https://github.com/brandonbloom/backtick/

  [form]

  (clojure.core/list 'do
        (-templ* form)))
