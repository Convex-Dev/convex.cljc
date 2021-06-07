(ns convex.break.test.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:import convex.core.Constants)
  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.gen              :as $.break.gen]
            [convex.cvm                    :as $.cvm]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites - Miscellaneous


(defn suite-export

  "Tests exportings symbols in user accounts only.
  
   See [[convex.lisp.test.core.actor]] namespace for more thorough tests involving
   actors."

  [ctx sym+]

  (mprop/check
    
    "Exporting symbols in user account"

    (let [ctx-2 ($.cvm.eval/ctx* ctx
                                 (do
                                   (def -export+
                                        ~(into #{}
                                               (map $.lisp/quoted)
                                               sym+))
                                   (def -result-export
                                        (export ~@sym+))))]
      (mprop/mult

        "`export` returns `*exports*`"

        ($.cvm.eval/result ctx-2
                           '(= -result-export
                               *exports*))


        "`*export*` has been updated"

        ($.cvm.eval/result ctx-2
                           '(= -export+
                               *exports*))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/136
        ;;
        ;; "`exports?`"
        ;; ($.cvm.eval/result ctx-2
        ;;                      '($/every? (fn [sym]
        ;;                                   (exports? *address*
        ;;                                             sym))
        ;;                                 -export+))
        ))))



(defn suite-new

  "Every new account, actor or user, must pass this suite.

   Assumes address is interned as `addr`."

  [ctx actor?]

  (mprop/check

    "Every new account must pass this suite"

    (mprop/mult

      "Address is interned" 

      ($.lisp/address? ($.cvm.eval/result ctx
                                          'addr))


      "`account?`"

      ($.cvm.eval/result ctx
                         '(account? addr))


      "`actor?`"

      (actor? ($.cvm.eval/result ctx
                                 '(actor? addr)))


      "`address?`"

      ($.cvm.eval/result ctx
                         '(address? addr))


      "Balance is 0"

      ($.cvm.eval/result ctx
                         '(zero? (balance addr)))


      "Memory allowance is 0"

      ($.cvm.eval/result ctx
                         '(zero? ($/allowance addr)))


      "`get-holding` returns nothing on a virgin account"

      ($.cvm.eval/result ctx
                         '(nil? (get-holding addr)))


      "Comparing `account` with *state*"
      
      ($.cvm.eval/result ctx
                         '(= (account addr)
                             (get-in *state*
                                     [:accounts
                                      (long addr)]))))))



(defn suite-set-key

  "Suite testing setting a new public key."

  ;; TODO. Keep an eye on: https://github.com/Convex-Dev/convex/issues/129

  [ctx pubkey]

  (mprop/check

    "Setting a new public key"

    (let [ctx-2 ($.cvm.eval/ctx* ctx
                                 (do
                                   (def key-
                                        ~pubkey)
                                   (def ret-
                                        (set-key key-))))]
      (mprop/mult

        "New key is set in `*key*`"

        ($.cvm.eval/result ctx-2
                           '(= (blob key-)
                               (blob *key*)))


        "`*key*` is consistent with `account`"

        ($.cvm.eval/result ctx-2
                           '(= *key*
                               (:key (account *address*))))))))



(defn suite-transfer-memory

  "Suite testing memory tranfers."

  [ctx faulty-amount percent]
 
  (mprop/check

    "Transfering memory"

    (let [ctx-2 ($.cvm.eval/ctx* ctx
                                 (do
                                   (def memory-before
                                        *memory*)
                                   (def amount
                                        ($/long-percentage ~percent
                                                           *memory*))
                                   (def -transfer-memory
                                        (transfer-memory addr
                                                         amount))))]
      (mprop/mult

        "Returns the given amount"

        ($.cvm.eval/result ctx-2
                           '(= amount
                               -transfer-memory))


        "Consistenty between sender account information and `*memory*` (before transfer)"

        ($.cvm.eval/result ctx
                           '(= *memory*
                               ($/allowance)))


        "Consistency between sender account information and `*memory*` (after transfer)"

        ($.cvm.eval/result ctx-2
                           '(= *memory*
                               ($/allowance)))


        "Allowance of sender account has diminished as expected"

        ($.cvm.eval/result ctx-2
                           '(and (= memory-before
                                    (+ ($/allowance)
                                       amount))
                                 (= ($/allowance)
                                    (- memory-before
                                       amount))))


        "Allowance of receiver account has increased as needed"

        ($.cvm.eval/result ctx-2
                           '(= amount
                               ($/allowance addr)))


        "Transfering negative allowance"

        ($.cvm.eval/code?* ctx-2
                           :ARGUMENT
                           (transfer-memory addr
                                            ~(min -1
                                                  (long (* percent
                                                           Long/MIN_VALUE)))))


        "Transfering too much allowance, insufficient amount"

        ($.cvm.eval/code?* ctx-2
                           :MEMORY
                           (transfer-memory addr
                                            (let [allowance ($/allowance)]
                                              (+ allowance
                                                 (max 1
                                                      (long (floor (* ~percent
                                                                     (- ~Constants/MAX_SUPPLY
                                                                        allowance)))))))))


        "Transfering allowance beyond authorized limit"

        ($.cvm.eval/code?* ctx-2
                           :ARGUMENT
                           (transfer-memory addr
                                            (+ (max 1
                                                    amount)
                                               ~Constants/MAX_SUPPLY)))


        "Transfering garbage instead of memory"

        ($.cvm.eval/code?* ctx-2
                           :CAST
                           (transfer-memory addr
                                            ~faulty-amount))))))


;;;;;;;;;; Suites - Holdings


(defn ctx-holding

  "Prepares `ctx` for [[suite-*holdings*]] and [[suite-holding]]."

  [ctx sym-addr holding]

  ($.cvm.eval/ctx* ctx
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

  (mprop/check

    "Using `*holdings*` in user accounts"

    (mprop/mult

      "`*holdings*` has one element"

      ($.cvm.eval/result ctx
                         '(= *holdings*
                             (if (nil? holding)
                               (blob-map)
                               (assoc (blob-map)
                                      *address*
                                      holding))))


      "`*holdings* is consistent with `account`"

      ($.cvm.eval/result ctx
                         '(if (nil? holding)
                            true
                            (= *holdings*
                               (:holdings (account *address*)))))


      "Removing only holding from `*holdings*`"

      ($.cvm.eval/result ctx
                         '(do
                            (set-holding *address*
                                         nil)
                            (= (blob-map)
                               *holdings*))))))



(defn suite-holding

  "Testing properties related to setting some holding on any account."

  [ctx]

  (mprop/check

    "Setting and getting holdings"

    (mprop/mult
   
      "`set-holding` returns the given holding"

      ($.cvm.eval/result ctx
                         '(= holding
                             -set-holding))
   

      "`get-holding` returns the given holding"

      ($.cvm.eval/result ctx
                         '(= holding
                             (get-holding addr)))
   

      "`set-holding` is consistent with `account`"

      ($.cvm.eval/result ctx
                         '(= (if (nil? holding)
                               (blob-map)
                               (assoc (blob-map)
                                      *address*
                                      holding))
                             (:holdings (account addr))))
   

      "Removing holding"

      (let [ctx-2 ($.cvm.eval/ctx* ctx
                                   (do
                                     (def -set-holding-2
                                          (set-holding addr
                                                       nil))))]
        (mprop/mult
   
          "`set-holding` with nil returns nil"
          ($.cvm.eval/result ctx-2
                             '(nil? -set-holding-2))
    
          "`account` shows nil in :holdings"
          ($.cvm.eval/result ctx-2
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

  ($.cvm.eval/ctx* ctx
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


  [ctx beacon]

  (mprop/check

    beacon

    (mprop/mult

       "`transfer` returns the sent amount"

       ($.cvm.eval/result ctx
                          '(= amount
                              -transfer))


       "Consistency between sender account information and `*balance*`, `balance` (before transfer)"

       ($.cvm.eval/result '(= *balance*
                              (balance *address*)
                              (:balance (account *address*))))


       "Consistency between sender account information and `*balance*`, `balance` (after transfer)"

       ($.cvm.eval/result ctx
                          '(= *balance*
                               (balance *address*)
                               (:balance (account *address*))))


       "Consistency between receiver account information and `balance`"

       ($.cvm.eval/result ctx
                          '(= (balance addr)
                              (:balance (account addr))))


       "Own balance has been correctly updated"

       ($.cvm.eval/result ctx
                          '(and (= balance-before
                                   (+ *balance*
                                      amount))
                                (= *balance*
                                   (- balance-before
                                      amount))))


       "Balance of receiver has been correctly updated"

       ($.cvm.eval/result ctx
                          '(= amount
                              (balance addr)))


       "Transfering negative amount"

       ($.cvm.eval/code?* ctx
                          :ARGUMENT
                          (transfer addr
                                    (min -1
                                         (long (* percent
                                                  ~Long/MIN_VALUE)))))


       "Transfering too much funds, insufficient amount"

       ($.cvm.eval/code?* ctx
                          :FUNDS
                          (transfer addr
                                    (let [balance *balance*]
                                      (+ balance
                                         (max 1
                                              (long (floor (* percent
                                                              (- ~Constants/MAX_SUPPLY
                                                                 balance)))))))))
 

       "Transfering funds beyond authorized limit"

       ($.cvm.eval/code?* ctx
                          :ARGUMENT
                          (transfer addr
                                    (+ (max 1
                                            amount)
                                       ~Constants/MAX_SUPPLY)))


       "Transfering garbage instead of funds"

       ($.cvm.eval/code? ctx
                         ($.cvm/code-std* :CAST)
                         '(transfer addr
                                    faulty-amount)))))


;;;;;;;;;; Tests


(mprop/deftest account-inexistant

  {:ratio-num 5}

  (TC.prop/for-all [unused-address $.break.gen/unused-address]
    (mprop/mult

      "Account does not exist (long)"

      ($.cvm.eval/result* (not (account? ~unused-address)))


      "Account does not exist (address)"

      ($.cvm.eval/result* (not (account (address ~unused-address))))


      "Actor does not exist (long)"

      ($.cvm.eval/result* (not (actor? ~unused-address)))


      "Actor does not exist (address)"

      ($.cvm.eval/result* (not (actor? (address ~unused-address)))))))



(mprop/deftest main

  {:ratio-num 2}

  (TC.prop/for-all [export-sym+   (TC.gen/vector $.lisp.gen/symbol)
                    faulty-amount $.break.gen/not-long
                    holding       $.lisp.gen/any
                    pubkey        $.lisp.gen/blob-32
                    percent       $.break.gen/percent]
    (let [ctx            ($.cvm.eval/ctx* (def addr
                                               (create-account ~pubkey)))
          ctx-*holdings* (ctx-holding ctx
                                      '*address*
                                      holding)]
      (mprop/and (suite-export ctx
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


;;;;;;;;;; Negative tests


(mprop/deftest error-cast-address

  ;; Functions that should throw a CAST error when not operating over an address.

  {:ratio-num 5}

  (TC.prop/for-all [x $.break.gen/not-address]
    (mprop/mult

      "`account`"

      ($.cvm.eval/code?* :CAST
                         (account ~x))


      "`balance`"

      ($.cvm.eval/code?* :CAST
                         (balance ~x))


      "`exports?`"

      ($.cvm.eval/code?* :CAST
                         (exports? ~x
                                   'foo))


      "`get-holding`"

      ($.cvm.eval/code?* :CAST
                         (get-holding ~x))


      "`set-controller`"

      (if (nil? x)
        true
        ($.cvm.eval/code?* :CAST
                           (set-controller ~x)))


      "`set-holding`"

      ($.cvm.eval/code?* :CAST
                         (set-holding ~x
                                      ~x))


      "`transfer`"

      ($.cvm.eval/code?* :CAST
                         (transfer ~x
                                   1))


      "`transfer-memory`"

      ($.cvm.eval/code?* :CAST
                         (transfer-memory ~x
                                          1)))))



(mprop/deftest error-cast-key

  {:ratio-num 10}

  ;; Providing something that cannot be used as a key should fail.

  (TC.prop/for-all [x (TC.gen/such-that (fn [x]
                                          (if-some [x-2 (cond
                                                          (string? x)      x
                                                          ($.lisp/blob? x) ($.lisp/meta-raw x))]
                                            (not= (count x-2)
                                                         64)
                                            (some? x)))
                                        $.lisp.gen/any)]
    ($.cvm.eval/code?* :CAST
                       (set-key ~x))))



(mprop/deftest error-nobody

  ;; Side-effects on adresses that should fail if the target address does not exist.

  {:ratio-num 10}

  (TC.prop/for-all [addr $.break.gen/unused-address]
    (mprop/mult

      "`set-controller`"

      ($.cvm.eval/code?* :NOBODY
                         (set-controller ~addr))


      "`set-holding`"

      ($.cvm.eval/code?* :NOBODY
                         (set-holding ~addr
                                      42))



       "`transfer`"

       ($.cvm.eval/code?* :NOBODY
                          (transfer ~addr
                                    42))


      "Transfering allowance to unused address"

      ($.cvm.eval/code?* :NOBODY
                         (transfer-memory ~addr
                                          42)))))
