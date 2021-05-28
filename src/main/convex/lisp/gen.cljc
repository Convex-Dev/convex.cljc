(ns convex.lisp.gen

  "Generators for Convex Lisp types and constructs."

  {:author "Adam Helinski"}

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
  (:require [clojure.core]
            [clojure.set]
            [clojure.string]
            [clojure.test.check.generators :as TC.gen]
            [convex.lisp.form              :as $.form]))


;;;;;;;;;; Miscellaneous helpers


(defn quoted

  "Quotes the value produced by `gen` such as `(quote x)`."

  [gen]

  (TC.gen/fmap $.form/quoted
               gen))


;;;;;;;;;; Miscellaneous scalar types


(def address

  "Any address."

  (TC.gen/fmap $.form/address
               (TC.gen/large-integer* {:min 0})))



(def boolean

  "True or false."

  TC.gen/boolean)



(def byte

  "Value between -128 and 127."

  (TC.gen/choose -128
                 127))



(def double

  "Any double, including +/- Infinity and NaN."

  TC.gen/double)



(def long

  "Any long."

  TC.gen/large-integer)



(def number

  "Either [[double]] or [[long]]"

  (TC.gen/one-of [double
                  long]))



(defn number-bounded

  "Like [[number]] but accept a map with `:min` and `:max` for setting boundaries.
  
   Both are optional."

  [option+]

  (TC.gen/one-of [(TC.gen/double* option+)
                  (TC.gen/large-integer* option+)]))



(def nothing

  "Always generated nil."

  (TC.gen/return nil))


;;;;;;;;;; Hex-strings


(def hex-digit

  "Any valid character for hexadecimal notation, from `\0` to  `\f`."

  ;; TODO. Case insensitive, but CVM outputs as lowercase.

  (TC.gen/elements [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f]))



(def hex-string

  "A valid hex-string where each byte is written as two [[hex-digit]]."

  (TC.gen/fmap (fn [digit+]
                 (let [n (count digit+)]
                   (clojure.string/join (if (even? n)
                                          digit+
                                          (conj digit+
                                                (nth digit+
                                                     (rand-int n)))))))
               (TC.gen/vector hex-digit)))



(def hex-string-8

  "Like [[hex-string]] but fixed size representing 8 bytes.
  
   Compatible with addresses."

  (TC.gen/fmap (fn [[x x+]]
                 (clojure.string/join (cons x
                                            x+)))
               (TC.gen/tuple (TC.gen/elements [\0 \1 \2 \3 \4 \5 \6 \7])
                             (TC.gen/vector hex-digit
                                            15))))



(def hex-string-32

  "Like [[hex-string]] but fixed size representing 32 bytes."

  (TC.gen/fmap clojure.string/join
               (TC.gen/vector hex-digit
                              64)))


;;;;;;;;;; Blobs


(def blob

  "Any blob."

  (TC.gen/fmap $.form/blob
               hex-string))



(def blob-8

  "Like [[blob]] but fixed size representing 8 bytes.
  
   Compatible with addresses."

  (TC.gen/fmap $.form/blob
               hex-string-8))



(def blob-32

  "Like [[blob]] but fixed size representing 32 bytes."

  (TC.gen/fmap $.form/blob
               hex-string-32))


;;;;;;;;;; Characters and strings


(def char

  "Any character"

  TC.gen/char)



(def string
  
  "Any string."

  ;; TOOD. Should not be alphanum only but Convex does not escape all special chars yet.

  TC.gen/string-alphanumeric)


;;;;;;;;;; Symbolic


(def string-symbolic

  "Like [[string]] but can be used for building keywords and symbols.
  
   Size is between 1 and 64 characters and the first character is compatible with symbolic types."

  ;; TODO. Not only alphanumeric, see [[string]].

  (TC.gen/fmap (fn [[ch-1 ch+]]
                 (clojure.string/join (cons ch-1
                                            ch+)))
               (TC.gen/tuple TC.gen/char-alpha
                             (TC.gen/vector TC.gen/char-alphanumeric
                                            0
                                            63))))



(def keyword

  "Any keyword."

  (TC.gen/fmap clojure.core/keyword
               string-symbolic))



(def symbol

  "Any unqualified symbol."

  (TC.gen/fmap clojure.core/symbol
               string-symbolic))



(def symbol-ns

  "Any qualified symbol where namespace can be either an address or an unqualified symbol."

  (TC.gen/fmap (fn [[namespace- name-]]
                 (clojure.core/symbol namespace-
                                      name-))
               (TC.gen/tuple (TC.gen/one-of [(TC.gen/fmap str
                                                          address)
                                              string-symbolic])
                             string-symbolic)))



(def symbol-quoted

  "Like [[symbol]] but the output is quoted."

  (quoted symbol))



(def symbol-ns-quoted

  "Like [[symbol-ns]] but the output is quoted."

  (quoted symbol-ns))


;;;;;;;;;; Recursive definition of Convex types


(def scalar

  "Any of:
  
   - [[address]]
   - [[blob]]
   - [[boolean]]
   - [[char]]
   - [[double]]
   - [[keyword]]
   - [[long]]
   - [[nothing]]
   - [[string]]
   - [[symbol-quoted]]
   - [[symbol-ns-quoted]]"

  (TC.gen/one-of [address
                  blob
                  boolean
                  char
                  double
                  keyword
                  long
                  nothing
                  string
                  (TC.gen/one-of [symbol-quoted
                                  symbol-ns-quoted])]))



(def recursive

  "Base generators for recursive Convex types where an item of a collection can be a collection as well.
  
   Leaves are [[scalar]] while containes can be lists, maps, sets, and vectors.
  
   Produces a scalar in roughly 10% of outputs."

  (TC.gen/recursive-gen (fn [gen-inner]
                          (let [gen-vector (TC.gen/vector gen-inner)]
                            (TC.gen/one-of [(TC.gen/fmap $.form/list
                                                         gen-vector)
                                            (TC.gen/scale #(quot %
                                                                 2)
                                                          (TC.gen/map gen-inner
                                                                      gen-inner))
                                            (TC.gen/set gen-inner)
                                            gen-vector])))
                        scalar))


;;;;;;;;;; Collections


(def list

  "Any list."
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    ($.form/list (reduce-kv conj
                                                       []
                                                       x))
                   (set? x)    ($.form/list x)
                   (seq? x)    (if ($.form/list? x)
                                 x
                                 ($.form/list [x]))
                   (vector? x) ($.form/list x)
                   :else       ($.form/list [x])))
               recursive))



(let [-to-map (fn [sq]
                (into {}
                      (clojure.core/map vec)
                      (partition 2
                                 2
                                 sq
                                 sq)))]
  (def map

    "Any hash-map (not blob-maps)."
    
    (TC.gen/fmap (fn [x]
                   (cond
                     (map? x)    x
                     (set? x)    (-to-map x)
                     (seq? x)    (if ($.form/list? x)
                                   (-to-map (rest x))
                                   {x x})
                     (vector? x) (-to-map x)
                     :else       {x x}))
                 recursive)))



(def set

  "Any set."
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    (reduce-kv conj
                                          #{}
                                          x)
                   (set? x)    x
                   (seq? x)    (if ($.form/list? x)
                                 (clojure.core/set (rest x))
                                 #{x})
                   (vector? x) (clojure.core/set x)
                   :else       #{x}))
               recursive))



(def vector

  "Any vector."
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    (reduce-kv conj
                                          []
                                          x)
                   (set? x)    (vec x)
                   (seq? x)    (if ($.form/list? x)
                                 (vec (rest x))
                                 [x])
                   (vector? x) x
                   :else       [x]))
               recursive))


;;;;;;;;;; Grouping types


(def any

  "Any type from this namespace."

  (TC.gen/frequency [[10 recursive]
                     [9  scalar]]))



(defn any-but

  "Any type but from the given `exclusion-set`."

  [exclusion-set]

  (TC.gen/one-of (vec (clojure.set/difference #{address
                                                blob
                                                boolean
                                                char
                                                double
                                                keyword
                                                list
                                                long
                                                nothing
                                                map
                                                set
                                                string
                                                symbol-quoted
                                                symbol-ns-quoted
                                                vector}
                                              exclusion-set))))



(def collection

  "Any collection."

  (TC.gen/one-of [list
                  map
                  set
                  vector]))



(def sequential

  "Either a list of a vector."

  (TC.gen/one-of [list
                  vector]))


;;;;;;;;;; Pseudo booleans


(def falsy

  "Either false or nil."

  (TC.gen/elements [false
                    nil]))



(def truthy

  "Any but [[falsy]]."

  (TC.gen/such-that #(and (some? %)
                          (not (false? %)))
                    any))


;;;;;;;;;; Miscellaneous utilities


(defn binding+

  "Vector of `[ [[symbol]] [[any]] ]` where symbols are garanteed to be unique.
  
   Useful for generating `let`-like bindings."


  ([n-min n-max]

   (binding+ n-min
             n-max
             any))


  ([n-min n-max gen-value]

   (TC.gen/let [sym+ (TC.gen/vector-distinct symbol
                                             {:max-elements n-max
                                              :min-elements n-min})
                x+   (TC.gen/vector gen-value
                                    (count sym+))]
     (mapv vec
           (partition 2
                      (interleave sym+
                                  x+))))))


;;;;;;;;;; Core


(def core-symbol

  ""

  (TC.gen/elements ['*
                    '*address*
                    '*aliases*
                    '*balance*
                    '*caller*
                    '*depth*
                    '*exports*
                    '*holdings*
                    ;'*initial-expander* ;; TODO. https://github.com/Convex-Dev/convex/issues/150
                    '*juice*
                    '*key*
                    '*memory*
                    '*offer*
                    '*origin*
                    '*registry*
                    '*result*
                    '*sequence*
                    '*state*
                    '*timestamp*
                    '+
                    '-
                    '/
                    '<
                    '<=
                    '=
                    '==
                    '>
                    '>=
                    'abs
                    'accept
                    'account
                    'account?
                    ; 'actor ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                    'actor?
                    'address
                    'address?
                    'and
                    'apply
                    'assert
                    'assoc
                    'assoc-in
                    'balance
                    'blob
                    'blob-map
                    'blob?
                    'boolean
                    'boolean?
                    'byte
                    'call
                    'call*
                    'ceil
                    'char
                    'coll?
                    'compile
                    'concat
                    'cond
                    'conj
                    'cons
                    'contains-key?
                    'count
                    'create-account
                    'dec
                    'def
                    'defactor
                    'defexpander
                    'defined?
                    'defmacro
                    'defn
                    'deploy
                    'difference
                    'disj
                    'dissoc
                    'do
                    'doc
                    'dotimes
                    'double
                    'empty
                    'empty?
                    'encoding
                    'eval
                    'eval-as
                    'exp
                    ;'expand ;; TODO. https://github.com/Convex-Dev/convex/issues/149
                    'export
                    'exports?
                    'fail
                    'first
                    'floor
                    ; 'fn ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                    'fn?
                    'for
                    'get
                    'get-holding
                    'get-in
                    'halt
                    'hash
                    'hash-map
                    'hash-set
                    'hash?
                    'identity
                    'if
                    'if-let
                    'import
                    'inc
                    'intersection
                    ;'into
                    'keys
                    'keyword
                    'keyword?
                    'last
                    'let
                    'list
                    'list?
                    'log
                    'long
                    'long?
                    'lookup
                    'lookup-syntax
                    'loop
                    'macro
                    'map
                    'map?
                    'mapcat
                    'mapv
                    'max
                    'merge
                    'meta
                    'min
                    'mod
                    'name
                    'next
                    'nil?
                    'not
                    'nth
                    'number?
                    'or
                    'pow
                    'quasiquote
                    'query
                    'quot
                    'quote
                    'recur
                    'reduce
                    'reduced
                    'rem
                    'return
                    'rollback
                    'schedule
                    'schedule*
                    'second
                    'set
                    'set!
                    'set*
                    'set-controller
                    'set-holding
                    'set-key
                    'set-memory
                    'set?
                    'signum
                    'sqrt
                    'stake
                    'str
                    'str?
                    'subset?
                    'symbol
                    'symbol?
                    'syntax
                    'syntax?
                    'transfer
                    'transfer-memory
                    'undef
                    'undef*
                    'union
                    'unsyntax
                    'values
                    'vec
                    'vector
                    'vector?
                    'when
                    'when-let
                    'when-not
                    'zero?]))



(defn random-call

  ""

  
  ([]

   (random-call nil))


  ([option+]

   (TC.gen/let [sym  (or (:gen-sym option+)
                         core-symbol)
                arg+ (TC.gen/vector (or (:gen-arg option+)
                                        any)
                                    (or (:min option+)
                                        0)
                                    (or (:max option+)
                                        16))]
     (list* sym
            arg+))))


;;;;;;;;;;

     
(comment


  (TC.gen/generate list
                   30)


  )
