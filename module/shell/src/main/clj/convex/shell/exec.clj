(ns convex.shell.exec

  "All aspects of actually executing transactions.
  
   When an error is detected, [[convex.shell.exec.fail/err]] is called."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [compile
                            eval])
  (:require [convex.cell            :as $.cell]
            [convex.cvm             :as $.cvm]
            [convex.db              :as $.db]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.err       :as $.shell.err]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.kw        :as $.shell.kw]
            [convex.std             :as $.std]))


;;;;;;;;;; Values


(def max-juice

  "Maximum juice value set on context prior to handling code."

  Long/MAX_VALUE)


;;;;;;;;;; Miscellaneous


(defn juice

  "Computes consumed juice based on the current limit."

  [env]

  (- (env :convex.shell.juice/limit)
     ($.cvm/juice (env :convex.shell/ctx))))



(defn result

  "Extracts a result from the current context attached to `env`."

  [env]

  (-> (env :convex.shell/ctx)
      ($.cvm/result)))


;;;;;;;;;; Special transactions


(defn sreq-dispatch
  
  "Dispatch function used by the [[sreq]] multimethod.
  
   Returns nil if the given result is not a special request."

  ([result]

   (when (and ($.std/vector? result)
              (>= (count result)
                  2)
              (= (.get ^AVector result
                       0)
                 $.shell.kw/cvm-sreq))
     (.get ^AVector result
           1)))


  ([_env result]

   (sreq-dispatch result)))



(defmulti sreq

  "After evaluating a transaction, the shell must check if the result is a special request.
  
   It uses [[sreq-dispatch]] to forward the result to the appropriate special request implementation, an \"unknown\"
   implementation if it looks like a special request but is not implemented, or the \"nil\" implementation if it is not
   a special request.

   Implentations of special requests are in the [[convex.shell.sreq]] namespace."

  sreq-dispatch

  :default :unknown)


;;;;;;;;;; Transactions


(defn eval

  "Evaluates `trx` after refilling juice."

  ([env]

   (eval env
         (result env)))


  ([env trx]

   (let [ctx (-> (env :convex.shell/ctx)
                 ($.cvm/juice-set (env :convex.shell.juice/limit))
                 ($.cvm/eval trx))
         ex  ($.cvm/exception ctx)]
     (-> env
         (assoc :convex.shell/ctx
                ctx)
         (cond->
           ex
           ($.shell.exec.fail/err (-> ex
                                      ($.shell.err/mappify)
                                      ($.shell.err/assoc-trx trx))))))))



(defn trx

  "Evaluates `trx` and forwards result to [[sreq]] unless an error occured."

  [env trx]
  
  ; (println :trx trx \newline)
  (let [env-2 (eval env
                    trx)]
    (if (env-2 :convex.shell/error)
      env-2
      (sreq env-2
            (result env-2)))))



(defn trx-track-juice

  "Similar to [[trx]] but requests are not performed, new state is discarded, and `$/*result*` is `[consumed-juice trx-result]`."

  [env trx]

  (let [env-2 (eval env
                    trx)]
    (if (env-2 :convex.shell/error)
      env-2
      ($.shell.ctx/def-result env
                              ($.cell/* [~($.cell/long (juice env-2))
                                         ~(result env-2)])))))



(defn trx+

  "Executes transactions located in `$.trx/*list*` in the context until that list becomes empty."

  [env]

  (loop [env-2 env]
    (let [trx+ ($.shell.ctx/current-trx+ env-2)]
      ; (println :trx+ trx+ \newline)
      (if (pos? (count trx+))
        (let [env-3 (trx ($.shell.ctx/def-trx+ env-2
                                               (.drop trx+
                                                      1))
                         (.get trx+
                               0))]
          (recur (dissoc env-3
                         :convex.shell/error)))
        (if-let [result (and (not ($.shell.ctx/active-repl? env-2))
                                  ($.shell.ctx/result env-2))]
          (-> env-2
              ($.shell.ctx/def-trx+ ($.cell/* (($.stream/!.outln (quote ~result))
                                               nil)))
              (convex.shell.exec/trx+))
          (do
            (when (env-2 :convex.shell.db/instance)
              ($.db/close))
            (when-some [exit-code (env-2 :convex.shell/exit-code)]
              (when-not (= (System/getProperty "convex.dev")
                           "true")
                (System/exit exit-code)))
            env-2))))))
