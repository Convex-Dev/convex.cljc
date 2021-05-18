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



(defn suite-transfer

  "Suite where some amount of coin is sent to another account and both accounts must balance
   as expected.

   Assumes address is interned as `addr`."

  [ctx ratio]

  (let [ctx-2 ($.test.eval/ctx* ctx
                                (do
                                  (def balance-before
                                       *balance*)
                                  (def amount
                                       (long (floor (* ~ratio
                                                       balance-before))))
                                  (def -transfer
                                       (transfer addr
                                                 amount))))]
    ($.test.prop/mult*

      "`transfer` returns the sent amount"
      ($.test.eval/result ctx-2
                          '(= amount
                              -transfer))

      "Own balance has been correctly updated"
      ($.test.eval/result ctx-2
                          '(and (= balance-before
                                   (+ *balance*
                                      amount))
                                (= *balance*
                                   (- balance-before
                                      amount))))

      "Balance of receiver has been correctly updated"
      ($.test.eval/result ctx-2
                          '(= amount
                              (balance addr))))))


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



($.test.prop/deftest main

  ($.test.prop/check [:tuple
                      :convex/hexstring-32
                      [:double
                       {:max 1
                        :min 0}]]
                     (fn [[pubkey ratio-coin]]
                       (let [ctx ($.test.eval/ctx* (def addr
                                                        (create-account ~pubkey)))]
                         ($.test.prop/and* (suite-new ctx
                                                      false?)
                                           (suite-transfer ctx
                                                           ratio-coin))))))


;;;;;;;;;;


; *balance*
; *exports*
; *holdings*
; *memory*
; account
; balance
; create-account
; export
; exports?
; get-holding
; set-controller
; set-holding
; set-key
; transfer
; transfer-memory
