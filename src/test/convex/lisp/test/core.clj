(ns convex.lisp.test.core

  "Testing Convex Core type predicates."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.util           :as $.test.util]))


;;;;;;;;;; Default values


(def max-size-coll

  ""

  5)


;;;;;;;;;;


(tc.ct/defspec -account-inexistant

  (tc.prop/for-all* [($.test.util/generator [:and
                                             :int
                                             [:>= 50]])]
                      (fn [x]
                        ($.test.util/prop+

                          "Account does not exist"
                          (false? ($.test.eval/form (list 'account?
                                                          x)))

                          "Actor does not exist"
                          (false? ($.test.eval/form (list 'actor?
                                                          x)))))))



(t/deftest blob-map--

  (t/is (map? ($.test.eval/form '(blob-map)))))



(defn -new-account

  ""

  [ctx actor?]

  ($.test.util/prop+

    "Address is interned"
    ($.test.util/valid? :convex/address
                        ($.test.eval/form ctx
                                          'addr))

    "(account?)"
    ($.test.eval/form ctx
                      '(account? addr))

    "(actor?)"
    (actor? ($.test.eval/form ctx
                              '(actor? addr)))

    "(address?)"
    ($.test.eval/form ctx
                      '(address? addr))

    "(balance)"
    (zero? ($.test.eval/form ctx
                             '(balance addr)))

    "(get-holding)"
    (nil? ($.test.eval/form ctx
                            '(get-holding addr)))

    "(account) and comparing with *state*"
    (let [[addr-long
           account]  ($.test.eval/form ctx
                                       '[(long addr)
                                         (account addr)])]
      (= account
         ($.test.eval/form ctx
                           ($/templ {'?addr addr-long}
                                    '(get-in *state*
                                             [:accounts
                                              ?addr])))))))



(tc.ct/defspec create-account--

  (tc.prop/for-all* [($.test.util/generator :convex/hexstring-32)]
                    (fn [x]
                      (let [ctx ($.test.eval/form->context ($/templ {'?hexstring x}
                                                                    '(def addr
                                                                          (create-account ?hexstring))))]
                        (-new-account ctx
                                      false?)))))



(tc.ct/defspec deploy--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator :convex/data)]
                    (fn [x]
                      (let [ctx ($.test.eval/form->context ($/templ {'?data x}
                                                                    '(def addr
                                                                          (deploy (quote '?data)))))]
                        (-new-account ctx
                                      true?)))))



(defn -eval-fn?

  "Returns true if the given form evals to a function."

  [form]

  ($.test.eval/form (list 'fn?
                          form)))



(defn -eval-fn

  "Returns true if calling the given function `form` with the given `arg+` returns `ret`."

  [form arg+ ret]

  ($.test.eval/form (list '=
                          ret
                          (list* form
                                 arg+))))


(defn -eval-fn-def

  "Like [[-eval-fn]] but interns the function first using `def`.

   Also tests `fn?`."
  

  [form arg+ ret]

  ($.test.util/result+ ($.test.eval/form ($/templ {'?call (list* 'f
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

  ($.test.util/result+ ($.test.eval/form ($/templ {'?call (list* 'f
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
                      (map? ($.test.eval/form (list* 'hash-map
                                                     (map #(list 'quote
                                                                 %)
                                                          x)))))))



(t/deftest hash-map--no-arg

  (t/is (= {}
           ($.test.eval/form '(hash-map)))))



(tc.ct/defspec hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/data])]
                    (fn [x]
                      (set? ($.test.eval/form (list* 'hash-set
                                                     (map #(list 'quote
                                                                 %)
                                                          x)))))))



(t/deftest hash-set--no-arg

  (t/is (= #{}
           ($.test.eval/form '(hash-set)))))



;; TODO. `log`, about logging



(tc.ct/defspec list--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
		      							    :convex/data])]
		      	    (fn [x]
		      	      (= (apply list
		      	      	   	    x)
		      	         ($.test.eval/form (list* 'list
 		      	      							  (map #(list 'quote
                                                              %)
                                                       x)))))))



(tc.ct/defspec vector--

  {:max-size max-size-coll}

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
		      							   :convex/data])]
		      	    (fn [x]
		      	      (= (apply vector
                                x)
		      	         ($.test.eval/form (list* 'vector
 		      	      							  (map #(list 'quote
                                                               %)
                                                       x)))))))

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

