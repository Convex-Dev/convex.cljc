(ns convex.test.break.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj                    :as $.clj]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj.gen                :as $.clj.gen]
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
                                                        'addr
                                                        x)
                     $.break.test.account/suite-holding)

                 ($.break.test.account/suite-transfer ctx-2
                                                      "Transfering coins to an actor")

                 (mprop/check

                   "`accept` and `receive-coin`"

                   (mprop/mult

                     "Cannot send coin to actor without a callable `receive-coin` function"

                     ($.clj.eval/code?* ctx-2
                                        :STATE
                                        (transfer (deploy
                                                    '(defn receive-coin [origin offer no-arg]))
                                                  ($/long-percentage ~percent
                                                                     *balance*)))


                     "`receive-coin` is callable"

                     ($.clj.eval/result ctx-2
                                        '(callable? addr
                                                    'receive-coin))


                     "`accept` returns the accepted amount"

                     ($.clj.eval/result ctx-2
                                        '(lookup addr
                                                 -accept))


                     "Caller argument"

                     ($.clj.eval/result ctx-2
                                        '(lookup addr
                                                 -caller))


                     "Offer argument"

                     ($.clj.eval/result ctx-2
                                        '(lookup addr
                                                 -offer))


                     "Third argument is nil"

                     ($.clj.eval/result ctx-2
                                        '(lookup addr
                                                 -nil-arg-2))))))))


;;;;;;;;;; Tests


(mprop/deftest main

  {:ratio-num 5}

  (TC.prop/for-all [faulty-amount $.break.gen/not-long
                    holding       $.clj.gen/any
                    percent       $.break.gen/percent]
    (let [ctx ($.clj.eval/ctx* (do
                                 (def addr
                                      (deploy '(do

                                                 (def holding
                                                      ~holding)

                                                 (defn receive-coin
                                                   ~($.clj/literal "^{:callable? true}")
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
                                                           (accept offer)))))))
                                 (def addr-empty
                                      (deploy nil))))]
      (mprop/and (-> ($.break.test.account/ctx-holding ctx
                                                       'addr
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
