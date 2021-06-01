(ns convex.break.test.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.lisp.gen]
            [convex.break.eval             :as $.test.eval]
            [convex.break.gen              :as $.break.gen]
            [convex.break.prop             :as $.break.prop]
            [convex.break.test.account     :as $.break.test.account]))



;;;;;;;;;; Suites


(defn suite-transfer

  "Tests transfering coins to an actor which is more complex than to a regular account."

  [ctx faulty-amount percent x]

  ($.break.prop/checkpoint*

    "Transfering coins"

    (let [ctx-2 ($.break.test.account/ctx-transfer ctx
                                                   faulty-amount
                                                   percent)]
      ($.break.prop/and* (-> ($.break.test.account/ctx-holding ctx-2
                                                               'addr
                                                               x)
                            $.break.test.account/suite-holding)
                        ($.break.test.account/suite-transfer ctx-2
                                                             "Transfering coins to an actor")
                        ($.break.prop/checkpoint*

                          "`accept` and `receive-coin`"
                          ($.break.prop/mult*

                            "Cannot send coin to actor without an exported `receive-coin` function"
                            ($.test.eval/error-state?* ctx-2
                                                       (transfer (deploy
                                                                   '(defn receive-coin [origin offer no-arg]))
                                                                 ($/long-percentage ~percent
                                                                                    *balance*)))

                            "`receive-coin` is exported"
                            ($.test.eval/result ctx-2
                                                '(exports? addr
                                                           'receive-coin))

                            "`accept` returns the accepted amount"
                            ($.test.eval/result ctx-2
                                                '(lookup addr
                                                         '-accept))

                            "Caller argument"
                            ($.test.eval/result ctx-2
                                                '(lookup addr
                                                         '-caller))

                            "Offer argument"
                            ($.test.eval/result ctx-2
                                                '(lookup addr
                                                         '-offer))

                            "Third argument is nil"
                            ($.test.eval/result ctx-2
                                                '(lookup addr
                                                         '-nil-arg-2))))))))


;;;;;;;;;; Tests


($.break.prop/deftest main

  (TC.prop/for-all [faulty-amount  $.break.gen/not-long
                    percent        $.break.gen/percent
                    x              $.lisp.gen/any]
    (let [ctx ($.test.eval/ctx* (do
                                  (def addr
                                       (deploy '(do

                                                  (def x
                                                       ~x)

                                                  (defn receive-coin
                                                    [origin offer no-arg]
                                                    (def -caller
                                                         (= origin
                                                            *caller*))
                                                    (def -offer
                                                         (= offer
                                                            *offer*))
                                                    (def -nil-arg-2
                                                          (nil? no-arg))
                                                    (def -accept
                                                         (= offer
                                                            (accept offer))))

                                                  (export receive-coin))))
                                  (def addr-empty
                                       (deploy nil))))]
      ($.break.prop/and* (-> ($.break.test.account/ctx-holding ctx
                                                               'addr
                                                               x)
                             $.break.test.account/suite-holding)
                         ($.break.test.account/suite-new ctx
                                                         true?)
                         (suite-transfer ctx
                                         faulty-amount
                                         percent
                                         x)
                         ($.break.test.account/suite-transfer-memory ctx
                                                                     faulty-amount
                                                                     percent)))))
