(ns convex.run.exec

  "All aspects of actually executing transactions.

   When an error is detected, [[fail]] is called."

  {:author "Adam Helinski"}

  (:import (convex.core.data AMap
                             AVector))
  (:refer-clojure :exclude [compile
                            eval])
  (:require [convex.cell    :as $.cell]
            [convex.cvm     :as $.cvm]
            [convex.read    :as $.read]
            [convex.std     :as $.std]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]))


(declare fail)


;;;;;;;;;; Values


(def max-juice

  "Maximum juice value set on context prior to handling code."

  Long/MAX_VALUE)


;;;;;;;;;; Miscellaneous


(defn juice

  "Computes consumed juice, extracting [[max-juice]] from the current value."

  [env]

  (- max-juice
     (-> env
         :convex.run/ctx
         $.cvm/juice)))



(defn result

  "Extracts a result from the current context attached to `env`."

  [env]

  (-> env
      :convex.run/ctx
      $.cvm/result))



(defn update-ctx

  "Refills the current context with maximum juice and calls `f` with that context and `trx`.

   The context is then reattached to `env`."

  [env kw-phase f trx]

  (let [ctx (f (-> env
                   :convex.run/ctx
                   $.cvm/juice-refill)
               trx)
        ex  ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.run/ctx
             ctx)
      ex
      (fail (-> ($.run.err/mappify ex)
                ($.run.err/assoc-phase kw-phase)
                ($.run.err/assoc-trx trx))))))


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
                 $.run.kw/cvm-sreq))
     (.get ^AVector result
           1)))


  ([_env result]

   (sreq-dispatch result)))



(defmulti sreq

  "After evaluating a transaction, the runner must check if the result is a special request.

   It uses [[sreq-dispatch]] to forward the result to the appropriate special request implementation, an \"unknown\"
   implementation if it looks like a special request but is not implemented, or the \"nil\" implementation if it is not
   a special request.

   Implentations of special requests are in the [[convex.run.sreq]] namespace."

  sreq-dispatch

  :default :unknown)


;;;;;;;;;; Execution steps


(defn expand

  "Expands the given `trx` using the current context."


  ([env]

   (expand env
           (result env)))


  ([env trx]

   (update-ctx env
               $.run.kw/expand
               $.cvm/expand
               trx)))



(defn compile

  "Compiles the given, previously expanded `trx` using the current context.

   See [[expand]]."


  ([env]

   (compile env
            (result env)))


  ([env trx-canonical]

   (update-ctx env
               $.run.kw/compile
               $.cvm/compile
               trx-canonical)))



(defn exec

  "Runs the given, previously compiled `trx` using the current context.

   See [[compile]]."


  ([env]

   (exec env
         (result env)))


  ([env trx-compiled]

   (update-ctx env
               $.run.kw/exec
               $.cvm/exec
               trx-compiled)))



(defn eval

  "Evaluates `trx`."

  ([env]

   (eval env
         (result env)))


  ([env trx]

   (update-ctx env
               $.run.kw/eval
               $.cvm/eval
               trx)))


;;;;;;;;;; Transactions


(defn trx

  "Evaluates `trx` and forwards result to [[sreq]] unless an error occured."

  [env trx]

  (let [env-2 (eval env
                    trx)]
    (if (env-2 :convex.run/error)
      env-2
      (sreq env-2
            (result env-2)))))



(defn trx-track

  "Similar to [[trx]].

   However, requests are not performed and juice consumption is tracked by going manually through
   [[expand]], [[compile]], and [[exec]]. Those are reported with the actual result in a map interned
   under `$/*result*`."

  [env trx]

  (let [env-2 (expand env
                      trx)]
    (if (env-2 :convex.run/error)
      env-2
      (let [juice-expand (juice env-2)
            env-3        (compile env-2)]
        (if (env-3 :convex.run/error)
          env-3
          (let [juice-compile (juice env-3)
                env-4         (exec env-3)]
            (if (env-4 :convex.run/error)
              env-4
              (let [juice-exec (juice env-4)]
                ($.run.ctx/def-result env-4
                                      ($.cell/map {$.run.kw/juice         ($.cell/long (+ juice-expand
                                                                                          juice-compile
                                                                                          juice-exec))
                                                   $.run.kw/juice-expand  ($.cell/long juice-expand)
                                                   $.run.kw/juice-compile ($.cell/long juice-compile)
                                                   $.run.kw/juice-exec    ($.cell/long juice-exec)
                                                   $.run.kw/result        (result env-2)}))))))))))



(defn trx+

  "Executes transactions located in `$.trx/*list*` in the context until that list becomes empty."

  [env]

  (loop [env-2 env]
    (let [trx+ ($.run.ctx/current-trx+ env-2)]
      (if (pos? (count trx+))
        (let [env-3 (trx ($.run.ctx/def-trx+ env-2
                                             (.drop trx+
                                                    1))
                         (.get trx+
                               0))]
          (recur (dissoc env-3
                         :convex.run/error)))
        env-2))))


;;;;;;;;;; Notifying a failure or full halt


(let [trx-pop ($.read/string "($.catch/pop)")]

  (defn fail

    "Must be called in case of failure, `err` being an error map (see the [[convex.run.err]] namespace).

     Under `$.catch/*stack*` in the context is a stack of error handling transactions. This functions pops
     the next error handling transaction and prepends it to `$.trx/*list*`, the list of transactions pending
     for execution.

     Also, error becomes available under `$/*result*`.

     This simple scheme allows sophisticated exception handling to be implemented from CVX, as seen in the
     `$.catch` acccount."

    [env ^AMap err]

    (let [err-2 (.assoc err
                        $.run.kw/exception?
                        ($.cell/boolean true))]
      (-> env
          (assoc :convex.run/error
                 err-2)
          (cond->
            (env :convex.run/ctx)
            (-> (update :convex.run/ctx
                        $.cvm/exception-clear)
                ($.run.ctx/def-result err-2)))
          ($.run.ctx/prepend-trx trx-pop)))))
