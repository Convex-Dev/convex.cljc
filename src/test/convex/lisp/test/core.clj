(ns convex.lisp.test.core

  "Testing Convex core namespace."

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


;;;;;;;;;; TODO

;; `log`, about logging
