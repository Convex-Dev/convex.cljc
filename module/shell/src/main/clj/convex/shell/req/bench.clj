(ns convex.shell.req.bench

  "Requests related to benchmarking."

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.transactions ATransaction))
  (:refer-clojure :exclude [eval])
  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.cvm                    :as $.cvm]
            [convex.shell.req.gen          :as $.shell.req.gen]
            [convex.shell.req.trx          :as $.shell.req.trx]
            [criterium.core                :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(defn- -criterium

  ;; Benchmarks a function and attaches the result to `ctx`.

  [ctx f-bench f-result option+]

  (let [stat+        (criterium/benchmark* f-bench
                                           option+)
        avg          (first (stat+ :mean))
        overhead     (stat+ :overhead)
        option-2+    (stat+ :options)
        sample-count (option-2+ :samples)]
    ($.cvm/result-set ctx
                      (-> ($.cell/* {:avg            ~($.cell/double avg)
                                     :exec.count     ~($.cell/long (* sample-count
                                                                      (stat+ :execution-count)))
                                     :Hz             ~($.cell/double (/ 1
                                                                        avg))
                                     :overhead       ~($.cell/double overhead)
                                     :overhead.ratio ~($.cell/double (/ overhead
                                                                        avg))
                                     :sample.count   ~($.cell/long sample-count)
                                     :sample.time    ~($.cell/long (option-2+ :target-execution-time))
                                     :stddev         ~($.cell/double (Math/sqrt ^double (first (stat+ :variance))))
                                     :time           ~($.cell/long (reduce +
                                                                           0
                                                                           (stat+ :samples)))})
                          (f-result)))))



(defn- -overhead
 
  ;;

  [f]

  (-> (criterium/benchmark* f
                            {
                             })
      (:lower-q)
      (first)))


;;;;;;;;;;


(defn eval

  "Request for benchmarking some code using Criterium."

  [ctx [code]]

  (-criterium ctx
              (fn []
                (-> ctx
                    ($.cvm/fork)
                    ($.cvm/eval code)))
              identity
              {}))



(defn trx

  "Request for benchmarking a transaction."

  [ctx [^ATransaction trx]]

  (or ($.shell.req.trx/-ensure-trx ctx
                                   trx)
      (-criterium ctx
                  (let [^State state ($.cvm/state ctx)]
                    (fn []
                      (.applyTransaction state
                                         trx)))
                  identity
                  {})))



(defn trx-gen

  "Request for benchmarking generated transaction (without throwing away the state)."

  ;; Sample count must be >= 3

  [ctx [gen sample-count sample-time]]

  (let [ctx-2    ($.cvm/fork ctx)
        gen-2    @(first gen)
        v*state  (volatile! ($.cvm/state ctx))
        overhead (-overhead (fn []
                              (vswap! v*state
                                      (fn [state]
                                        (binding [$.shell.req.gen/-*ctx* ($.cvm/state-set ctx-2
                                                                                          state)]
                                          (TC.gen/generate gen-2))
                                        ($.cvm/state ctx)
                                        state))))]
    (-criterium ctx
                (fn []
                  (vswap! v*state
                          (fn [state]
                            (-> (.applyTransaction ^State @v*state
                                                   (binding [$.shell.req.gen/-*ctx* ($.cvm/state-set ctx-2
                                                                                                     state)]
                                                     (TC.gen/generate gen-2)))
                                ($.cvm/state)))))
                identity
                {:overhead              overhead
                 :samples               ($.clj/long sample-count)
                 :target-execution-time ($.clj/long sample-time)})))
