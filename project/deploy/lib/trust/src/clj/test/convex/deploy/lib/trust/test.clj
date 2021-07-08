(ns convex.deploy.lib.test.trust

  "Testing the Trust library."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj.gen                :as $.clj.gen]
            [convex.cvm                    :as $.cvm]
            [convex.data                   :as $.data]
            [convex.sync                   :as $.sync]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; CVM context


(def ctx

  "Base context for this namespace."

  (-> ($.sync/disk {'$     (-> "convex/break.cvx"
                               clojure.java.io/resource
                               .openStream)
                    'trust (-> "convex/trust.cvx"
                               clojure.java.io/resource
                               .openStream)})
      :convex.sync/ctx
      ($.clj.eval/ctx '(do
                         (set-key (blob "0000000000000000000000000000000000000000000000000000000000000000"))
                         (def $
                              (deploy (first $)))
                         (def trust
                              (deploy (first trust)))))
      $.cvm/juice-refill))


;;;;;;;;;; Suites


(defn suite-list-get

  "Used by [[suite-list]].
  
   Tests reading trust."

  [ctx controller sym-actor]


  (let [ctx-2 ($.clj.eval/ctx* ctx
                               (def actor
                                    ~sym-actor))]
    (mprop/mult

      "Controller is right"

      ($.clj.eval/result* ctx-2
                          (= (lookup actor
                                     controller)
                             ~controller))
      

      "All forbidden addresses are not trusted"

      ($.clj.eval/result ctx-2
                         '($/every? (fn [addr]
                                      (not (trust/trusted? actor
                                                           addr)))
                                    addr-forbid+))


      "All allowed addresses are trusted"

      ($.clj.eval/result ctx-2
                         '($/every? (fn [addr]
                                      (trust/trusted? actor
                                                      addr))
                                    addr-allow+))


      "`trust/trusted?` is consistent with calling `check-trusted` on actor"

      ($.clj.eval/result ctx-2
                         '($/every? (fn [addr]
                                      (= (trust/trusted? actor
                                                         addr)
                                         (call actor
                                               (check-trusted? addr
                                                               nil
                                                               nil))))
                                    addr-all+)))))



(defn suite-list

  "Suite used by [[blacklist]] and [[whitelist]]."

  [ctx not-caller f-list-set]

  (let [ctx-2 ($.clj.eval/ctx ctx
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

  (TC.gen/bind (TC.gen/vector $.clj.gen/address)
               (fn [addr-forbid+]
                 (TC.gen/tuple (TC.gen/vector-distinct (TC.gen/such-that (comp not
                                                                               (partial contains?
                                                                                        (set addr-forbid+)))
                                                                         $.clj.gen/address)
                                                       {:min-elements 1})
                               (TC.gen/return addr-forbid+)))))


(def gen-not-caller

  "Generates an address that is different from the caller."

  (TC.gen/such-that (let [addr ($.clj.eval/result ctx
                                                  '*address*)]
                      #(not (= %
                               addr)))
                    $.clj.gen/address))


;;;;;;;;;; Tests


(mprop/deftest blacklist

  ;; Creating a blacklist.

  (TC.prop/for-all [[addr-allow+
                     addr-forbid+] gen-addr-list+
                    not-caller     gen-not-caller]
    (let [ctx-2 ($.clj.eval/ctx* ctx
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

                      ($.clj.eval/result ctx-3
                                         '(do
                                            ($/foreach (fn [addr]
                                                         (call actor-controlled
                                                               (set-trusted addr
                                                                            false)))
                                                       addr-allow+)
                                            (= (lookup actor-controlled
                                                       blacklist)
                                               addr-all+)))
                      

                      "Adding trust with `set-trusted`"

                      ($.clj.eval/result ctx-3
                                         '(do
                                            ($/foreach (fn [addr]
                                                         (call actor-controlled
                                                               (set-trusted addr
                                                                            true)))
                                                       addr-forbid+)
                                            (= (lookup actor-controlled
                                                       blacklist)
                                               #{})))
                

                      "Not changing trust with `set-trusted`"

                      ($.clj.eval/result ctx-3
                                         '(do
                                            (let [listing-before (lookup actor-controlled
                                                                         blacklist)]
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
                                                         blacklist)
                                                 listing-before))))))))))



(mprop/deftest whitelist

  ;; Creating a whitelist.

  (TC.prop/for-all [[addr-allow+
                     addr-forbid+] gen-addr-list+
                    not-caller     gen-not-caller]
    (let [ctx-2 ($.clj.eval/ctx* ctx
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

                      ($.clj.eval/result ctx-3
                                         '(do
                                            ($/foreach (fn [addr]
                                                         (call actor-controlled
                                                               (set-trusted addr
                                                                            false)))
                                                       addr-allow+)
                                            (= (lookup actor-controlled
                                                       whitelist)
                                               #{})))
                      

                      "Adding trust with `set-trusted`"

                      ($.clj.eval/result ctx-3
                                         '(do
                                            ($/foreach (fn [addr]
                                                         (call actor-controlled
                                                               (set-trusted addr
                                                                            true)))
                                                       addr-forbid+)
                                            (= (lookup actor-controlled
                                                       whitelist)
                                               addr-all+)))
                

                      "Not changing trust with `set-trusted`"

                      ($.clj.eval/result ctx-3
                                         '(do
                                            (let [listing-before (lookup actor-controlled
                                                                         whitelist)]
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
                                                         whitelist)
                                                 listing-before))))))))))



(mprop/deftest upgrade

  ;; Creating an upgradable actor.

  {:ratio-num 2}

  (TC.prop/for-all [upgrade-data $.clj.gen/any
                    upgrade-sym  $.clj.gen/symbol]
    (let [ctx-2 ($.clj.eval/ctx* ctx
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

        ($.clj.eval/result ctx-2
                           '(= *address*
                               (lookup actor-controlled
                                       upgradable-root)))


        "Root can be set via options"

        ($.clj.eval/result ctx-2
                           '(= blacklist
                               (lookup actor-uncontrolled
                                       upgradable-root)))


        "Can eval code in controlled actor"

        ($.clj.eval/result* ctx-2
                            (do
                              (upgrade actor-controlled)
                              (= ~upgrade-data
                                 (lookup actor-controlled
                                         ~upgrade-sym))))


        "Cannot eval code after giving up root access"

        ($.clj.eval/code? ctx-2
                          ($.data/code-std* :STATE)
                          '(do
                             (trust/remove-upgradability! actor-controlled)
                             (upgrade actor-controlled)))


        "Cannot eval code in uncontrolled actor"

        ($.clj.eval/code? ctx-2
                          ($.data/code-std* :TRUST)
                          '(upgrade actor-uncontrolled))


        "Cannot remove upgradability in uncontrolled actor"

        ($.clj.eval/code? ctx-2
                          ($.data/code-std* :TRUST)
                          '(trust/remove-upgradability! actor-uncontrolled))))))
