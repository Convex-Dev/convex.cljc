(ns convex.shell.req.bench

  (:require [convex.cell    :as $.cell]
            [convex.cvm     :as $.cvm]
            [criterium.core :as criterium]))


;;;;;;;;;;


(defn trx

  ;; Benchmarks a transaction using Criterium.

  [ctx [trx]]

  (let [stat+ (criterium/benchmark* (fn []
                                      (-> ctx
                                          ($.cvm/fork)
                                          ($.cvm/eval trx)))
                                    {})]
    ($.cvm/result-set ctx
                      ($.cell/* {:mean   ~($.cell/double (first (stat+ :mean)))
                                 :stddev ~($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))

