(ns convex.lisp.test.core

  "Testing Convex Core by generating Convex Lisp forms as Clojure data, converting them to source,
   and evaling."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.hex                 :as $.hex]
            [convex.lisp.schema              :as $.schema]
            [convex.lisp.test.util           :as $.test.util]))


;;;;;;;;;; Default values


(def max-size-coll

  ""

  5)


;;;;;;;;;; Building common types of properties


(defn prop-pred-data-false

  ""


  ([core-symbol schema-without]

   (prop-pred-data-false core-symbol
                         schema-without
                         nil))


  ([core-symbol schema-without clojure-pred]

   (tc.prop/for-all* [($.test.util/generator-data-without schema-without)]
                     (if clojure-pred
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred core-symbol
                                                          x)]
                           ($.test.util/prop+

                             "Always returns false"
                             (not x-2)

                             "Consistent with Clojure"
                             (= x-2
                                (clojure-pred x)))))
                       (fn [x]
                         (not ($.test.util/eval-pred core-symbol
                                                     x)))))))



(defn prop-pred-data-true

  ""


  ([core-symbol schema]

   (prop-pred-data-true core-symbol
                        schema
                        nil))


  ([core-symbol schema clojure-pred]

   (tc.prop/for-all* [($.test.util/generator schema)]
                     (if clojure-pred
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred core-symbol
                                                          x)]
                           ($.test.util/prop+

                             "Always returns true"
                             x-2

                             "Consistent with Clojure"
                             (= x-2
                                (clojure-pred x)))))
                       (fn [x]
                         ($.test.util/eval-pred core-symbol
                                                x))))))



;;;;;;;;;;


(tc.ct/defspec -account-inexistant

  (tc.prop/for-all* [($.test.util/generator [:and
                                             :int
                                             [:>= 50]])]
                      (fn [x]
                        ($.test.util/prop+

                          "Account does not exist"
                          (false? ($.test.util/eval (list 'account?
                                                          x)))

                          "Actor does not exist"
                          (false? ($.test.util/eval (list 'actor?
                                                          x)))))))



(tc.ct/defspec address?--

  (tc.prop/for-all* [($.test.util/generator :convex/address)]
                    (fn [x]
                      ($.test.util/eval (list 'address?
                                              x)))))



(tc.ct/defspec address?--false

  ;; TODO. Also test `actor?`? See #74.

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator-data-without #{:convex/address
                                                           :convex/boolean  ;; TODO. See #73
                                                           :convex/char     ;; TODO. See #68
                                                           :convex/double
                                                           :convex/long})]
                    (fn [x]
                      (false? ($.test.util/eval ($/templ {'?x x}
                                                         '(address? (quote ?x))))))))



(tc.ct/defspec blob?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'blob?
                        #{:convex/blob}))



(tc.ct/defspec blob?--true

  (prop-pred-data-true 'blob?
                       :convex/blob))



(t/deftest blob-map--

  (t/is (map? ($.test.util/eval '(blob-map)))))



(tc.ct/defspec boolean?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'boolean?
                        #{:convex/boolean}
                        boolean?))



(t/deftest boolean?--true

  (t/is (true? ($.test.util/eval true))
        "True")

  (t/is (false? ($.test.util/eval false))
        "False"))



(tc.ct/defspec coll?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'coll?
                        #{:convex/list
                          :convex/map
                          :convex/set
                          :convex/vector}
                        coll?))



(tc.ct/defspec coll?--true

  {:max-size max-size-coll}

  (prop-pred-data-true 'coll?
                       :convex/collection
                       coll?))



(defn -new-account

  ""

  [ctx actor?]

  ($.test.util/prop+

    "Address is interned"
    ($.test.util/valid? :convex/address
                        ($.test.util/eval ctx
                                          'addr))

    "(account?)"
    ($.test.util/eval ctx
                      '(account? addr))

    "(actor?)"
    (actor? ($.test.util/eval ctx
                              '(actor? addr)))

    "(address?)"
    ($.test.util/eval ctx
                      '(address? addr))

    "(balance)"
    (zero? ($.test.util/eval ctx
                             '(balance addr)))

    "(get-holding)"
    (nil? ($.test.util/eval ctx
                            '(get-holding addr)))

    "(account) and comparing with *state*"
    (let [[addr-long
           account]  ($.test.util/eval ctx
                                       '[(long addr)
                                         (account addr)])]
      (= account
         ($.test.util/eval ctx
                           ($/templ {'?addr addr-long}
                                    '(get-in *state*
                                             [:accounts
                                              ?addr])))))))


(tc.ct/defspec create-account--

  (tc.prop/for-all* [($.test.util/generator :convex/hexstring-32)]
                    (fn [x]
                      (let [ctx ($.test.util/eval-context ($/templ {'?hexstring x}
                                                                   '(def addr
                                                                         (create-account ?hexstring))))]
                        (-new-account ctx
                                      false?)))))



(tc.ct/defspec deploy--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator :convex/data)]
                    (fn [x]
                      (let [ctx ($.test.util/eval-context ($/templ {'?data x}
                                                                   '(def addr
                                                                         (deploy (quote '?data)))))]
                        (-new-account ctx
                                      true?)))))



(defn -eval-fn?

  "Returns true if the given form evals to a function."

  [form]

  ($.test.util/eval (list 'fn?
                          form)))



(defn -eval-fn

  "Returns true if calling the given function `form` with the given `arg+` returns `ret`."

  [form arg+ ret]

  ($.test.util/eval (list '=
                          ret
                          (list* form
                                 arg+))))


(defn -eval-fn-def

  "Like [[-eval-fn]] but interns the function first using `def`.

   Also tests `fn?`."
  

  [form arg+ ret]

  ($.test.util/result+ ($.test.util/eval ($/templ {'?call (list* 'f
                                                                 arg+)
                                                   '?fn   form
                                                   '?ret  ret}
                                                  '(do
                                                     (def f
                                                          ?fn)
                                                     [(fn? f)
                                                      (= ?ret
                                                         ?call)])))
                       ["Fn?"
                        "Equal"]))



(defn -eval-fn-let

  "Like [[-eval-fn]] but binds the function locally using `let`.

   Also tests `fn?`."

  [form arg+ ret]

  ($.test.util/result+ ($.test.util/eval ($/templ {'?call (list* 'f
                                                                 arg+)
                                                   '?fn   form
                                                   '?ret  ret}
                                                  '(let [f ?fn]
                                                     [(fn? f)
                                                      (= ?ret
                                                         ?call)])))
                       ["Fn?"
                        "Equal"]))



(tc.ct/defspec fn--arg-0

  ;; Calling no-arg functions.

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator :convex/data)]
                    (fn [x]
                      (let [x-2     (list 'quote
                                          x)
                            fn-form (list 'fn
                                          []
                                          x-2)
                            templ   (fn [form]
                                      ($/templ {'?fn fn-form
                                                '?x  x-2}
                                               form))]
                        ($.test.util/prop+

                          "Function?"
                          (-eval-fn? fn-form)

                          "Direct"
                          (-eval-fn fn-form
                                    nil
                                    x-2)
  
                          "Interned"
                          (-eval-fn-def fn-form
                                        nil
                                        x-2)
  
                          "Let"
                          (-eval-fn-let fn-form
                                        nil
                                        x-2))))))



(tc.ct/defspec fn--arg-fixed

  ;; Calling functions with a fixed number of arguments.

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator-binding+ 1)]
                    (fn [x]
                      (let [arg+     (mapv #(list 'quote
                                                  (second %))
                                           x)
                            binding+ (mapv first
                                           x)
                            fn-form  (list 'fn
                                           binding+
                                           binding+)]
                        ($.test.util/prop+

                          "Function?"
                          (-eval-fn? fn-form)

                          "Direct"
                          (-eval-fn fn-form
                                    arg+
                                    arg+)

                          "Interned"
                          (-eval-fn-def fn-form
                                        arg+
                                        arg+)

                          "Let"
                          (-eval-fn-let fn-form
                                        arg+
                                        arg+))))))



(tc.ct/defspec fn--arg-variadic

  ;; Calling functions with a variadic number of arguments.

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator-binding+ 1)]
                    (fn [x]
                      (let [arg+       (mapv #(list 'quote
                                                    (second %))
                                             x)
                            binding+   (mapv first
                                             x)
                            pos-amper  (rand-int (count binding+))
                            binding-2+ (vec (concat (take pos-amper
                                                          binding+)
                                                    ['&]
                                                    (drop pos-amper
                                                          binding+)))
                            fn-form    (list 'fn
                                             binding-2+
                                             binding+)
                            ret        (update arg+
                                               pos-amper
                                               vector)]
                        ($.test.util/prop+

                          "Function?"
                          (-eval-fn? fn-form)

                          "Direct"
                          (-eval-fn fn-form
                                    arg+
                                    ret)

                          "Interned"
                          (-eval-fn-def fn-form
                                        arg+
                                        ret)


                          "Let"
                          (-eval-fn-let fn-form
                                        arg+
                                        ret)

                          "1 argument less"
                          (let [ret-1  (assoc arg+
                                              pos-amper
                                              [])
                                arg-1+ (into []
                                             (concat (take pos-amper
                                                           arg+)
                                                     (drop (inc pos-amper)
                                                           arg+)))]
                            ($.test.util/prop+

                              "Direct"
                              (-eval-fn fn-form
                                        arg-1+
                                        ret-1)


                              "Interned"
                              (-eval-fn-def fn-form
                                            arg-1+
                                            ret-1)


                              "Let"
                              (-eval-fn-let fn-form
                                            arg-1+
                                            ret-1)))

                          "Extra argument"
                          (let [ret+1  (update arg+
                                               pos-amper
                                               #(vector 42
                                                        %))
                                arg+1+ (into []
                                             (concat (take pos-amper
                                                           arg+)
                                                     [42]
                                                     (drop pos-amper
                                                           arg+)))]
                            ($.test.util/prop+

                              "Direct"
                              (-eval-fn fn-form
                                        arg+1+
                                        ret+1)

                              "Interned"
                              (-eval-fn-def fn-form
                                            arg+1+
                                            ret+1)

                              "Let"
                              (-eval-fn-let fn-form
                                            arg+1+
                                            ret+1))))))))



(tc.ct/defspec hash-map--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:+ [:cat
                                                 :convex/data
                                                 :convex/data]])]
                    (fn [x]
                      (map? ($.test.util/eval (list* 'hash-map
                                                     (map #(list 'quote
                                                                 %)
                                                          x)))))))



(t/deftest hash-map--no-arg

  (t/is (= {}
           ($.test.util/eval '(hash-map)))))



(tc.ct/defspec hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/data])]
                    (fn [x]
                      (set? ($.test.util/eval (list* 'hash-set
                                                     (map #(list 'quote
                                                                 %)
                                                          x)))))))



(t/deftest hash-set--no-arg

  (t/is (= #{}
           ($.test.util/eval '(hash-set)))))



(tc.ct/defspec keyword?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'keyword?
                        #{:convex/keyword}
                        keyword?))



(tc.ct/defspec keyword?--true

  (prop-pred-data-true 'keyword?
                       :convex/keyword
                       keyword?))



;; TODO. `log`, about logging



(tc.ct/defspec list--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
		      							    :convex/data])]
		      	    (fn [x]
		      	      (= (apply list
		      	      	   	    x)
		      	         ($.test.util/eval (list* 'list
 		      	      							  (map #(list 'quote
                                                              %)
                                                       x)))))))



(tc.ct/defspec list?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'list?
                        #{:convex/list}
                        list?))



(tc.ct/defspec list?--true

  {:max-size max-size-coll}

  (prop-pred-data-true 'list?
                       :convex/list
                       list?))



(tc.ct/defspec long?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'long?
                        #{:convex/long}
                        int?))



(tc.ct/defspec long?--true

  (prop-pred-data-true 'long?
                       :convex/long
                       int?))



(tc.ct/defspec map?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'map?
                        #{:convex/map}
                        map?))



(tc.ct/defspec map?--true

  {:max-size max-size-coll}

  (prop-pred-data-true 'map?
                       :convex/map
                       map?))



(tc.ct/defspec nil?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'nil?
                        #{:convex/nil}
                        nil?))



(t/deftest nil?--true

  (t/is (true? (nil? ($.test.util/eval nil))))

  (t/is (true? (nil? ($.test.util/eval '(do nil))))))



(tc.ct/defspec number?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'number?
                        #{:convex/boolean ;; TODO. See #73.
                          :convex/char    ;; TODO. See #68.
                          :convex/double
                          :convex/long}
                        number?))



(tc.ct/defspec number?--true

  (prop-pred-data-true 'number?
                       :convex/number
                       number?))



(tc.ct/defspec set?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'set?
                        #{:convex/set}
                        set?))



(tc.ct/defspec set?--true

  {:max-size max-size-coll}

  (prop-pred-data-true 'set?
                       :convex/set
                       set?))



(tc.ct/defspec str?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'str?
                        #{:convex/string}
                        string?))



(tc.ct/defspec str?--true

  (prop-pred-data-true 'str?
                       :convex/string
                       string?))



(tc.ct/defspec symbol?--true

  (prop-pred-data-true 'symbol?
                       :convex/symbol
                       symbol?))



(tc.ct/defspec symbol?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'symbol?
                        #{:convex/symbol}
                        (partial $.test.util/valid?
                                 :convex/symbol)))



(tc.ct/defspec vector--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
		      							   :convex/data])]
		      	    (fn [x]
		      	      (= (apply vector
                                x)
		      	         ($.test.util/eval (list* 'vector
 		      	      							  (map #(list 'quote
                                                               %)
                                                       x)))))))

(tc.ct/defspec vector?--false

  {:max-size max-size-coll}

  (prop-pred-data-false 'vector?
                        #{:convex/vector}
                        vector?))



(tc.ct/defspec vector?--true

  {:max-size max-size-coll}

  (prop-pred-data-true 'vector?
                       :convex/vector
                       vector?))




;; Creating collections

; blob-map
; hash-map
; hash-set
; list
; vector



;; Associative operations (with lists as well)

; assoc
; assoc-in
; contains-key?
; get
; get-in
; keys



;; Map operations

; merge
; values



;; Misc operations

; conj
; count
; empty
; empty?
; first
; into
; last
; next
; nth
; reduce
; reduced
; second


;; Producing vectors

; concat
; map && mapv ???



;; Producing lists

; cons



;; Set operations

; difference
; disj
; intersection
; subset
; union

