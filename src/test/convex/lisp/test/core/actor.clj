(ns convex.lisp.test.core.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.core.account :as $.test.core.account]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;; Suites


(defn suite-transfer

  "Tests transfering coins to an actor which is more complex than to a regular account."

  [ctx faulty-amount percent x]

  ($.test.prop/checkpoint*

    "Transfering coins"

    (let [ctx-2 ($.test.core.account/ctx-transfer ctx
                                                  faulty-amount
                                                  percent)]
      ($.test.prop/and* (-> ($.test.core.account/ctx-holding ctx-2
                                                             'addr
                                                             x)
                            $.test.core.account/suite-holding)
                        ($.test.core.account/suite-transfer ctx-2
                                                            "Transfering coins to an actor")
                        ($.test.prop/checkpoint*

                          "`accept` and `receive-coin`"
                          ($.test.prop/mult*

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


($.test.prop/deftest main

  (TC.prop/for-all [faulty-amount  $.test.gen/not-long
                    percent        $.test.gen/percent
                    x              $.gen/any]
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
      ($.test.prop/and* (-> ($.test.core.account/ctx-holding ctx
                                                             'addr
                                                             x)
                            $.test.core.account/suite-holding)
                        ($.test.core.account/suite-new ctx
                                                       true?)
                        (suite-transfer ctx
                                        faulty-amount
                                        percent
                                        x)
                        ($.test.core.account/suite-transfer-memory ctx
                                                                   faulty-amount
                                                                   percent)))))
