(ns convex.shell.req.state

  (:import (convex.core State))
  (:require [convex.cell       :as $.cell]
            [convex.cvm        :as $.cvm]
            [convex.shell.fail :as $.shell.fail]
            [convex.std        :as $.std]))


;;;;;;;;;;


(defn- -ensure-state

  [ctx state]

  (when-not (instance? State
                       state)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/string "Can only load a state"))))


;;;;;;;;;;


(defn safe

  [ctx [f]]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Can only execute a no-arg function")))
      (let [ctx-2 (-> ctx
                      ($.cvm/fork)
                      ($.cvm/invoke f
                                    ($.cvm/arg+*)))
            ex    ($.cvm/exception ctx-2)]
        (if ex
          ($.cvm/result-set ctx
                            ($.cell/* [false
                                       ~($.shell.fail/mappify-cvm-ex ex)]))
          ($.cvm/result-set ctx-2
                            ($.cell/* [true
                                       ~($.cvm/result ctx-2)]))))))



(defn switch

  [ctx [^State state result ]]

  (or (-ensure-state ctx
                     state)
      (-> ctx
          ($.cvm/state-set (.putAccount state
                                        ($.cell/address 8)
                                        ($.cvm/account ctx
                                                       ($.cell/address 8))))
          ($.cvm/result-set result))))
