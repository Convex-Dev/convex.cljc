(ns convex.break.test.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:import convex.core.Constants)
  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.gen              :as $.break.gen]
            [convex.break.prop             :as $.break.prop]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]))


;;;;;;;;;; Suites - Miscellaneous


(defn suite-export

  "Tests exportings symbols in user accounts only.
  
   See [[convex.lisp.test.core.actor]] namespace for more thorough tests involving
   actors."

  [ctx sym+]

  ($.break.prop/checkpoint*
    
    "Exporting symbols in user account"

    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def -export+
                                          ~(into #{}
                                                 (map $.lisp/quoted)
                                                 sym+))
                                     (def -result-export
                                          (export ~@sym+))))]
      ($.break.prop/mult*

        "`export` returns `*exports*`"
        ($.break.eval/result ctx-2
                             '(= -result-export
                                 *exports*))

        "`*export*` has been updated"
        ($.break.eval/result ctx-2
                             '(= -export+
                                 *exports*))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/136
        ;;
        ;; "`exports?`"
        ;; ($.break.eval/result ctx-2
        ;;                      '($/every? (fn [sym]
        ;;                                   (exports? *address*
        ;;                                             sym))
        ;;                                 -export+))
        ))))



(defn suite-new

  "Every new account, actor or user, must pass this suite.

   Assumes address is interned as `addr`."

  [ctx actor?]

  ($.break.prop/checkpoint*

    "Every new account must pass this suite"

    ($.break.prop/mult*

      "Address is interned" 
      ($.lisp/address? ($.break.eval/result ctx
                                            'addr))

      "`account?`"
      ($.break.eval/result ctx
                           '(account? addr))

      "`actor?`"
      (actor? ($.break.eval/result ctx
                                   '(actor? addr)))

      "`address?`"
      ($.break.eval/result ctx
                           '(address? addr))

      "Balance is 0"
      ($.break.eval/result ctx
                           '(zero? (balance addr)))

      "Memory allowance is 0"
      ($.break.eval/result ctx
                           '(zero? ($/allowance addr)))

      "`get-holding` returns nothing on a virgin account"
      ($.break.eval/result ctx
                           '(nil? (get-holding addr)))

      "Comparing `account` with *state*"
      ($.break.eval/result ctx
                           '(= (account addr)
                               (get-in *state*
                                       [:accounts
                                        (long addr)]))))))



(defn suite-set-key

  "Suite testing setting a new public key."

  ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/129

  [ctx pubkey]

  ($.break.prop/checkpoint*

    "Setting a new public key"

    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def key-
                                          ~pubkey)
                                     (def ret-
                                          (set-key key-))))]
      ($.break.prop/mult*

        "New key is set in `*key*`"
        ($.break.eval/result ctx-2
                             '(= (blob key-)
                                 (blob *key*)))

        "`*key*` is consistent with `account`"
        ($.break.eval/result ctx-2
                             '(= *key*
                                 (:key (account *address*))))))))



(defn suite-transfer-memory

  "Suite testing memory tranfers."

  [ctx faulty-amount percent]
 
  ($.break.prop/checkpoint*

    "Transfering memory"

    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def memory-before
                                          *memory*)
                                     (def amount
                                          ($/long-percentage ~percent
                                                             *memory*))
                                     (def -transfer-memory
                                          (transfer-memory addr
                                                           amount))))]
      ($.break.prop/mult*

        "Returns the given amount"
        ($.break.eval/result ctx-2
                             '(= amount
                                 -transfer-memory))

        "Consistenty between sender account information and `*memory*` (before transfer)"
        ($.break.eval/result ctx
                             '(= *memory*
                                 ($/allowance)))

        "Consistency between sender account information and `*memory*` (after transfer)"
        ($.break.eval/result ctx-2
                             '(= *memory*
                                 ($/allowance)))

        "Allowance of sender account has diminished as expected"
        ($.break.eval/result ctx-2
                             '(and (= memory-before
                                      (+ ($/allowance)
                                         amount))
                                   (= ($/allowance)
                                      (- memory-before
                                         amount))))

        "Allowance of receiver account has increased as needed"
        ($.break.eval/result ctx-2
                             '(= amount
                                 ($/allowance addr)))

        "Transfering negative allowance"
        ($.break.eval/error-arg?* ctx-2
                                  (transfer-memory addr
                                                   ~(min -1
                                                         (long (* percent
                                                                  Long/MIN_VALUE)))))

        "Transfering too much allowance, insufficient amount"
        ($.break.eval/error-memory?* ctx-2
                                     (transfer-memory addr
                                                      (let [allowance ($/allowance)]
                                                        (+ allowance
                                                           (max 1
                                                                (long (floor (* ~percent
                                                                               (- ~Constants/MAX_SUPPLY
                                                                                  allowance)))))))))

        "Transfering allowance beyond authorized limit"
        ($.break.eval/error-arg?* ctx-2
                                  (transfer-memory addr
                                                   (+ (max 1
                                                           amount)
                                                      ~Constants/MAX_SUPPLY)))

        "Transfering garbage instead of memory"
        ($.break.eval/error-cast?* ctx-2
                                   (transfer-memory addr
                                                    ~faulty-amount))))))


;;;;;;;;;; Suites - Holdings


(defn ctx-holding

  "Prepares `ctx` for [[suite-*holdings*]] and [[suite-holding]]."

  [ctx sym-addr holding]

  ($.break.eval/ctx* ctx
                     (do
                       (def addr
                            ~sym-addr)
                       (def holding
                            ~holding)
                       (def -set-holding
                            (set-holding addr
                                         holding)))))



(defn suite-*holdings*

  "Complement for [[suite-holding]] which focuses on `*holdings*`."

  [ctx]

  ($.break.prop/checkpoint*

    "Using `*holdings*` in user accounts"

    ($.break.prop/mult*

      "`*holdings*` has one element"
      ($.break.eval/result ctx
                           '(= *holdings*
                               (if (nil? holding)
                                 (blob-map)
                                 (assoc (blob-map)
                                        *address*
                                        holding))))

      ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/131
      ;;
      "`*holdings* is consistent with `account`"
      ($.break.eval/result ctx
                           '(if (nil? holding)
                              true
                              (= *holdings*
                                 (:holdings (account *address*)))))

      "Removing only holding from `*holdings*`"
      ($.break.eval/result ctx
                           '(do
                              (set-holding *address*
                                           nil)
                              (= (blob-map)
                                 *holdings*))))))



(defn suite-holding

  "Testing properties related to setting some holding on any account."

  [ctx]

  ($.break.prop/checkpoint*

    "Setting and getting holdings"

    ($.break.prop/mult*
   
      "`set-holding` returns the given holding"
      ($.break.eval/result ctx
                           '(= holding
                               -set-holding))
   
      "`get-holding` returns the given holding"
      ($.break.eval/result ctx
                           '(= holding
                               (get-holding addr)))
   
      "`set-holding` is consistent with `account`"
      ($.break.eval/result ctx
                           '(= (if (nil? holding)
                                 (blob-map)
                                 (assoc (blob-map)
                                        *address*
                                        holding))
                               (:holdings (account addr))))
   
      "Removing holding"
      (let [ctx-2 ($.break.eval/ctx* ctx
                                     (do
                                       (def -set-holding-2
                                            (set-holding addr
                                                         nil))))]
        ($.break.prop/mult*
   
          "`set-holding` with nil returns nil"
          ($.break.eval/result ctx-2
                               '(nil? -set-holding-2))
    
          "`account` shows nil in :holdings"
          ($.break.eval/result ctx-2
                               '(= (blob-map)
                                   (get (account addr)
                                        :holdings
                                        :convex-sentinel))))))))


;;;;;;;;;; Suites - Transfering coins


(defn ctx-transfer

  "Preparing the given `ctx` for [[suite-transfer]].
   
   Assumes address of the receiver is interned as `addr`.
  
   `percent` is the percentage of the current balance that should be transfered."

  [ctx faulty-amount percent]

  ($.break.eval/ctx* ctx
                     (do
                       (def balance-before
                            *balance*)
                       (defn compute-amount []
                         ($/long-percentage ~percent
                                            *balance*))
                       (def amount
                            (compute-amount))
                       (def faulty-amount
                            ~faulty-amount)
                       (def percent
                            ~percent)
                       (def -transfer
                            (transfer addr
                                      amount)))))



(defn suite-transfer

  "Suite where some percentage of current balance is sent to another account and both accounts must balance
   out as expected.

   If `percent` is given, `ctx` is prepared with [[ctx-transfer]]. If not, it must be prepared beforehand."


  [ctx checkpoint]

  ($.break.prop/checkpoint*

    checkpoint

    ($.break.prop/mult*

       "`transfer` returns the sent amount"
       ($.break.eval/result ctx
                            '(= amount
                                -transfer))

       "Consistency between sender account information and `*balance*`, `balance` (before transfer)"
       ($.break.eval/result '(= *balance*
                                (balance *address*)
                                (:balance (account *address*))))


       "Consistency between sender account information and `*balance*`, `balance` (after transfer)"
       ($.break.eval/result ctx
                           '(= *balance*
                                (balance *address*)
                                (:balance (account *address*))))

       "Consistency between receiver account information and `balance`"
       ($.break.eval/result ctx
                            '(= (balance addr)
                                (:balance (account addr))))

       "Own balance has been correctly updated"
       ($.break.eval/result ctx
                            '(and (= balance-before
                                     (+ *balance*
                                        amount))
                                  (= *balance*
                                     (- balance-before
                                        amount))))

       "Balance of receiver has been correctly updated"
       ($.break.eval/result ctx
                            '(= amount
                                (balance addr)))

       "Transfering negative amount"
       ($.break.eval/error-arg?* ctx
                                 (transfer addr
                                           (min -1
                                                (long (* percent
                                                         ~Long/MIN_VALUE)))))

       "Transfering too much funds, insufficient amount"
       ($.break.eval/error-fund?* ctx
                                  (transfer addr
                                            (let [balance *balance*]
                                              (+ balance
                                                 (max 1
                                                      (long (floor (* percent
                                                                      (- ~Constants/MAX_SUPPLY
                                                                         balance)))))))))
 
       "Transfering funds beyond authorized limit"
       ($.break.eval/error-arg?* ctx
                                 (transfer addr
                                           (+ (max 1
                                                   amount)
                                              ~Constants/MAX_SUPPLY)))

       "Transfering garbage instead of funds"
       ($.break.eval/error-cast? ctx
                                 '(transfer addr
                                            faulty-amount)))))


;;;;;;;;;; Tests


($.break.prop/deftest account-inexistant

  (TC.prop/for-all [unused-address $.break.gen/unused-address]
    ($.break.prop/mult*

      "Account does not exist (long)"
      ($.break.eval/result* (not (account? ~unused-address)))

      "Account does not exist (address)"
      ($.break.eval/result* (not (account (address ~unused-address))))

      "Actor does not exist (long)"
      ($.break.eval/result* (not (actor? ~unused-address)))

      "Actor does not exist (address)"
      ($.break.eval/result* (not (actor? (address ~unused-address)))))))



($.break.prop/deftest main

  (TC.prop/for-all [export-sym+    (TC.gen/vector $.lisp.gen/symbol)
                    faulty-amount  $.break.gen/not-long
                    holding        $.lisp.gen/any
                    pubkey         $.lisp.gen/hex-string-32
                    percent        $.break.gen/percent]
    (let [ctx            ($.break.eval/ctx* (do
                                              (def addr
                                                   (create-account ~pubkey))))
          ctx-*holdings* (ctx-holding ctx
                                      '*address*
                                      holding)]
      ($.break.prop/and* (suite-export ctx
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
                                                      faulty-amount
                                                      percent)
                                        "Transfering coins to a user account")
                        (suite-transfer-memory ctx
                                               faulty-amount
                                               percent)))))


;; TODO. `set-controller`, already a bit tested by `eval-as`, also see: https://github.com/Convex-Dev/convex/issues/133


;;;;;;;;;; Negative tests


($.break.prop/deftest error-cast-address

  ;; Functions that should throw a CAST error when not operating over an address.

  (TC.prop/for-all [x $.break.gen/not-address]
    ($.break.prop/mult*

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/158
      ;;
      ;; "`account`"
      ;; ($.break.eval/error-cast?* (account ~x))

      "`balance`"
      ($.break.eval/error-cast?* (balance ~x))

      "`exports?`"
      ($.break.eval/error-cast?* (exports? ~x
                                           'foo))

      "`get-holding`"
      ($.break.eval/error-cast?* (get-holding ~x))

      "`set-controller`"
      (if (nil? x)
        true
        ($.break.eval/error-cast?* (set-controller ~x)))

      "`set-holding`"
      ($.break.eval/error-cast?* (set-holding ~x
                                              ~x))

      "`transfer`"
      ($.break.eval/error-cast?* (transfer ~x
                                           1))

      "`transfer-memory`"
      ($.break.eval/error-cast?* (transfer-memory ~x
                                                  1)))))



($.break.prop/deftest error-cast-key

  ;; Providing something that cannot be used as a key should fail.

  (TC.prop/for-all [x (TC.gen/such-that (fn [x]
                                          (if-some [x-2 (cond
                                                          (string? x)      x
                                                          ($.lisp/blob? x) ($.lisp/meta-raw x))]
                                            (= (count x-2)
                                               64)
                                            (some? x)))
                                        $.lisp.gen/any)]
    ($.break.eval/error-cast?* (set-key ~x))))



($.break.prop/deftest error-nobody

  ;; Side-effects on adresses that should fail if the target address does not exist.

  (TC.prop/for-all [addr $.break.gen/unused-address]
    ($.break.prop/mult*

      "`set-controller`"
      ($.break.eval/error-nobody?* (set-controller ~addr))

      "`set-holding`"
      ($.break.eval/error-nobody?* (set-holding ~addr
                                                42))


       "`transfer`"
       ($.break.eval/error-nobody?* (transfer ~addr
                                              42))

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/159
      ;;
      ;; "Transfering allowance to unused address"
      ;; ($.break.eval/error-nobody?* (transfer-memory ~addr
      ;;                                               42))
      )))
