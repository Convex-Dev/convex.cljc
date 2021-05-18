(ns convex.lisp.test.core.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.test.eval   :as $.test.eval]
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

      "`account?`"
      ($.test.eval/result ctx
                          '(account? addr))

      "`actor?`"
      (actor? ($.test.eval/result ctx
                                  '(actor? addr)))

      "`address?`"
      ($.test.eval/result ctx
                          '(address? addr))

      "Balance is 0"
      ($.test.eval/result ctx
                          '(zero? (balance addr)))

      "`get-holding` returns nothing on a viring account"
      ($.test.eval/result ctx
                          '(nil? (get-holding addr)))

      "Comparing `account` with *state*"
      ($.test.eval/result ctx
                          '(= (account addr)
                              (get-in *state*
                                      [:accounts
                                       (long addr)]))))))


;;;;;;;;;; Tests


($.test.prop/deftest account-inexistant

  ($.test.prop/check [:and
                      :int
                      [:>= 50]]
                     (fn [x]
                       ($.test.prop/mult*

                         "Account does not exist (long)"
                         ($.test.eval/result* (not (account? ~x)))

                         "Account does not exist (address)"
                         ($.test.eval/result* (not (account (address ~x))))

                         "Actor does not exist (long)"
                         ($.test.eval/result* (not (actor? ~x)))

                         "Actor does not exist (address)"
                         ($.test.eval/result* (not (actor? (address ~x))))))))



($.test.prop/deftest create-account--

  ($.test.prop/check :convex/hexstring-32
                     (fn [hexstring]
                       (suite-new ($.test.eval/ctx* (def addr
                                                         (create-account ~hexstring)))
                                  false?))))



($.test.prop/deftest transfer--

  ($.test.prop/check [:tuple
                      :convex/hexstring-32
                      [:double
                       {:max 1
                        :min 0}]]
                     (fn [[pubkey ratio]]
                       (let [ctx ($.test.eval/ctx* (do
                                                     (def balance-original
                                                          *balance*)
                                                     (def addr
                                                          (create-account ~pubkey))
                                                     (def amount
                                                          (long (floor (* ~ratio
                                                                          balance-original))))
                                                     (transfer addr
                                                               amount)))]
                         ($.test.prop/mult*

                           "Own balance has been correctly update"
                           ($.test.eval/result* ctx
                                                (== balance-original
                                                    (+ *balance*
                                                       amount)))

                           "Balance of receiver has been correctly updated"
                           ($.test.eval/result* ctx
                                                (== amount
                                                    (balance addr))))))))
