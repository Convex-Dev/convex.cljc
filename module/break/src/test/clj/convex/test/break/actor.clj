(ns convex.test.break.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.break.gen              :as $.break.gen]
            [convex.test.break.account     :as $.break.test.account]
            [helins.mprop                  :as mprop]))



;;;;;;;;;; Suites


(defn suite-transfer

  "Tests transfering coins to an actor which is more complex than to a regular account."

  [ctx faulty-amount percent x]

  (mprop/check

    "Transfering coins"

    (let [ctx-2 ($.break.test.account/ctx-transfer ctx
                                                   faulty-amount
                                                   percent)]
      (mprop/and (-> ($.break.test.account/ctx-holding ctx-2
                                                       ($.cell/* addr)
                                                       x)
                     $.break.test.account/suite-holding)

                 ($.break.test.account/suite-transfer ctx-2
                                                      "Transfering coins to an actor")

                 (mprop/check

                   "`accept` and `receive-coin`"

                   (mprop/mult

                     "Cannot send coin to actor without a callable `receive-coin` function"

                     (= ($.cell/code-std* :STATE)
                        ($.eval/exception-code ctx-2
                                               ($.cell/* (transfer (deploy
                                                                     '(defn receive-coin [origin offer no-arg]))
                                                                   ($/long-percentage ~percent
                                                                                      *balance*)))))


                     "`receive-coin` is callable"

                     ($.eval/true? ctx-2
                                   ($.cell/* (callable? addr
                                                        'receive-coin)))


                     "`accept` returns the accepted amount"

                     ($.eval/true? ctx-2
                                   ($.cell/* (lookup addr
                                                     -accept)))


                     "Caller argument"

                     ($.eval/true? ctx-2
                                   ($.cell/* (lookup addr
                                                     -caller)))


                     "Offer argument"

                     ($.eval/true? ctx-2
                                   ($.cell/* (lookup addr
                                                     -offer)))


                     "Third argument is nil"

                     ($.eval/true? ctx-2
                                   ($.cell/* (lookup addr
                                                     -nil-arg-2)))))))))


;;;;;;;;;; Tests


(mprop/deftest main

  {:ratio-num 5}

  (TC.prop/for-all [faulty-amount $.break.gen/not-long
                    holding       $.gen/any
                    percent       $.break.gen/percent]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def addr
                                           (deploy '(do

                                                      (def holding
                                                           (quote ~holding))

                                                      (defn receive-coin
                                                        ~($.cell/syntax ($.cell/* [origin offer no-arg])
                                                                        ($.cell/* {:callable? true}))
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
                                                                (accept offer)))))))
                                      (def addr-empty
                                           (deploy nil)))))]
      (mprop/and (-> ($.break.test.account/ctx-holding ctx
                                                       ($.cell/* addr)
                                                       holding)
                     $.break.test.account/suite-holding)

                 ($.break.test.account/suite-new ctx
                                                 true?)

                 (suite-transfer ctx
                                 faulty-amount
                                 percent
                                 holding)

                 ($.break.test.account/suite-transfer-memory ctx
                                                             faulty-amount
                                                             percent)))))
