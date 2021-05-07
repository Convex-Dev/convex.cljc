(ns convex.lisp.test.core.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.mult :as $.test.mult]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest account-inexistant

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



($.test.prop/deftest create-account--

  ($.test.prop/check :convex/hexstring-32
                     (fn [x]
                       ($.test.prop/mult
                         ($.test.mult/new-account []
                                                  ($.test.eval/form->context ($.form/templ {'?hexstring x}
                                                                                           '(def addr
                                                                                                 (create-account ?hexstring))))
                                                  false?)))))



($.test.prop/deftest transfer--

  ($.test.prop/check [:tuple
                      :convex/hexstring-32
                      [:double
                       {:max 1
                        :min 0}]]
                     (fn [[pubkey ratio]]
                       ($.test.prop/mult-result ($.test.eval/form
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
