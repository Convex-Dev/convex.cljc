(ns convex.shell.req.bench

  "Requests related to benchmarking."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [eval])
  (:require [convex.cell    :as $.cell]
            [convex.cvm     :as $.cvm]
            [criterium.core :as criterium]))


;;;;;;;;;;


(defn eval

  "Request for benchmarking some code using Criterium."

  [ctx [code]]

  (let [stat+ (criterium/benchmark* (fn []
                                      (-> ctx
                                          ($.cvm/fork)
                                          ($.cvm/eval code)))
                                    {})]
    ($.cvm/result-set ctx
                      ($.cell/* {:mean   ~($.cell/double (first (stat+ :mean)))
                                 :stddev ~($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))

