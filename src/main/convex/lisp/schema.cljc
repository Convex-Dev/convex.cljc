(ns convex.lisp.schema

  "Creating Malli schemas for working with Convex.
  
   See [[registry]]."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]
            [convex.lisp                   :as $.lisp]
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



(defn quoted

  ""

  [schema]

  (call 'quote
        schema))



(defn core

  ""


  ([]

   (core nil))


  ([registry]

   (assoc registry
          :convex.core/call   [:and
                               [:cat
                                :convex.core/symbol
                                [:* :convex/data]]
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
                               {:dispatch (fn [x]
                                            (cond->
                                              x
                                              (seq? x)
                                              first))}
                               ['* :convex.core.api/*]
                               ['+ :convex.core.api/+]
                               ['- :convex.core.api/-]
                               ['/ :convex.core.api/div]
                               ['< :convex.core.api/<]
                               ['<= :convex.core.api/<=]
                               ['= :convex.core.api/=]
                               ['== :convex.core.api/==]
                               ['> :convex.core.api/>]
                               ['>= :convex.core.api/>=]
                               ['abs :convex.core.api/abs]
                               ;['accept :convex.core.api/accept]
                               ['account :convex.core.api/account]
                               ['account? :convex.core.api/account?]
                               ['actor :convex.core.api/actor]
                               ['actor? :convex.core.api/actor?]
                               ['address :convex.core.api/address]
                               ['address? :convex.core.api/address?]
                               ['and :convex.core.api/and]
                               ;['apply :convex.core.api/apply]
                               ['assert :convex.core.api/assert]
                               ;['assoc :convex.core.api/assoc]
                               ;['assoc-in :convex.core.api/assoc-in]
                               ['balance :convex.core.api/balance]
                               ['blob :convex.core.api/blob]
                               ;['blob-map :convex.core.api/blob-map]
                               ['blob? :convex.core.api/blob?]
                               ['boolean :convex.core.api/boolean]
                               ['boolean? :convex.core.api/boolean?]
                               ['byte :convex.core.api/byte]
                               ;['call :convex.core.api/call]
                               ;['call* :convex.core.api/call*]
                               ['ceil :convex.core.api/ceil]
                               ['char :convex.core.api/char]
                               ['coll? :convex.core.api/coll?]
                               ['compile :convex.core.api/compile]
                               ['concat :convex.core.api/concat]
                               ['cond :convex.core.api/cond]
                               ;['conj :convex.core.api/conj]
                               ['cons :convex.core.api/cons]
                               ['contains-key? :convex.core.api/contains-key?]
                               ['count :convex.core.api/count]
                               ['create-account :convex.core.api/create-account]
                               ['dec :convex.core.api/dec]
                               ['def :convex.core.api/def]
                               ['defactor :convex.core.api/defactor]
                               ;['defexpander :convex.core.api/defexpander]
                               ['defined? :convex.core.api/defined?]
                               ['defmacro :convex.core.api/defmacro]
                               ['defn :convex.core.api/defn]
                               ;['deploy :convex.core.api/deploy]
                               ['difference :convex.core.api/difference]
                               ['disj :convex.core.api/disj]
                               ['dissoc :convex.core.api/dissoc]
                               ;['do :convex.core.api/do]
                               ['doc :convex.core.api/doc]
                               ['dotimes :convex.core.api/dotimes]
                               ['double :convex.core.api/double]
                               ['empty :convex.core.api/empty]
                               ['empty? :convex.core.api/empty?]
                               ['encoding :convex.core.api/encoding]
                               ;['eval ':convex.core.api/eval]
                               ;['eval-as :convex.core.api/eval-as]
                               ['exp :convex.core.api/exp]
                               ;['expand :convex.core.api/expand]
                               ;['expander :convex.core.api/expander]
                               ['export :convex.core.api/export]
                               ['exports? :convex.core.api/exports?]
                               ;['fail :convex.core.api/fail]
                               ['first :convex.core.api/first]
                               ['floor :convex.core.api/floor]
                               ['fn :convex.core.api/fn]
                               ['fn? :convex.core.api/fn?]
                               ;['for :convex.core.api/for]
                               ['get :convex.core.api/get]
                               ['get-holding :convex.core.api/get-holding]
                               ;['get-in :convex.core.api/get-in]
                               ['halt :convex.core.api/halt]
                               ['hash :convex.core.api/hash]
                               ['hash-map :convex.core.api/hash-map]
                               ['hash-set :convex.core.api/hash-set]
                               ['hash? :convex.core.api/hash?]
                               ['identity :convex.core.api/identity]
                               ['if :convex.core.api/if]
                               ['if-let :convex.core.api/if-let]
                               ;['import :convex.core.api/import]
                               ['inc :convex.core.api/inc]
                               ['intersection :convex.core.api/intersection]
                               ['into :convex.core.api/into]
                               ['keys :convex.core.api/keys]
                               ['keyword :convex.core.api/keyword]
                               ['keyword? :convex.core.api/keyword?]
                               ['last :convex.core.api/last]
                               ['let :convex.core.api/let]
                               ['list :convex.core.api/list]
                               ['list? :convex.core.api/list?]
                               ['log :convex.core.api/log]
                               ['long :convex.core.api/long]
                               ['long? :convex.core.api/long?]
                               ['lookup :convex.core.api/lookup]
                               ['lookup-syntax :convex.core.api/lookup-syntax]
                               ;['loop :convex.core.api/loop]
                               ;['macro :convex.core.api/macro]
                               ['map :convex.core.api/map]
                               ['map? :convex.core.api/map?]
                               ;['mapcat :convex.core.api/mapcat]
                               ['mapv :convex.core.api/mapv]
                               ['max :convex.core.api/max]
                               ['merge :convex.core.api/merge]
                               ['meta :convex.core.api/meta]
                               ['min :convex.core.api/min]
                               ['mod :convex.core.api/mod]
                               ['name :convex.core.api/name]
                               ['next :convex.core.api/next]
                               ['nil? :convex.core.api/nil?]
                               ['not :convex.core.api/not]
                               ;['nth :convex.core.api/nth]
                               ['number? :convex.core.api/number?]
                               ['or :convex.core.api/or]
                               ['pow :convex.core.api/pow]
                               ;['query :convex.core.api/query]
                               ['quot :convex.core.api/quot]
                               ['quote :convex.core.api/quote]
                               ;['recur :convex.core.api/recur]
                               ;['reduce :convex.core.api/reduce]
                               ;['reduced :convex.core.api/reduced]
                               ['rem :convex.core.api/rem]
                               ['return :convex.core.api/return]
                               ['rollback :convex.core.api/rollback]
                               ;['schedule :convex.core.api/schedule]
                               ;['schedule* :convex.core.api/schedule*]
                               ;['second :convex.core.api/second]
                               ;['set :convex.core.api/set]
                               ['set! :convex.core.api/set!]
                               ['set* :convex.core.api/set*]
                               ;['set-controller :convex.core.api/set-controller]
                               ;['set-holding :convex.core.api/set-holding]
                               ['set-key :convex.core.api/set-key]
                               ;['set-memory :convex.core.api/set-memory]
                               ['set? :convex.core.api/set?]
                               ['signum :convex.core.api/signum]
                               ['sqrt :convex.core.api/sqrt]
                               ;['stake :convex.core.api/stake]
                               ['str :convex.core.api/str]
                               ['str? :convex.core.api/str?]
                               ['subset? :convex.core.api/subset?]
                               ['symbol :convex.core.api/symbol]
                               ['symbol? :convex.core.api/symbol?]
                               ['syntax :convex.core.api/syntax]
                               ['syntax? :convex.core.api/syntax?]
                               ;['transfer :convex.core.api/transfer]
                               ;['transfer-memory :convex.core.api/transfer-memory]
                               ['undef :convex.core.api/undef]
                               ['undef* :convex.core.api/undef*]
                               ['union :convex.core.api/union]
                               ['unsyntax :convex.core.api/unsyntax]
                               ['values :convex.core.api/values]
                               ['vec :convex.core.api/vec]
                               ['vector :convex.core.api/vector]
                               ['vector? :convex.core.api/vector?]
                               ['when :convex.core.api/when]
                               ['when-let :convex.core.api/when-let]
                               ['when-not :convex.core.api/when-not]
                               ['zero? :convex.core.api/zero?]]

          :convex.core.api/*   (call '*
                                     [:* :convex/number])
          :convex.core.api/+   (call '+
                                     [:* :convex/number])
          :convex.core.api/-   (call '-
                                     [:+ :convex/number])
          :convex.core.api/div (call '/
                                     [:+ :convex/number])


          :convex.core.api/<               (call '<
                                                 [:* :convex/number])
          :convex.core.api/<=              (call '<=
                                                 [:* :convex/number])
          :convex.core.api/=               (call '=
                                                 [:* :convex/data])
          :convex.core.api/==              (call '==
                                                 [:* :convex/number])
          :convex.core.api/>               (call '<
                                                 [:* :convex/number])
          :convex.core.api/>=              (call '>=
                                                 [:* :convex/number])
          :convex.core.api/abs             (call 'abs
                                                 :convex/number)
          ;:convex.core.api/accept          (call 'accept
          :convex.core.api/account         (call 'account
                                                 :convex/address)
          :convex.core.api/account?        (call 'account?
                                                 :convex/data)
          ;; TODO. Improve
          :convex.core.api/actor           (call 'actor
                                                 [:cat
                                                  :convex/arg+
                                                  :convex/data])
          :convex.core.api/actor?          (call 'actor?
                                                 :convex/data)
          :convex.core.api/address         (call 'address
                                                 :convex.cast/address)
          :convex.core.api/address?        (call 'address?
                                                 :convex/data)
          :convex.core.api/and             (call 'and
                                                 [:* :convex/data])
          ;:convex.core.api/apply           (call 'apply
          :convex.core.api/assert          (call 'assert
                                                 [:and
                                                  :convex/data
                                                  [:not [:enum
                                                         'false
                                                         'nil]]])
          ;:convex.core.api/assoc           (call 'assoc
          ;:convex.core.api/assoc-in        (call 'assoc-in
          ;; TODO. Should be castable to address
          :convex.core.api/balance         (call 'balance
                                                 :convex/address)
          :convex.core.api/blob            (call 'blob
                                                 :convex.cast/blob)
          ;; TODO. Should be variadic like `hash-map`
          ;; :convex.core.api/blob-map        (call 'blob-map
          ;;                                        nil)
          :convex.core.api/blob?           (call 'blob?
                                                 :convex/data)
          :convex.core.api/boolean         (call 'boolean
                                                 :convex/data)
          :convex.core.api/boolean?        (call 'boolean?
                                                 :convex/data)
          ;; TODO. Should be restricted to number only
          :convex.core.api/byte            (call 'byte
                                                 :convex/number)
          ;:convex.core.api/call            (call 'call
          ;:convex.core.api/call*           (call 'call*
          :convex.core.api/ceil            (call 'ceil
                                                 :convex/number)
          ;; TODO. Should be number only
          :convex.core.api/char            (call 'char
                                                 :convex/number)
          :convex.core.api/coll?           (call 'coll?
                                                 :convex/data)
          ;; TODO. Make more complex
          :convex.core.api/compile         (call 'compile
                                                 :convex/data)
          :convex.core.api/concat          (call 'concat
                                                 :convex/collection)
          :convex.core.api/cond            (call 'cond
                                                 [:* :convex/data])
          ;:convex.core.api/conj            (call 'conj
          :convex.core.api/cons            (call 'cons
                                                 [:cat
                                                  :convex/data
                                                  :convex/collection])
          :convex.core.api/contains-key?   (call 'contains-key?
                                                 [:cat
                                                  :convex/collection
                                                  :convex/data])
          :convex.core.api/count           (call 'count
                                                 :convex/collection)
          :convex.core.api/create-account  (call 'create-account
                                                 [:or
                                                  :convex/blob-32
                                                  :convex/hexstring-32])
          :convex.core.api/dec             (call 'dec
                                                 :convex/long)
          :convex.core.api/def             (call 'def
                                                 [:cat
                                                  :convex/symbol
                                                  :convex/data])
          ;; TODO. Make more interesting
          :convex.core.api/defactor        (call 'defactor
                                                 [:cat
                                                  :convex/symbol
                                                  :convex/arg+
                                                  :convex/data])
          ;:convex.core.api/defexpander     (call 'defexpander
          :convex.core.api/defined?        (call 'defined?
                                                 :convex/symbol)
          :convex.core.api/defmacro        (call 'defmacro
                                                 [:cat
                                                  :convex/symbol
                                                  :convex/arg+
                                                  ;; TODO. Improve
                                                  :convex/data])
          :convex.core.api/defn            (call 'defn
                                                 [:cat
                                                  :convex/symbol
                                                  :convex/arg+
                                                  ;; TODO. Improve
                                                  :convex/data])
          ;:convex.core.api/deploy          (call 'deploy
          :convex.core.api/difference      (call 'difference
                                                 [:+ [:or
                                                      :convex/nil
                                                      :convex/set]])
          :convex.core.api/disj            (call 'disj
                                                 [:cat
                                                  [:or
                                                   :convex/nil
                                                   :convex/set]
                                                  :convex/data])
          :convex.core.api/dissoc          (call 'dissoc
                                                 [:cat
                                                  [:alt
                                                   [:cat
                                                    [:or
                                                     :convex/map
                                                     :convex/nil]
                                                    [:+ :convex/data]]
                                                   [:cat
                                                    [:= '(blob-map)]
                                                    [:+ :convex/blob]]]])

          ;; TODO.
          ;; :convex.core.api/do              (call 'do

          :convex.core.api/doc             (call 'doc
                                                 :convex/symbol)
          :convex.core.api/dotimes         (call 'dotimes
                                                 [:cat
                                                  [:tuple
                                                   :convex/symbol
                                                   :convex/number]
                                                  ;; TODO. Improve
                                                  :convex/data])
          ;; TODO. Only real number? Eg. Not chars nor booleans
          :convex.core.api/double          (call 'double
                                                 :convex/number)
          :convex.core.api/empty           (call 'empty
                                                 :convex/collection)
          :convex.core.api/empty?          (call 'empty?
                                                 :convex/data)
          :convex.core.api/encoding        (call 'encoding
                                                 :convex/data)

          ;; TODO.
          ;; :convex.core.api/eval            (call 'eval
          ; ;:convex.core.api/eval-as         (call 'eval-as

          :convex.core.api/exp             (call 'exp
                                                 :convex/number)

          ;; TODO.
          ;; :convex.core.api/expand          (call 'expand
          ;; :convex.core.api/expander        (call 'expander

          ;; TODO. Should be only symbols
          :convex.core.api/export          (call 'export
                                                 [:* :convex/data])

          :convex.core.api/exports?        (call 'exports?
                                                 [:cat
                                                  :convex/address
                                                  :convex.quoted/symbol])
          ;:convex.core.api/fail            (call 'fail

          ;; TODO. Blobs etc...
          :convex.core.api/first           (call 'first
                                                 [:and
                                                  :convex/collection
                                                  [:fn not-empty]])
          :convex.core.api/floor           (call 'floor
                                                  :convex/number)
          :convex.core.api/fn              (call 'fn
                                                 ;; TODO. Improve
                                                 [:cat
                                                  :convex/arg+
                                                  :convex/data])
          :convex.core.api/fn?             (call 'fn?
                                                 [:or
                                                  :convex/data
                                                  :convex.core.api/fn])

          ;; TODO.
          ;; :convex.core.api/for             (call 'for

          :convex.core.api/get             (call 'get
                                                 [:cat
                                                  :convex/collection
                                                  :convex/data
                                                  [:? :convex/data]])
          :convex.core.api/get-holding     (call 'get-holding
                                                 :convex/address)

          ;; TODO.
          ;; :convex.core.api/get-in          (call 'get-in

          :convex.core.api/halt            (call 'halt
                                                 :convex/data)
          :convex.core.api/hash            (call 'hash
                                                 :convex.cast/blob)
          :convex.core.api/hash-map        (call 'hash-map
                                                 [:* [:cat
                                                      :convex/data
                                                      :convex/data]])
          :convex.core.api/hash-set        (call 'hash-set
                                                 [:* :convex/data])
          :convex.core.api/hash?           (call 'hash?
                                                 :convex/data)
          :convex.core.api/identity        (call 'identity
                                                 :convex/data)
          :convex.core.api/if              (call 'if
                                                 ;; TODO. Improve
                                                 [:cat
                                                  :convex/data
                                                  :convex/data
                                                  [:? :convex/data]])
          :convex.core.api/if-let          (call 'if-let
                                                 ;; TODO. Add destructuring
                                                 [:cat
                                                  [:tuple
                                                   :convex/symbol
                                                   :convex/data]
                                                  :convex/data
                                                  [:? :convex/data]])
          ;:convex.core.api/import          (call 'import
          :convex.core.api/inc             (call 'inc
                                                 :convex/long)
          :convex.core.api/intersection    (call 'intersection
                                                 [:+ [:or
                                                      :convex/nil
                                                      :convex/set]])
          ;; TODO. Should accept arity 1?
          :convex.core.api/into            (call 'into
                                                 [:alt
                                                  [:cat
                                                   [:or
                                                    :convex/list
                                                    :convex/set
                                                    :convex/vector]
                                                   :convex/collection]
                                                  [:cat
                                                   :convex/map
                                                   [:vector
                                                    [:tuple
                                                     :convex/data
                                                     :convex/data]]]
                                                  [:cat
                                                   [:= '(blob-map)]
                                                   [:vector
                                                    [:tuple
                                                     :convex/blob
                                                     :convex/data]]]])
          :convex.core.api/keys            (call 'keys
                                                 :convex/map)
          :convex.core.api/keyword         (call 'keyword
                                                 :convex.cast/keyword)
          :convex.core.api/keyword?        (call 'keyword?
                                                 :convex/data)
          ;; TODO. Blobs etc
          :convex.core.api/last            (call 'last
                                                 [:and
                                                  :convex/collection
                                                  [:fn not-empty]])
          :convex.core.api/let             (call 'let
                                                 ;; TODO. Destructuring
                                                 [:cat
                                                  [:and
                                                   [:*
                                                    {:gen/fmap vec}
                                                    [:cat
                                                     :convex/symbol
                                                     :convex/data]]
                                                   vector?]
                                                  ;; TODO. Improve
                                                  :convex/data])
          :convex.core.api/list            (call 'list
                                                 [:* :convex/data])
          :convex.core.api/list?           (call 'list?
                                                 :convex/data)
          :convex.core.api/log             (call 'log
                                                 :convex/data)
          :convex.core.api/long            (call 'long
                                                 :convex.cast/long)
          :convex.core.api/long?           (call 'long?
                                                 :convex/data)
          :convex.core.api/lookup          (call 'lookup
                                                 :convex.quoted/symbol)
          :convex.core.api/lookup-syntax   (call 'lookup-syntax
                                                 :convex.quoted/symbol)

          ;; TODO.
          ;; :convex.core.api/loop            (call 'loop
          ;; :convex.core.api/macro           (call 'macro

          :convex.core.api/map             (call 'map
                                                 [:cat
                                                  [:= 'identity]
                                                  :convex/collection])
          :convex.core.api/map?            (call 'map?
                                                 :convex/data)

          ;; TODO.
          ;; :convex.core.api/mapcat          (call 'mapcat

          :convex.core.api/mapv            (call 'mapv
                                                 [:cat
                                                  [:= 'identity]
                                                  :convex/collection])
          :convex.core.api/max             (call 'max
                                                 [:* :convex/number])
          :convex.core.api/merge           (call 'merge
                                                 [:* [:or
                                                      :convex/map
                                                      :convex/nil]])
          :convex.core.api/meta            (call 'meta
                                                 :convex/data)
          :convex.core.api/min             (call 'min
                                                 [:* :convex/number])
          :convex.core.api/mod             (call 'mod
                                                 [:cat
                                                  :convex/long
                                                  [:and
                                                   :convex/long
                                                   [:not [:= 0]]]])
          :convex.core.api/name            (call 'name
                                                 :convex.cast/symbolic)
          :convex.core.api/next            (call 'next
                                                 :convex/collection)
          :convex.core.api/nil?            (call 'nil?
                                                 :convex/data)
          :convex.core.api/not             (call 'not
                                                 :convex/data)

          ;; TODO.
          ;; :convex.core.api/nth             (call 'nth

          :convex.core.api/number?         (call 'number?
                                                 :convex/data)
          :convex.core.api/or              (call 'or
                                                 [:* :convex/data])
          :convex.core.api/pow             (call 'pow
                                                 [:cat
                                                  :convex/number
                                                  :convex/number])

          ;; TODO.
          ;; :convex.core.api/query           (call 'query

          :convex.core.api/quot            (call 'quot
                                                 [:cat
                                                  :convex/long
                                                  [:and
                                                   :convex/long
                                                   [:not [:= 0]]]])
          :convex.core.api/quote           (call 'quote
                                                 :convex/data)

          ;:convex.core.api/recur           (call 'recur

          ;; TODO.
          ;; :convex.core.api/reduce          (call 'reduce
          ;; :convex.core.api/reduced         (call 'reduced

          :convex.core.api/rem             (call 'rem
                                                 [:cat
                                                  :convex/long
                                                  [:and
                                                   :convex/long
                                                   [:not [:= 0]]]])
          :convex.core.api/return          (call 'return
                                                 :convex/data)
          :convex.core.api/rollback        (call 'rollback
                                                 :convex/data)

          ;; TODO.
          ;; :convex.core.api/schedule        (call 'schedule
          ;; :convex.core.api/schedule*       (call 'schedule*

          ;; TOOD.
          ;; :convex.core.api/second          (call 'second

          ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/77
          ;;
          ;; :convex.core.api/set             (call 'set
          ;;                                        :convex/collection)

          :convex.core.api/set!            (call 'set!
                                                 [:cat
                                                  :convex/symbol
                                                  :convex/data])
          :convex.core.api/set*            (call 'set*
                                                 [:cat
                                                  :convex.quoted/symbol
                                                  :convex/data])
          ;; TODO.
          ;; :convex.core.api/set-controller  (call 'set-controller
          ;; :convex.core.api/set-holding     (call 'set-holding

          :convex.core.api/set-key         (call 'set-key
                                                 [:or
                                                  :convex/blob-32
                                                  :convex/hexstring-32])

          ;; TODO.
          ;; :convex.core.api/set-memory      (call 'set-memory

          :convex.core.api/set?            (call 'set?
                                                 :convex/data)
          :convex.core.api/signum          (call 'signum
                                                 :convex/number)
          :convex.core.api/sqrt            (call 'sqrt
                                                 :convex/number)

          ;; TODO.
          ;;:convex.core.api/stake           (call 'stake

          :convex.core.api/str             (call 'str
                                                 [:* :convex/data])

          :convex.core.api/str?            (call 'str?
                                                 :convex/data)
          :convex.core.api/subset?         (call 'subset?
                                                 [:repeat
                                                  {:max 2
                                                   :min 2}
                                                  [:or
                                                   :convex/nil
                                                   :convex/set]])
          :convex.core.api/symbol          (call 'symbol
                                                 :convex.cast/symbol)
          :convex.core.api/symbol?         (call 'symbol?
                                                 :convex/data)
          :convex.core.api/syntax          (call 'syntax
                                                 [:cat
                                                  :convex/data
                                                  [:? :convex/map]])
          :convex.core.api/syntax?         (call 'syntax?
                                                 :convex/data)

          ;; TODO.
          ;; :convex.core.api/transfer        (call 'transfer
          ;; :convex.core.api/transfer-memory (call 'transfer-memory

          :convex.core.api/undef           (call 'undef
                                                 :convex/symbol)
          :convex.core.api/undef*          (call 'undef*
                                                 :convex.quoted/symbol)
          :convex.core.api/union           (call 'union
                                                 [:* [:or
                                                      :convex/nil
                                                      :convex/set]])
          :convex.core.api/unsyntax        (call 'unsyntax
                                                 :convex/data)
          :convex.core.api/values          (call 'values
                                                 :convex/map)
          :convex.core.api/vec             (call 'vec
                                                 :convex/collection)
          :convex.core.api/vector          (call 'vector
                                                 [:* :convex/data])
          :convex.core.api/vector?         (call 'vector?
                                                 :convex/data)
          :convex.core.api/when            (call 'when
                                                 [:cat
                                                  :convex/data
                                                  :convex/data])
          :convex.core.api/when-let        (call 'when-let
                                                 [:cat
                                                  [:tuple
                                                   :convex/symbol
                                                   :convex/data]
                                                  :convex/data])
          :convex.core.api/when-not        (call 'when-not
                                                 [:cat
                                                  :convex/data
                                                  :convex/data])
          :convex.core.api/zero?           (call 'zero?
                                                 :convex/data)
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
          ;  :list                (malli/-collection-schema (fn [prop+ [child]]
          ;                                                   {:empty           '()
          ;                                                    :pred            seq?
          ;                                                    :type            :list
          ;                                                    :type-properties {:error/message "should be a list"
          ;                                                                      :gen/fmap      #(or (list* %)
          ;                                                                                          '())
          ;                                                                      :gen/schema    [:vector
          ;                                                                                      prop+
          ;                                                                                      child]}}))
          :convex/address      [:and
                                {:gen/fmap   $.lisp/address
                                 :gen/schema pos-int?}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches #"#\d+"
                                                        (name sym))))]]
          :convex/arg+         [:vector :convex/symbol]
          :convex/blob         [:and
                                {:gen/fmap   $.lisp/blob
                                 :gen/schema :convex/hexstring}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex
                                                        (name sym))))]]
		  :convex/blob-8	   [:and
								{:gen/fmap   $.lisp/blob
                                 :gen/schema :convex/hexstring-8}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex-8
                                                        (name sym))))]]
		  :convex/blob-32	   [:and
								{:gen/fmap   $.lisp/blob
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
                                :convex/vector
                                :convex.quoted/symbol]
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
          :convex/list         [:cat
                                [:= 'list]
                                [:* [:ref :convex/data]]]
                               ; [:list [:ref :convex/data]]
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
                                   (not (or ($.lisp/address? x)
                                            ($.lisp/blob? x))))]]
          :convex/truthy       [:and
                                :convex/data
                                [:not :convex/falsy]]
          :convex/vector       [:vector [:ref :convex/data]]
          :convex.quoted/symbol (quoted :convex/symbol)
)))



(defn castable

  ""

  [registry]

  (assoc registry
         :convex.cast/address   [:or
                                 :convex/address
                                 :convex/blob-8
                                 :convex/hexstring-8
                                 :convex/long]
         :convex.cast/blob      [:or
                                 :convex/address
                                 :convex/blob
                                 :convex/hexstring]
         :convex.cast/keyword   :convex.cast/symbolic
         :convex.cast/long      [:or
                                 :convex/boolean
                                 :convex/char
                                 :convex/double
                                 :convex/long]
         :convex.cast/symbol    :convex.cast/symbolic
         :convex.cast/symbolic  [:or
                                 :convex/keyword
                                 [:and
                                  :convex/string
                                  [:fn not-empty]]
                                 :convex.quoted/symbol]
         ))



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
       castable
       core
       data)))
