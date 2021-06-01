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


(defn suite-blacklist

  ""

  [ctx controller sym-actor]


  (let [ctx-2 ($.break.eval/ctx* ctx
                                 (def bl
                                      ~sym-actor))]
    ($.break.prop/mult*

          "Controller is right"
          ($.break.eval/result* ctx-2
                                (= (lookup bl
                                           'controller)
                                   ~controller))
          
          "All addresses to forbid are indeed forbidden"
          ($.break.eval/result ctx-2
                               '($/every? not-trusted?
                                          addr-black+))

          "Any not-forbidden address is trusted"
          ($.break.eval/result ctx-2
                               '($/every? trusted?
                                          addr-test+))

          "`trust/trusted?` is consistent with calling `check-trusted` on actor"
          ($.break.eval/result ctx-2
                               '($/every? (fn [addr]
                                            (= (trusted? addr)
                                               (call bl
                                                     (check-trusted? addr
                                                                     nil
                                                                     nil))))
                                          addr-all+)))))


;;;;;;;;;; Tests


($.break.prop/deftest blacklist

  (TC.prop/for-all [[addr-black+
                     addr-test+] (TC.gen/bind (TC.gen/vector $.lisp.gen/address)
                                              (fn [addr-black+]
                                                (TC.gen/tuple (TC.gen/return addr-black+)
                                                              (TC.gen/vector-distinct (TC.gen/such-that (comp not
                                                                                                              (partial contains?
                                                                                                                       (set addr-black+)))
                                                                                                        $.lisp.gen/address)
                                                                                      {:min-elements 1}))))
                    not-caller   (TC.gen/such-that (let [addr ($.break.eval/result ctx
                                                                                   '*address*)]
                                                     #(not (= %
                                                              addr)))
                                                   $.lisp.gen/address)]
    (let [ctx-2 ($.break.eval/ctx* ctx
                                   (do
                                     (def addr-black+
                                          ~addr-black+)

                                     (def addr-test+
                                          ~addr-test+)

                                     (def addr-all+
                                          (into #{}
                                                (concat addr-black+
                                                        addr-test+)))

                                     (def actor-controlled
                                          (deploy (trust/build-blacklist {:blacklist addr-black+})))

                                     (def actor-uncontrolled
                                          (deploy (trust/build-blacklist {:blacklist  addr-black+
                                                                          :controller ~not-caller})))

                                     ;; `bl` is defined by [[suite-blacklist]]

                                     (defn trusted? [addr]
                                       (trust/trusted? bl
                                                       addr))

                                     (defn not-trusted? [addr]
                                       (not (trusted? addr)))))]
      ($.break.prop/and* ($.break.prop/checkpoint*

                           "Controller is caller"
                           (suite-blacklist ctx-2
                                            '*address*
                                            'actor-controlled))

                         ($.break.prop/checkpoint*

                           "Controller is not caller"
                           (suite-blacklist ctx-2
                                            not-caller
                                            'actor-uncontrolled))

                         ($.break.prop/mult*

                           "Adding to blacklist with `set-trusted`"
                           ($.break.eval/result ctx-2
                                                '(do
                                                   ($/foreach (fn [addr]
                                                                (call actor-controlled
                                                                      (set-trusted addr
                                                                                   false)))
                                                              addr-test+)
                                                   (= (lookup actor-controlled
                                                              'blacklist)
                                                      addr-all+)))
                 
                           "Removing from blacklist with `set-trusted`"
                           ($.break.eval/result ctx-2
                                                '(do
                                                   ($/foreach (fn [addr]
                                                                (call actor-controlled
                                                                      (set-trusted addr
                                                                                   true)))
                                                              addr-black+)
                                                   (= (lookup actor-controlled
                                                              'blacklist)
                                                      #{})))

                           "Not changing status with `set-trusted`"
                           ($.break.eval/result ctx-2
                                                '(do
                                                   (let [blacklist-before (lookup actor-controlled
                                                                                  'blacklist)]
                                                     ($/foreach (fn [addr]
                                                                  (call actor-controlled
                                                                        (set-trusted addr
                                                                                     false)))
                                                                addr-black+)
                                                     ($/foreach (fn [addr]
                                                                  (call actor-controlled
                                                                        (set-trusted addr
                                                                                     true)))
                                                                addr-test+)
                                                     (= (lookup actor-controlled
                                                                'blacklist)
                                                        blacklist-before)))))
                         ))))
