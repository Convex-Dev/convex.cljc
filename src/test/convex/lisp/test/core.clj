(ns convex.lisp.test.core

  "Testing Convex Core type predicates."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.schema         :as $.test.schema]
            [convex.lisp.test.util           :as $.test.util]))


;;;;;;;;;; Default values


(def max-size-coll

  ""

  5)


;;;;;;;;;;


(tc.ct/defspec -account-inexistant

  ($.test.prop/check [:and
                      :int
                      [:>= 50]]
                     (fn [x]
                       ($.test.prop/mult*

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

  ($.test.prop/mult*

    "Address is interned"
    ($.test.schema/valid? :convex/address
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

  ($.test.prop/check :convex/hexstring-32
                     (fn [x]
                       (let [ctx ($.test.eval/form->context ($/templ {'?hexstring x}
                                                                     '(def addr
                                                                           (create-account ?hexstring))))]
                         (-new-account ctx
                                       false?)))))



(tc.ct/defspec deploy--

  {:max-size max-size-coll}

  ($.test.prop/check :convex/data
                     (fn [x]
                       (let [ctx ($.test.eval/form->context ($/templ {'?data x}
                                                                     '(def addr
                                                                           (deploy (quote '?data)))))]
                         (-new-account ctx
                                       true?)))))



(tc.ct/defspec hash-map--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  {:max-size max-size-coll}

  ($.test.prop/check [:+ [:cat
                          :convex/data
                          :convex/data]]
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

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/data]
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

  ($.test.prop/check [:vector
                      {:min 1}
		      	      :convex/data]
		      	     (fn [x]
		      	       (= (apply list
		      	       	   	    x)
		      	          ($.test.eval/form (list* 'list
 		      	       							  (map #(list 'quote
                                                               %)
                                                        x)))))))



(tc.ct/defspec vector--

  {:max-size max-size-coll}

  ($.test.prop/check [:vector
                      {:min 1}
		      		  :convex/data]
		      	     (fn [x]
		      	       (= (apply vector
                                 x)
		      	          ($.test.eval/form (list* 'vector
 		      	       							  (map #(list 'quote
                                                                %)
                                                        x)))))))


;;;;;;;;;;


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

