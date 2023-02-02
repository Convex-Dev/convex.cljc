(ns convex.shell.req.bench

  "Requests related to benchmarking."

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.transactions ATransaction))
  (:refer-clojure :exclude [eval])
  (:require [convex.cell          :as $.cell]
            [convex.cvm           :as $.cvm]
            [convex.shell.req.trx :as $.shell.req.trx]
            [criterium.core       :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(defn- -criterium

  [ctx f]

  (let [stat+ (criterium/benchmark* f
                                    {})]
    ($.cvm/result-set ctx
                      ($.cell/* {:mean   ~($.cell/double (first (stat+ :mean)))
                                 :stddev ~($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))


;;;;;;;;;;


(defn eval

  "Request for benchmarking some code using Criterium."

  [ctx [code]]

  (-criterium ctx
              (fn []
                (-> ctx
                    ($.cvm/fork)
                    ($.cvm/eval code)))))



(defn trx

  "Request for benchmarking a transaction."

  [ctx [^ATransaction trx]]

  (or ($.shell.req.trx/-ensure-trx ctx
                                   trx)
      (-criterium ctx
                  (let [^State state ($.cvm/state ctx)]
                    (fn []
                      (.applyTransaction state
                                         trx))))))
