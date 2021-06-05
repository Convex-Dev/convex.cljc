(ns convex.lib.test.trust

  "Testing the Trust library."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.cvm                    :as $.cvm]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; CVM context


(def ctx

  "Base context for this namespace."

  ($.cvm/import {"src/convex/break/util.cvx" '$
                 "src/convex/lib/trust.cvx"  'trust}))


;;;;;;;;;; Suites


(defn suite-list-get

  "Used by [[suite-list]].
  
   Tests reading trust."

  [ctx controller sym-actor]


  (let [ctx-2 ($.break.eval/ctx* ctx
                                 (def actor
                                      ~sym-actor))]
    (mprop/mult

      "Controller is right"

      ($.break.eval/result* ctx-2
                            (= (lookup actor
                                       'controller)
                               ~controller))
      

      "All forbidden addresses are not trusted"

      ($.break.eval/result ctx-2
                           '($/every? (fn [addr]
                                        (not (trust/trusted? actor
                                                             addr)))
                                      addr-forbid+))


      "All allowed addresses are trusted"

      ($.break.eval/result ctx-2
                           '($/every? (fn [addr]
                                        (trust/trusted? actor
                                                        addr))
                                      addr-allow+))


      "`trust/trusted?` is consistent with calling `check-trusted` on actor"

      ($.break.eval/result ctx-2
                           '($/every? (fn [addr]
                                        (= (trust/trusted? actor
                                                           addr)
                                           (call actor
                                                 (check-trusted? addr
                                                                 nil
                                                                 nil))))
                                      addr-all+)))))



(defn suite-list

  "Suite used by [[blacklist]] and [[white-list]]."

  [ctx not-caller f-list-set]

  (let [ctx-2 ($.break.eval/ctx ctx
                                '(def addr-all+
                                      (into #{}
                                            (concat addr-allow+
                                                    addr-forbid+))))]
    (mprop/mult

      "Getting trust, controller is caller"

      (suite-list-get ctx-2
                      '*address*
                      'actor-controlled)


      "Getting trust, controller is not caller"

      (suite-list-get ctx-2
                      not-caller
                      'actor-uncontrolled)


      "Setting trust"

      (f-list-set ctx-2))))


;;;;;;;;;; Generators


(def gen-addr-list+

  "Generates a pair of `[addr-allow+ addr-forbid+]`."

  (TC.gen/bind (TC.gen/vector $.lisp.gen/address)
               (fn [addr-forbid+]
                 (TC.gen/tuple (TC.gen/vector-distinct (TC.gen/such-that (comp not
                                                                               (partial contains?
                                                                                        (set addr-forbid+)))
                                                                         $.lisp.gen/address)
                                                       {:min-elements 1})
                               (TC.gen/return addr-forbid+)))))


(def gen-not-caller

  "Generates an address that is different from the caller."

  (TC.gen/such-that (let [addr ($.break.eval/result ctx
                                                    '*address*)]
                      #(not (= %
                               addr)))
                    $.lisp.gen/address))


;;;;;;;;;; Tests


(mprop/deftest blacklist

  ;; Creating a blacklist.

  (TC.prop/for-all [[addr-allow+
                     addr-forbid+] gen-addr-list+
                    not-caller     gen-not-caller]
    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def addr-allow+
                                          ~addr-allow+)

                                     (def addr-forbid+
                                          ~addr-forbid+)

                                     (def actor-controlled
                                          (deploy (trust/build-blacklist {:blacklist addr-forbid+})))

                                     (def actor-uncontrolled
                                          (deploy (trust/build-blacklist {:blacklist  addr-forbid+
                                                                          :controller ~not-caller})))))]
      (suite-list ctx-2
                  not-caller
                  (fn [ctx-3]
                    (mprop/mult

                      "Removing trust with `set-trust`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              ($/foreach (fn [addr]
                                                           (call actor-controlled
                                                                 (set-trusted addr
                                                                              false)))
                                                         addr-allow+)
                                              (= (lookup actor-controlled
                                                         'blacklist)
                                                 addr-all+)))
                      

                      "Adding trust with `set-trusted`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              ($/foreach (fn [addr]
                                                           (call actor-controlled
                                                                 (set-trusted addr
                                                                              true)))
                                                         addr-forbid+)
                                              (= (lookup actor-controlled
                                                         'blacklist)
                                                 #{})))
                

                      "Not changing trust with `set-trusted`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              (let [listing-before (lookup actor-controlled
                                                                           'blacklist)]
                                                ($/foreach (fn [addr]
                                                             (call actor-controlled
                                                                   (set-trusted addr
                                                                                true)))
                                                           addr-allow+)
                                                ($/foreach (fn [addr]
                                                             (call actor-controlled
                                                                   (set-trusted addr
                                                                                false)))
                                                           addr-forbid+)
                                                (= (lookup actor-controlled
                                                           'blacklist)
                                                   listing-before))))))))))



(mprop/deftest whitelist

  ;; Creating a whitelist.

  (TC.prop/for-all [[addr-allow+
                     addr-forbid+] gen-addr-list+
                    not-caller     gen-not-caller]
    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def addr-allow+
                                          ~addr-allow+)

                                     (def addr-forbid+
                                          ~addr-forbid+)

                                     (def actor-controlled
                                          (deploy (trust/build-whitelist {:whitelist addr-allow+})))

                                     (def actor-uncontrolled
                                          (deploy (trust/build-whitelist {:controller ~not-caller
                                                                          :whitelist  addr-allow+ })))))]
      (suite-list ctx-2
                  not-caller
                  (fn [ctx-3]
                    (mprop/mult

                      "Removing trust with `set-trust`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              ($/foreach (fn [addr]
                                                           (call actor-controlled
                                                                 (set-trusted addr
                                                                              false)))
                                                         addr-allow+)
                                              (= (lookup actor-controlled
                                                         'whitelist)
                                                 #{})))
                      

                      "Adding trust with `set-trusted`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              ($/foreach (fn [addr]
                                                           (call actor-controlled
                                                                 (set-trusted addr
                                                                              true)))
                                                         addr-forbid+)
                                              (= (lookup actor-controlled
                                                         'whitelist)
                                                 addr-all+)))
                

                      "Not changing trust with `set-trusted`"

                      ($.break.eval/result ctx-3
                                           '(do
                                              (let [listing-before (lookup actor-controlled
                                                                           'whitelist)]
                                                ($/foreach (fn [addr]
                                                             (call actor-controlled
                                                                   (set-trusted addr
                                                                                true)))
                                                           addr-allow+)
                                                ($/foreach (fn [addr]
                                                             (call actor-controlled
                                                                   (set-trusted addr
                                                                                false)))
                                                           addr-forbid+)
                                                (= (lookup actor-controlled
                                                           'whitelist)
                                                   listing-before))))))))))



(mprop/deftest upgrade

  ;; Creating an upgradable actor.

  (TC.prop/for-all [upgrade-data $.lisp.gen/any
                    upgrade-sym  $.lisp.gen/symbol]
    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def actor-controlled
                                          (deploy (trust/add-trusted-upgrade nil)))

                                     (def blacklist
                                          (deploy (trust/build-blacklist {:blacklist [*address*]})))

                                     (def actor-uncontrolled
                                          (deploy (trust/add-trusted-upgrade {:root blacklist})))
                                     
                                     (defn upgrade [actor]
                                       (call actor
                                             (upgrade (quote (def ~upgrade-sym
                                                                  ~upgrade-data)))))))]
      (mprop/mult

        "Root is caller by default"

        ($.break.eval/result ctx-2
                             '(= *address*
                                 (lookup actor-controlled
                                         'upgradable-root)))


        "Root can be set via options"

        ($.break.eval/result ctx-2
                             '(= blacklist
                                 (lookup actor-uncontrolled
                                         'upgradable-root)))


        "Can eval code in controlled actor"

        ($.break.eval/result* ctx-2
                              (do
                                (upgrade actor-controlled)
                                (= ~upgrade-data
                                   (lookup actor-controlled
                                           (quote ~upgrade-sym)))))


        "Cannot eval code after giving up root access"

        ($.break.eval/error-state? ctx-2
                                   '(do
                                      (trust/remove-upgradability! actor-controlled)
                                      (upgrade actor-controlled)))


        "Cannot eval code in uncontrolled actor"

        ($.break.eval/error-trust? ctx-2
                                   '(upgrade actor-uncontrolled))


        "Cannot remove upgradability in uncontrolled actor"

        ($.break.eval/error-trust? ctx-2
                                   '(trust/remove-upgradability! actor-uncontrolled))))))
