(ns convex.lisp.schema

  "Creating Malli schemas for working with Convex.
  
   See [[registry]]."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]
            [convex.lisp.form              :as $.form]
            [convex.lisp.hex               :as $.hex]
            [malli.core                    :as malli]))


;;;;;;;;;; Schemas


(defn call

  ""

  [sym schema-arg+]

  [:and
   [:cat
    [:= sym]
    schema-arg+]
   seq?])



(defn core

  ""


  ([]

   (core nil))


  ([registry]

   (assoc registry
          :convex.core/call   [:and
                               [:cat
                                :convex.core/symbol
                                [:*
                                 {:gen/fmap (partial map
                                                     $.form/quoted)}
                                 :convex/data
                                 ]]
                               seq?]
          :convex.core/symbol [:enum
                               '*
                               '*address*
                               '*aliases*
                               '*balance*
                               '*caller*
                               '*depth*
                               '*exports*
                               '*holdings*
                               '*initial-expander*
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
                               'actor
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
                               ;'eval
                               'eval-as
                               'exp
                               'expand
                               'expander
                               'export
                               'exports?
                               'fail
                               'first
                               'floor
                               'fn
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
                               'into
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
                               'zero?]
          :convex.core/result [:multi
                               {:dispatch first}
                               ['* :convex.core.api/*]
                               ['+ :convex.core.api/+]
                               ;:convex.core.api/-
                               ;:convex.core.api/div
                               ]

          :convex.core.api/*   (call '*
                                     [:* :convex.core/number])
          :convex.core.api/+   (call '+
                                     [:* :convex.core/number])
          :convex.core.api/-   (call '-
                                     [:+ :convex.core/number])
          :convex.core.api/div (call '/
                                     [:+ :convex.core/number])

          :convex.core/number  [:or
                                :convex/number
                                [:ref :convex.core.api/*]
                                [:ref :convex.core.api/+]
                                ;[:ref :convex.core.api/-]
                                ;[:ref :convex.core.api/div]
                                ]

          )))



;; Some attempt of creating more precise positive fuzzying by taking into account that a function can return different
;; types of output depending on inputs.
;;
;; But fails because of: https://github.com/metosin/malli/issues/450
;;
;;
;; (defn core
;; 
;;   ""
;; 
;; 
;;   ([]
;; 
;;    (core nil))
;; 
;; 
;;   ([registry]
;; 
;;    (assoc registry
;;           :convex.core/result [:multi
;;                                {:dispatch (partial take
;;                                                    2)}
;;                                ['(* :convex/double) '(* :convex/double)]
;;                                ['(* :convex/long)   '(* :convex/long)]
;;                                ]
;; 
;; 
;; 
;;           '(* :convex/double)  (call '*
;;                                      [:and
;;                                       [:* :convex.core/number]
;;                                       [:fn #(some double?
;;                                                   %)]])
;;           '(* :convex/long)    (call '*
;;                                      [:and
;;                                       [:* :convex.core/number]
;;                                       [:fn #(every? int?
;;                                                     %)]])
;; 
;; 
;;           :convex.core/double  [:or
;;                                 [:ref '(* :convex/double)]
;;                                 ]
;; 
;;           :convex.core/long    [:or
;;                                 [:ref '(* :convex/long)]
;;                                 ]
;; 
;;           :convex.core/number  [:or
;;                                 :convex/number
;;                                 :convex.core/double
;;                                 :convex.core/long
;;                                 ]
;;           )))



(defn data

  "Adds to the given `registry` all schemas for Convex data types."


  ([]

   (data nil))


  ([registry]

   (assoc registry
          :list                (malli/-collection-schema (fn [prop+ [child]]
                                                           {:empty           '()
                                                            :pred            seq?
                                                            :type            :list
                                                            :type-properties {:error/message "should be a list"
                                                                              :gen/fmap      #(or (list* %)
                                                                                                  '())
                                                                              :gen/schema    [:vector
                                                                                              prop+
                                                                                              child]}}))
          :convex/address      [:and
                                {:gen/fmap   $.form/address
                                 :gen/schema pos-int?}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches #"#\d+"
                                                        (name sym))))]]
          :convex/blob         [:and
                                {:gen/fmap   $.form/blob
                                 :gen/schema :convex/hexstring}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex
                                                        (name sym))))]]
		  :convex/blob-8	   [:and
								{:gen/fmap   $.form/blob
                                 :gen/schema :convex/hexstring-8}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex-8
                                                        (name sym))))]]
		  :convex/blob-32	   [:and
								{:gen/fmap   $.form/blob
                                 :gen/schema :convex/hexstring-32}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex-32
                                                        (name sym))))]]
          :convex/boolean      :boolean
          :convex/char         char?
          :convex/collection   [:or
                                :convex/list
                                :convex/map
                                :convex/set
                                :convex/vector]
          :convex/data         [:or
                                :convex/address
                                :convex/blob
                                :convex/boolean
                                :convex/char
                                :convex/double
                                :convex/keyword
                                :convex/list
                                :convex/long
                                :convex/map
                                :convex/nil
                                :convex/string
                                :convex/set
                                :convex/symbol
                                :convex/vector]
          :convex/double       [:double
                                {:gen/infinite? true
                                 :gen/NaN?      true}]
          :convex/falsy        [:enum
                                false
                                nil]
          :convex/hash         :convex/blob-32
          :convex/hexstring    [:re
                                {:gen/fmap   $.hex/from-int
                                 :gen/schema pos-int?}
                                $.hex/regex]
          :convex/hexstring-8  [:re
                                {:gen/fmap   $.hex/pad-8
                                 :gen/schema :convex/hexstring}
                                $.hex/regex-8]
          :convex/hexstring-32 [:re
                                {:gen/fmap   $.hex/pad-32
                                 :gen/schema :convex/hexstring}
                                $.hex/regex-32]
          :convex/keyword      :keyword
          :convex/list         [:list [:ref :convex/data]]
          :convex/long         :int
          :convex/map          [:map-of
                                [:ref :convex/data]
                                [:ref :convex/data]]
          :convex/meta         [:or
                                :convex/map
                                :convex/nil]
          :convex/nil          :nil
          :convex/number       [:or
                                :convex/double
                                :convex/long]
          :convex/scalar       [:or
                                :convex/address
                                :convex/blob
                                :convex/boolean
                                :convex/char
                                :convex/double
                                :convex/keyword
                                :convex/long
                                :convex/string
                                :convex/symbol]
          :convex/set          [:set
                                [:ref :convex/data]]
          :convex/string       [:string
                                 ;; TODO. Should not be necessary, see #66.
                                {:gen/gen tc.gen/string-alphanumeric}]
          :convex/symbol       [:and
                                :symbol
                                [:not= '_]
                                [:fn
                                 (fn [x]
                                   (not (or ($.form/address? x)
                                            ($.form/blob? x))))]]
          :convex/truthy       [:and
                                :convex/data
                                [:not :convex/falsy]]
          :convex/vector       [:vector [:ref :convex/data]])))



(defn sym-coercible

  "Returns a schema for a value that can be coerced to a Convex keyword or symbol."

  []

  [:or
   :convex/keyword
   :convex/symbol
   [:and
    :convex/string
    [:fn
     #(< 0
         (count %)
         32)]]])


;;;;;;;;;;


(defn registry

  "Adds to the given `registry` all relevant schemas from this namespace for working with Convex.

   If not `registry` is given, then the Malli default one is used. Otherwise, users must ensure that
   the given one is at least a subset."

  
  ([]

   (registry (malli/default-schemas)))


  ([registry]

   (-> registry
       core
       data)))
