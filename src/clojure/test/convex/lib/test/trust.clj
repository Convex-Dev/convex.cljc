(ns convex.lib.test.trust

  "Testing the Trust library."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.prop             :as $.break.prop]
            [convex.cvm                    :as $.cvm]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp.gen               :as $.lisp.gen]))


;;;;;;;;;; CVM context


(def ctx

  ""

  ($.cvm/import {"src/convex/break/util.cvx" '$
                 "src/convex/lib/trust.cvx"  'trust}))


;;;;;;;;;; Suites


(defn suite-list-get

  ""

  [ctx controller sym-actor]


  (let [ctx-2 ($.break.eval/ctx* ctx
                                 (def actor
                                      ~sym-actor))]
    ($.break.prop/mult*

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



(defn suite-list-set

  ""

  [ctx sym-lookup-list]

  (let [ctx-2 ($.break.eval/ctx* ctx
                                 (def sym-lookup
                                      (quote ~sym-lookup-list)))]
    ))



(defn suite-list

  ""

  [ctx not-caller f-list-set]

  (let [ctx-2 ($.break.eval/ctx ctx
                                '(def addr-all+
                                      (into #{}
                                            (concat addr-allow+
                                                    addr-forbid+))))]
    ($.break.prop/and* ($.break.prop/checkpoint*

                         "Getting trust, controller is caller"

                         (suite-list-get ctx-2
                                         '*address*
                                         'actor-controlled))

                       ($.break.prop/checkpoint*

                         "Getting trust, controller is not caller"

                         (suite-list-get ctx-2
                                         not-caller
                                         'actor-uncontrolled))

                       ($.break.prop/checkpoint*

                         "Setting trust"

                         (f-list-set ctx-2)))))


;;;;;;;;;; Generators


(def gen-addr-list+

  ""

  (TC.gen/bind (TC.gen/vector $.lisp.gen/address)
               (fn [addr-forbid+]
                 (TC.gen/tuple (TC.gen/vector-distinct (TC.gen/such-that (comp not
                                                                               (partial contains?
                                                                                        (set addr-forbid+)))
                                                                         $.lisp.gen/address)
                                                       {:min-elements 1})
                               (TC.gen/return addr-forbid+)))))


(def gen-not-caller

  ""

  (TC.gen/such-that (let [addr ($.break.eval/result ctx
                                                    '*address*)]
                      #(not (= %
                               addr)))
                    $.lisp.gen/address))


;;;;;;;;;; Tests


($.break.prop/deftest blacklist

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
                    ($.break.prop/mult*

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



($.break.prop/deftest whitelist

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
                    ($.break.prop/mult*

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
