(ns convex.lisp.test.core.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:import convex.core.Constants)
  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.form              :as $.form]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;; Suites - Miscellaneous


(defn suite-export

  "Tests exportings symbols in user accounts only.
  
   See [[convex.lisp.test.core.actor]] namespace for more thorough tests involving
   actors."

  [ctx sym+]

  ($.test.prop/checkpoint*
    
    "Exporting symbols in user account"

    (let [ctx-2 ($.test.eval/ctx* ctx
                                  (do
                                    (def -export+
                                         ~(into #{}
                                                (map $.form/quoted)
                                                sym+))
                                    (def -result-export
                                         (export ~@sym+))))]
      ($.test.prop/mult*

        "`export` returns `*exports*`"
        ($.test.eval/result ctx-2
                            '(= -result-export
                                *exports*))

        "`*export*` has been updated"
        ($.test.eval/result ctx-2
                            '(= -export+
                                *exports*))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/136
        ;;
        ;; "`exports?`"
        ;; ($.test.eval/result ctx-2
        ;;                     '($/every? (fn [sym]
        ;;                                  (exports? *address*
        ;;                                            sym))
        ;;                                -export+))
        ))))



(defn suite-new

  "Every new account, actor or user, must pass this suite.

   Assumes address is interned as `addr`."

  [ctx actor?]

  ($.test.prop/checkpoint*

    "Every new account must pass this suite"

    ($.test.prop/mult*

      "Address is interned" 
      ($.form/address? ($.test.eval/result ctx
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

      "Memory allowance is 0"
      ($.test.eval/result ctx
                          '(zero? ($/allowance addr)))

      "`get-holding` returns nothing on a virgin account"
      ($.test.eval/result ctx
                          '(nil? (get-holding addr)))

      "Comparing `account` with *state*"
      ($.test.eval/result ctx
                          '(= (account addr)
                              (get-in *state*
                                      [:accounts
                                       (long addr)]))))))



(defn suite-set-key

  "Suite testing setting a new public key."

  ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/129

  [ctx pubkey]

  ($.test.prop/checkpoint*

    "Setting a new public key"

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
                                (:key (account *address*))))))))



(defn suite-transfer-memory

  "Suite testing memory tranfers."

  [ctx faulty-amount percent unused-address]
 
  ($.test.prop/checkpoint*

    "Transfering memory"

    (let [ctx-2 ($.test.eval/ctx* ctx
                                  (do
                                    (def memory-before
                                         *memory*)
                                    (def amount
                                         (long (floor (* ~percent
                                                         *memory*))))
                                    (def -transfer-memory
                                         (transfer-memory addr
                                                          amount))))]
      ($.test.prop/mult*

        "Returns the given amount"
        ($.test.eval/result ctx-2
                            '(= amount
                                -transfer-memory))

        "Consistenty between sender account information and `*memory*` (before transfer)"
        ($.test.eval/result ctx
                            '(= *memory*
                                ($/allowance)))

        "Consistency between sender account information and `*memory*` (after transfer)"
        ($.test.eval/result ctx-2
                            '(= *memory*
                                ($/allowance)))

        "Allowance of sender account has diminished as expected"
        ($.test.eval/result ctx-2
                            '(and (= memory-before
                                     (+ ($/allowance)
                                        amount))
                                  (= ($/allowance)
                                     (- memory-before
                                        amount))))

        "Allowance of receiver account has increased as needed"
        ($.test.eval/result ctx-2
                            '(= amount
                                ($/allowance addr)))

        "Transfering negative allowance"
        ($.test.eval/error-arg?* ctx-2
                                 (transfer-memory addr
                                                  ~(min -1
                                                        (long (* percent
                                                                 Long/MIN_VALUE)))))

        "Transfering too much allowance, insufficient amount"
        ($.test.eval/error-memory?* ctx-2
                                    (transfer-memory addr
                                                     (let [allowance ($/allowance)]
                                                       (+ allowance
                                                          (max 1
                                                               (long (* ~percent
                                                                        (- ~Constants/MAX_SUPPLY
                                                                           allowance))))))))

        "Transfering allowance beyond authorized limit"
        ($.test.eval/error-arg?* ctx-2
                                 (transfer-memory addr
                                                  (+ amount
                                                     ~Constants/MAX_SUPPLY)))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/159
        ;;
        ;; "Transfering allowance to unused address"
        ;; ($.test.eval/error-nobody?* ctx-2
        ;;                             (transfer-memory ~unused-address
        ;;                                              amount))

        "Transfering garbage instead of memory"
        ($.test.eval/error-cast?* ctx-2
                                  (transfer-memory addr
                                                   ~faulty-amount))))))


;;;;;;;;;; Suites - Holdings


(defn ctx-holding

  "Prepares `ctx` for [[suite-*holdings*]] and [[suite-holding]]."

  [ctx sym-addr holding]

  ($.test.eval/ctx* ctx
                    (do
                      (def addr
                           ~sym-addr)
                      (def holding
                           (quote ~holding))
                      (def -set-holding
                           (set-holding addr
                                        holding)))))



(defn suite-*holdings*

  "Complement for [[suite-holding]] which focuses on `*holdings*`."

  [ctx]

  ($.test.prop/checkpoint*

    "Using `*holdings*` in user accounts"

    ($.test.prop/mult*

      "`*holdings*` has one element"
      ($.test.eval/result ctx
                          '(= *holdings*
                              (if (nil? holding)
                                (blob-map)
                                (assoc (blob-map)
                                       *address*
                                       holding))))

      ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/131
      ;;
      "`*holdings* is consistent with `account`"
      ($.test.eval/result ctx
                          '(if (nil? holding)
                             true
                             (= *holdings*
                                (:holdings (account *address*)))))

      "Removing only holding from `*holdings*`"
      ($.test.eval/result ctx
                          '(do
                             (set-holding *address*
                                          nil)
                             (= (blob-map)
                                *holdings*))))))



(defn suite-holding

  "Testing properties related to setting some holding on any account."

  [ctx]

  ($.test.prop/checkpoint*

    "Setting and getting holdings"

    ($.test.prop/mult*
   
      "`set-holding` returns the given holding"
      ($.test.eval/result ctx
                          '(= holding
                              -set-holding))
   
      "`get-holding` returns the given holding"
      ($.test.eval/result ctx
                          '(= holding
                              (get-holding addr)))
   
      "`set-holding` is consistent with `account`"
      ($.test.eval/result ctx
                          '(= (if (nil? holding)
                                (blob-map)
                                (assoc (blob-map)
                                       *address*
                                       holding))
                              (:holdings (account addr))))
   
      "Removing holding"
      (let [ctx-2 ($.test.eval/ctx* ctx
                                    (do
                                      (def -set-holding-2
                                           (set-holding addr
                                                        nil))))]
        ($.test.prop/mult*
   
          "`set-holding` with nil returns nil"
          ($.test.eval/result ctx-2
                              '(nil? -set-holding-2))
   
          "`account` shows nil in :holdings"
          ($.test.eval/result ctx-2
                              '(= (blob-map)
                                  (get (account addr)
                                       :holdings
                                       :convex-sentinel))))))))


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
                                           *balance*))))
                      (def -transfer
                           (transfer addr
                                     amount)))))



(defn suite-transfer

  "Suite where some percentage of current balance is sent to another account and both accounts must balance
   out as expected.

   If `percent` is given, `ctx` is prepared with [[ctx-transfer]]. If not, it must be prepared beforehand."


  [ctx checkpoint]

  ($.test.prop/checkpoint*

    checkpoint

    ($.test.prop/mult*

       "`transfer` returns the sent amount"
       ($.test.eval/result ctx
                           '(= amount
                               -transfer))

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
                               (balance addr))))))


;;;;;;;;;; Tests


($.test.prop/deftest account-inexistant

  (TC.prop/for-all [x (TC.gen/large-integer* {:min 50})]
    ($.test.prop/mult*

      "Account does not exist (long)"
      ($.test.eval/result* (not (account? ~x)))

      "Account does not exist (address)"
      ($.test.eval/result* (not (account (address ~x))))

      "Actor does not exist (long)"
      ($.test.eval/result* (not (actor? ~x)))

      "Actor does not exist (address)"
      ($.test.eval/result* (not (actor? (address ~x)))))))



($.test.prop/deftest main

  (TC.prop/for-all [export-sym+    (TC.gen/vector $.gen/symbol)
                    faulty-amount  $.test.gen/not-long
                    holding        $.gen/any
                    pubkey         $.gen/hex-string-32
                    percent        $.test.gen/percent
                    unused-address $.test.gen/unused-address]
    (let [ctx            ($.test.eval/ctx* (def addr
                                                (create-account ~pubkey)))
          ctx-*holdings* (ctx-holding ctx
                                      '*address*
                                      holding)]
      ($.test.prop/and* (suite-export ctx
                                      export-sym+)
                        (suite-*holdings* ctx-*holdings*)
                        (suite-holding ctx-*holdings*)
                        (suite-holding (ctx-holding ctx
                                                    'addr
                                                    holding))
                        (suite-new ctx
                                   false?)
                        (suite-set-key ctx
                                       pubkey)
                        (suite-transfer (ctx-transfer ctx
                                                      percent)
                                        "Transfering coins to a user account")
                        (suite-transfer-memory ctx
                                               faulty-amount
                                               percent
                                               unused-address)))))


;; TODO. `set-controller`, already a bit tested by `eval-as`, also see: https://github.com/Convex-Dev/convex/issues/133


;;;;;;;;;; Negative tests


($.test.prop/deftest error-cast-address


  (TC.prop/for-all [x $.test.gen/not-address]
    ($.test.prop/mult*

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/158
      ;;
      ;; "`account`"
      ;; ($.test.eval/error-cast?* (account ~x))

      "`balance`"
      ($.test.eval/error-cast?* (balance ~x))

      "`exports?`"
      ($.test.eval/error-cast?* (exports? ~x
                                          'foo))

      "`get-holding`"
      ($.test.eval/error-cast?* (get-holding ~x))

      "`set-holding`"
      ($.test.eval/error-cast?* (set-holding ~x
                                             ~x))

      "`transfer`"
      ($.test.eval/error-cast?* (transfer ~x
                                          1))

      "`transfer-memory`"
      ($.test.eval/error-cast?* (transfer-memory ~x
                                                 1)))))



($.test.prop/deftest error-cast-key

  ;; Providing something that cannot be used as a key should fail.

  (TC.prop/for-all [x (TC.gen/such-that (fn [x]
                                          (if-some [x-2 (cond
                                                          (string? x)      x
                                                          ($.form/blob? x) ($.form/meta-raw x))]
                                            (= (count x-2)
                                               64)
                                            (some? x)))
                                        $.gen/any)]
    ($.test.eval/error-cast?* (set-key ~x))))
