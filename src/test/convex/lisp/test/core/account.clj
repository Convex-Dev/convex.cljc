(ns convex.lisp.test.core.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Suites - Miscellaneous


(defn suite-holding

  ""

  [ctx sym-addr holding]

  (let [ctx-2 ($.test.eval/ctx* ctx
                                (do
                                  (def addr
                                       ~sym-addr)
                                  (def holding
                                       (quote ~holding))
                                  (def -set-holding
                                       (set-holding addr
                                                    holding))))]
    ;(println :got holding ($.test.eval/result ctx-2 '(account addr)))
    ($.test.prop/mult*

      "`set-holding` returns the given holding"
      ($.test.eval/result ctx-2
                          '(= holding
                              -set-holding))

      "`get-holding` returns the given holding"
      ($.test.eval/result ctx-2
                          '(= holding
                              (get-holding addr)))

      "`set-holding` is consistent with `account`"
      ($.test.eval/result ctx-2
                          '(= (if (nil? holding)
                                nil
                                (assoc (blob-map)
                                       *address*
                                       holding))
                              (:holdings (account addr))))

      "Removing holding"
      (let [ctx-3 ($.test.eval/ctx* ctx-2
                                    (do
                                      (def -set-holding-2
                                           (set-holding addr
                                                        nil))))]
        ($.test.prop/mult*

          "`set-holding` with nil returns nil"
          ($.test.eval/result ctx-3
                              '(nil? -set-holding-2))

          "`account` shows nil in :holdings"
          ($.test.eval/result ctx-3
                              '(nil? (get (account addr)
                                          :holdings
                                          :convex-sentinel))))))))



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



(defn suite-set-key

  ""

  ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/129

  [ctx pubkey]

  (let [ctx-2 ($.test.eval/ctx* ctx
                                (do
                                  (def key-
                                       ~pubkey)
                                  (def ret-
                                       (set-key key-))))]
    ($.test.prop/mult*

      "New key is set in `*key*`"
      ($.test.eval/result ctx-2
                          '(= (blob key-)
                              (blob *key*)))

      "`*key*` is consistent with `account`"
      ($.test.eval/result ctx-2
                          '(= *key*
                              (:key (account *address*)))))))


;;;;;;;;;; Suites - Transfering coins


(defn ctx-transfer

  "Preparing the given `ctx` for [[suite-transfer]].
   
   Assumes address of the receiver is interned as `addr`.
  
   `percent` is the percentage of the current balance that should be transfered."

  [ctx percent]

  ($.test.eval/ctx* ctx
                    (do
                      (def balance-before
                           *balance*)
                      (def amount
                           (long (floor (* ~percent
                                           balance-before))))
                      (def -transfer
                           (transfer addr
                                     amount)))))



(defn suite-transfer

  "Suite where some percentage of current balance is sent to another account and both accounts must balance
   out as expected.

   If `percent` is given, `ctx` is prepared with [[ctx-transfer]]. If not, it must be prepared beforehand."


  ([ctx]

   ($.test.prop/mult*

      "Consistency between sender account information and `*balance*`, `balance` (before transfer)"
      ($.test.eval/result '(= *balance*
                              (balance *address*)
                              (:balance (account *address*))))


      "Consistency between sender account information and `*balance*`, `balance` (after transfer)"
      ($.test.eval/result ctx
                          '(= *balance*
                              (balance *address*)
                              (:balance (account *address*))))

      "Consistency between receiver account information and `balance`"
      ($.test.eval/result ctx
                          '(= (balance addr)
                              (:balance (account addr))))

      "`transfer` returns the sent amount"
      ($.test.eval/result ctx
                          '(= amount
                              -transfer))

      "Own balance has been correctly updated"
      ($.test.eval/result ctx
                          '(and (= balance-before
                                   (+ *balance*
                                      amount))
                                (= *balance*
                                   (- balance-before
                                      amount))))

      "Balance of receiver has been correctly updated"
      ($.test.eval/result ctx
                          '(= amount
                              (balance addr)))))


  ([ctx percent]

   (suite-transfer (ctx-transfer ctx
                                 percent))))


;;;;;;;;;; Tests


($.test.prop/deftest account-inexistant

  ($.test.prop/check [:and
                      [:int
                       {:min 50}]]
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



($.test.prop/deftest ^:recur main

  ($.test.prop/check [:tuple
                      :convex/data
                      :convex/hexstring-32
                      :convex.test/percent]
                     (fn [[holding pubkey percent-coin]]
                       (let [ctx ($.test.eval/ctx* (def addr
                                                        (create-account ~pubkey)))]
                         ($.test.prop/and* (suite-holding ctx
                                                          '*address*
                                                          holding)
                                           (suite-holding ctx
                                                          'addr
                                                          holding)
                                           (suite-new ctx
                                                      false?)
                                           (suite-set-key ctx
                                                          pubkey)
                                           (suite-transfer ctx
                                                           percent-coin))))))


;;;;;;;;;;


; *balance*
; *key*
; balance
; create-account
; set-key
; transfer

; account

; *caller*
; *exports*
; *holdings*
; *memory*
; *origin*
; export
; exports?
; get-holding
; set-controller
; set-holding
; transfer-memory
