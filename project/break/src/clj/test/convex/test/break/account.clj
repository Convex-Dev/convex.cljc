(ns convex.test.break.account

  "Testing account utilities."

  {:author "Adam Helinski"}

  (:import (convex.core Constants))
  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.break.gen              :as $.break.gen]
            [convex.eval                   :as $.eval]
            [convex.cell                   :as $.cell]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites - Miscellaneous


(defn suite-new

  "Every new account, actor or user, must pass this suite.

   Assumes address is interned as `addr`."

  [ctx actor?]

  (mprop/check

    "Every new account must pass this suite"

    (mprop/mult

      "Address is interned" 

      ($.std/address? ($.eval/result ctx
                                     ($.cell/* addr)))


      "`account?`"

      ($.eval/true? ctx
                    ($.cell/* (account? addr)))


      "`actor?`"

      (actor? ($.eval/true? ctx
                            ($.cell/* (actor? addr))))


      "`address?`"

      ($.eval/true? ctx
                    ($.cell/* (address? addr)))


      "Balance is 0"

      ($.eval/true? ctx
                    ($.cell/* (zero? (balance addr))))


      "Memory allowance is 0"

      ($.eval/true? ctx
                    ($.cell/* (zero? ($/allowance addr))))


      "`get-holding` returns nothing on a virgin account"

      ($.eval/true? ctx
                    ($.cell/* (nil? (get-holding addr))))


      "Comparing `account` with *state*"

      ($.eval/true? ctx
                    ($.cell/* (= (account addr)
                                 (get-in *state*
                                         [:accounts
                                          (long addr)])))))))



(defn suite-set-key

  "Suite testing setting a new public key."

  [ctx pubkey]

  (mprop/check

    "Setting a new public key"

    (let [ctx-2 ($.eval/ctx ctx
                            ($.cell/* (do
                                        (def key-
                                             ~pubkey)
                                        (def ret-
                                             (set-key key-)))))]
      (mprop/mult

        "New key is set in `*key*`"

        ($.eval/true? ctx-2
                      ($.cell/* (= (blob key-)
                                   (blob *key*))))


        "`*key*` is consistent with `account`"

        ($.eval/true? ctx-2
                      ($.cell/* (= *key*
                                   (:key (account *address*)))))))))



(defn suite-transfer-memory

  "Suite testing memory tranfers."

  [ctx faulty-amount percent]
 
  (mprop/check

    "Transfering memory"

    (let [ctx-2 ($.eval/ctx ctx
                            ($.cell/* (do
                                        (def memory-before
                                             *memory*)
                                        (def amount
                                             ($/long-percentage ~percent
                                                                *memory*))
                                        (def -transfer-memory
                                             (transfer-memory addr
                                                              amount)))))]
      (mprop/mult

        "Returns the given amount"

        ($.eval/true? ctx-2
                      ($.cell/* (= amount
                                   -transfer-memory)))


        "Consistenty between sender account information and `*memory*` (before transfer)"

        ($.eval/true? ctx
                      ($.cell/* (= *memory*
                                   ($/allowance))))


        "Consistency between sender account information and `*memory*` (after transfer)"

        ($.eval/true? ctx-2
                      ($.cell/* (= *memory*
                                   ($/allowance))))


        "Allowance of sender account has diminished as expected"

        ($.eval/true? ctx-2
                      ($.cell/* (and (= memory-before
                                        (+ ($/allowance)
                                           amount))
                                     (= ($/allowance)
                                        (- memory-before
                                           amount)))))


        "Allowance of receiver account has increased as needed"

        ($.eval/true? ctx-2
                      ($.cell/* (= amount
                                   ($/allowance addr))))


        "Transfering negative allowance"

        (= ($.cell/code-std* :ARGUMENT)
           ($.eval/exception-code ctx-2
                                  ($.cell/* (transfer-memory addr
                                                             (min -1
                                                                  (long (* ~percent
                                                                           ~($.cell/long Long/MIN_VALUE))))))))


        "Transfering too much allowance, insufficient amount"

        (= ($.cell/code-std* :MEMORY)
           ($.eval/exception-code ctx-2
                                  ($.cell/* (transfer-memory addr
                                                             (let [allowance ($/allowance)]
                                                               (+ allowance
                                                                  (max 1
                                                                       (long (floor (* ~percent
                                                                                      (- ~($.cell/long Constants/MAX_SUPPLY)
                                                                                         allowance)))))))))))


        "Transfering allowance beyond authorized limit"

        (= ($.cell/code-std* :ARGUMENT)
           ($.eval/exception-code ctx-2
                                  ($.cell/* (transfer-memory addr
                                                             (+ (max 1
                                                                     amount)
                                                                ~($.cell/long Constants/MAX_SUPPLY))))))


        "Transfering garbage instead of memory"

        (= ($.cell/code-std* :CAST)
           ($.eval/exception-code ctx-2
                                  ($.cell/* (transfer-memory addr
                                                             ~faulty-amount))))))))


;;;;;;;;;; Suites - Holdings


(defn ctx-holding

  "Prepares `ctx` for [[suite-*holdings*]] and [[suite-holding]]."

  [ctx sym-addr holding]

  ($.eval/ctx ctx
              ($.cell/* (do
                          (def addr
                               ~sym-addr)
                          (def holding
                               (quote ~holding))
                          (def -set-holding
                               (set-holding addr
                                            holding))))))



(defn suite-*holdings*

  "Complement for [[suite-holding]] which focuses on `*holdings*`."

  [ctx]

  (mprop/check

    "Using `*holdings*` in user accounts"

    (mprop/mult

      "`*holdings*` has one element"

      ($.eval/true? ctx
                    ($.cell/* (= *holdings*
                                 (if (nil? holding)
                                   (blob-map)
                                   (assoc (blob-map)
                                          *address*
                                          holding)))))


      "`*holdings* is consistent with `account`"

      ($.eval/true? ctx
                    ($.cell/* (if (nil? holding)
                                true
                                (= *holdings*
                                   (:holdings (account *address*))))))


      "Removing only holding from `*holdings*`"

      ($.eval/true? ctx
                    ($.cell/* (do
                                (set-holding *address*
                                             nil)
                                (= (blob-map)
                                   *holdings*)))))))



(defn suite-holding

  "Testing properties related to setting some holding on any account."

  [ctx]

  (mprop/check

    "Setting and getting holdings"

    (mprop/mult
   
      "`set-holding` returns the given holding"

      ($.eval/true? ctx
                    ($.cell/* (= holding
                                 -set-holding)))
   

      "`get-holding` returns the given holding"

      ($.eval/true? ctx
                    ($.cell/* (= holding
                                 (get-holding addr))))
   

      "`set-holding` is consistent with `account`"

      ($.eval/true? ctx
                    ($.cell/* (= (if (nil? holding)
                                   (blob-map)
                                   (assoc (blob-map)
                                          *address*
                                          holding))
                                 (:holdings (account addr)))))
   

      "Removing holding"

      (let [ctx-2 ($.eval/ctx ctx
                              ($.cell/* (do
                                          (def -set-holding-2
                                               (set-holding addr
                                                            nil)))))]
        (mprop/mult
   
          "`set-holding` with nil returns nil"
          ($.eval/true? ctx-2
                        ($.cell/* (nil? -set-holding-2)))
    
          "`account` shows nil in :holdings"
          ($.eval/true? ctx-2
                        ($.cell/* (= (blob-map)
                                     (get (account addr)
                                          :holdings
                                          :convex-sentinel)))))))))


;;;;;;;;;; Suites - Transfering coins


(defn ctx-transfer

  "Preparing the given `ctx` for [[suite-transfer]].
   
   Assumes address of the receiver is interned as `addr`.
  
   `percent` is the percentage of the current balance that should be transfered."

  [ctx faulty-amount percent]

  ($.eval/ctx ctx
              ($.cell/* (do
                          (def balance-before
                               *balance*)
                          (defn compute-amount
                            []
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
                                         amount))))))



(defn suite-transfer

  "Suite where some percentage of current balance is sent to another account and both accounts must balance
   out as expected.

   If `percent` is given, `ctx` is prepared with [[ctx-transfer]]. If not, it must be prepared beforehand."


  [ctx beacon]

  (mprop/check

    beacon

    (mprop/mult

       "`transfer` returns the sent amount"

       ($.eval/true? ctx
                     ($.cell/* (= amount
                                  -transfer)))


       "Consistency between sender account information and `*balance*`, `balance` (before transfer)"

       ($.eval/true? ctx 
                     ($.cell/* (= *balance*
                                  (balance *address*)
                                  (:balance (account *address*)))))


       "Consistency between sender account information and `*balance*`, `balance` (after transfer)"

       ($.eval/true? ctx
                     ($.cell/* (= *balance*
                                  (balance *address*)
                                  (:balance (account *address*)))))


       "Consistency between receiver account information and `balance`"

       ($.eval/true? ctx
                     ($.cell/* (= (balance addr)
                                  (:balance (account addr)))))


       "Own balance has been correctly updated"

       ($.eval/true? ctx
                     ($.cell/* (and (= balance-before
                                       (+ *balance*
                                          amount))
                                    (= *balance*
                                       (- balance-before
                                          amount)))))


       "Balance of receiver has been correctly updated"

       ($.eval/true? ctx
                     ($.cell/* (= amount
                                  (balance addr))))


       "Transfering negative amount"

       (= ($.cell/code-std* :ARGUMENT)
          ($.eval/exception-code ctx
                                 ($.cell/* (transfer addr
                                                     (min -1
                                                          (long (* percent
                                                                   ~($.cell/long Long/MIN_VALUE))))))))


       "Transfering too much funds, insufficient amount"

       (= ($.cell/code-std* :FUNDS)
          ($.eval/exception-code ctx
                                 ($.cell/* (transfer addr
                                                     (let [balance *balance*]
                                                       (+ balance
                                                          (max 1
                                                               (long (floor (* percent
                                                                               (- ~($.cell/long Constants/MAX_SUPPLY)
                                                                                  balance)))))))))))
 

       "Transfering funds beyond authorized limit"

       (= ($.cell/code-std* :ARGUMENT)
          ($.eval/exception-code ctx
                                 ($.cell/* (transfer addr
                                                     (+ (max 1
                                                             amount)
                                                        ~($.cell/long Constants/MAX_SUPPLY))))))


       "Transfering garbage instead of funds"

       (= ($.cell/code-std* :CAST)
          ($.eval/exception-code ctx
                                 ($.cell/* (transfer addr
                                                     faulty-amount)))))))


;;;;;;;;;; Tests


(mprop/deftest account-inexistant

  {:ratio-num 5}

  (TC.prop/for-all [unused-address ($.break.gen/unused-address $.break/ctx)]
    (mprop/mult

      "Account does not exist (long)"

      ($.eval/true? $.break/ctx
                    ($.cell/* (not (account? ~unused-address))))


      "Account does not exist (address)"

      ($.eval/true? $.break/ctx
                    ($.cell/* (not (account (address ~unused-address)))))


      "Actor does not exist (long)"

      ($.eval/true? $.break/ctx
                    ($.cell/* (not (actor? ~unused-address))))


      "Actor does not exist (address)"

      ($.eval/true? $.break/ctx
                    ($.cell/* (not (actor? (address ~unused-address))))))))



(mprop/deftest main

  {:ratio-num 2}

  (TC.prop/for-all [faulty-amount $.break.gen/not-long
                    holding       $.gen/any
                    pubkey        $.gen/blob-32
                    percent       $.break.gen/percent]
    (let [ctx            ($.eval/ctx $.break/ctx
                                     ($.cell/* (def addr
                                                    (create-account ~pubkey))))
          ctx-*holdings* (ctx-holding ctx
                                      ($.cell/* *address*)
                                      holding)]
      (mprop/and (suite-*holdings* ctx-*holdings*)
                 (suite-holding ctx-*holdings*)
                 (suite-holding (ctx-holding ctx
                                             ($.cell/* addr)
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

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (account ~x))))


      "`balance`"

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (balance ~x))))


      "`get-holding`"

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (get-holding ~x))))


      "`set-controller`"

      (if (nil? (second x)) ;; `x` is quoted
        true
        (= ($.cell/code-std* :CAST)
           ($.eval/exception-code $.break/ctx
                                  ($.cell/* (set-controller ~x)))))


      "`set-holding`"

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (set-holding ~x
                                                       ~x))))


      "`transfer`"

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (transfer ~x
                                                    1))))


      "`transfer-memory`"

      (= ($.cell/code-std* :CAST)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (transfer-memory ~x
                                                           1)))))))



(mprop/deftest error-cast-key

  {:ratio-num 10}

  ;; Providing something that cannot be used as a key should fail.

  (TC.prop/for-all [x (TC.gen/such-that (fn [x]
                                          (if (or ($.std/blob? x)
                                                  ($.std/string? x))
                                            (not= ($.std/count x)
                                                  64)
                                            (some? x)))
                                        $.gen/any)]
    (= ($.cell/code-std* :CAST)
       ($.eval/exception-code $.break/ctx
                              ($.cell/* (set-key (quote ~x)))))))



(mprop/deftest error-nobody

  ;; Side-effects on adresses that should fail if the target address does not exist.

  {:ratio-num 10}

  (TC.prop/for-all [addr ($.break.gen/unused-address $.break/ctx)]
    (mprop/mult

      "`set-controller`"

      (= ($.cell/code-std* :NOBODY)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (set-controller ~addr))))


      "`set-holding`"

      (= ($.cell/code-std* :NOBODY)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (set-holding ~addr
                                                       42))))


       "`transfer`"

       (= ($.cell/code-std* :NOBODY)
          ($.eval/exception-code $.break/ctx
                                 ($.cell/* (transfer ~addr
                                                     0))))


      "Transfering allowance to unused address"

      (= ($.cell/code-std* :NOBODY)
         ($.eval/exception-code $.break/ctx
                                ($.cell/* (transfer-memory ~addr
                                          0)))))))
