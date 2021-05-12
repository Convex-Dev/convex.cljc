(ns convex.lisp.test.core.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Suites


(defn suite-new

  "Every new account, actor or user, must pass this suite.

   Assumes address is interned as `addr`."

  [ctx actor?]

  ($.test.prop/checkpoint*

    "Every new account must pass this suite"

    ($.test.prop/mult*

      "Address is interned"
      ($.test.schema/valid? :convex/address
                            ($.test.eval/result ctx
                                                'addr))
      "(account?)"
      ($.test.eval/result ctx
                          '(account? addr))
      "(actor?)"
      (actor? ($.test.eval/result ctx
                                  '(actor? addr)))
      "(address?)"
      ($.test.eval/result ctx
                          '(address? addr))
      "(balance)"
      (zero? ($.test.eval/result ctx
                                 '(balance addr)))
      "(get-holding)"
      (nil? ($.test.eval/result ctx
                                '(get-holding addr
                                              )))
      "(account) and comparing with *state*"
      (let [[addr-long
             account]  ($.test.eval/result ctx
                                           '[(long addr)
                                             (account addr)])]
        (= account
           ($.test.eval/result ctx
                               ($.form/templ {'?addr addr-long}
                                             '(get-in *state*
                                                      [:accounts
                                                       ?addr]))))))))


;;;;;;;;;; Tests


($.test.prop/deftest account-inexistant

  ($.test.prop/check [:and
                      :int
                      [:>= 50]]
                     (fn [x]
                       ($.test.prop/mult*

                         "Account does not exist"
                         (false? ($.test.eval/result (list 'account?
                                                           x)))

                         "Actor does not exist"
                         (false? ($.test.eval/result (list 'actor?
                                                           x)))))))



($.test.prop/deftest create-account--

  ($.test.prop/check :convex/hexstring-32
                     (fn [x]
                       (suite-new ($.test.eval/ctx ($.form/templ {'?hexstring x}
                                                                 '(def addr
                                                                       (create-account ?hexstring))))
                                  false?))))



($.test.prop/deftest transfer--

  ($.test.prop/check [:tuple
                      :convex/hexstring-32
                      [:double
                       {:max 1
                        :min 0}]]
                     (fn [[pubkey ratio]]
                       ($.test.prop/mult-result ($.test.eval/result
                                                  ($.form/templ {'?pubkey pubkey
                                                                 '?ratio  ratio}
                                                                '(do
                                                                   (let [balance-original *balance*
                                                                         addr             (create-account ?pubkey)
                                                                         amount           (floor (* ?ratio
                                                                                                    *balance*))]
                                                                     (transfer addr
                                                                               amount)
                                                                     [(== balance-original
                                                                          (+ *balance*
                                                                             amount))
                                                                      (== amount
                                                                          (balance addr))]))))
                                                ["Own balance has been correctly updated"
                                                 "Balance of receiver has been correctly updated"]))))
