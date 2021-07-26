(ns convex.run.exec

  "All aspects of executing transactions for the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector
                             AMap))
  (:refer-clojure :exclude [compile
                            cycle
                            eval])
  (:require [convex.cvm        :as $.cvm]
            [convex.data       :as $.data]
            [convex.run.ctx    :as $.run.ctx]
            [convex.run.err    :as $.run.err]
            [convex.run.kw     :as $.run.kw]
            [convex.run.stream :as $.run.stream]
            [convex.run.sym    :as $.run.sym]))



;;;;;;;;;; Values


(def default-err
  
  "Default error stream."

  4)



(def max-juice

  "Maximum juice value set on context prior to handling code."

  Long/MAX_VALUE)


;;;;;;;;;; Miscellaneous


(defn juice

  "Computes consumed juice, extracting [[max-juice]] from the current value."

  [env]

  (- max-juice
     (-> env
         :convex.sync/ctx
         $.cvm/juice)))



(defn result

  "Extracts a result from the current context attached to `env`."

  [env]

  (-> env
      :convex.sync/ctx
      $.cvm/result))



(defn update-ctx

  "Refills the current context with maximum juice and calls `f` with that context and `trx`.
  
   The context is then reattached to `env`."

  [env kw-phase f trx]

  (let [ctx (f (-> env
                   :convex.sync/ctx
                   $.cvm/juice-refill)
               trx)
        ex  ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/fail (-> ($.run.err/mappify ex)
                          ($.run.err/assoc-phase kw-phase)
                          ($.run.err/assoc-trx trx))))))


;;;;;;;;;; Special transactions


(defn sreq-dispatch
  
  "Dispatch function used by the [[sreq]] multimethod.
  
   Returns nil if the given result is not a special request."

  ([result]

   (when (and ($.data/vector? result)
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



(defn sreq-safe

  "Calls [[sreq]] while wrapped in a try-catch.
  
   Errors are reported using [[$.run.err/fail]]."

  [env trx result]

  (try
    (sreq env
          result)
    (catch Throwable _ex
      ($.run.err/fail env
                      ($.run.err/sreq ($.data/code-std* :FATAL)
                                      ($.data/string "Unknown error happened while finalizing transaction")
                                      trx)))))


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


;;;;;;;;;; Transactions


(defn trx

  "Evaluates `trx` and interns result in `env/*result*`."

  [env trx]

  (let [env-2 (update-ctx env
                          $.run.kw/eval
                          $.cvm/eval
                          trx)]
    (if (env-2 :convex.run/error)
      env-2
      (let [res (result env-2)]
        (sreq-safe ($.run.ctx/def-result env-2
                                         res)
                   trx
                   res)))))



(defn trx-monitor

  "Like [[trx]] but result is a map containing `:result` as well as juice values for each steps ([[expand]],
   [[compile]], and [[exec]])."

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
              (let [juice-exec (juice env-4)
                    res        (result env-4)]
                (sreq-safe ($.run.ctx/def-result env-4
                                                 ($.data/map {$.run.kw/juice         ($.data/long (+ juice-expand
                                                                                                     juice-compile
                                                                                                     juice-exec))
                                                              $.run.kw/juice-expand  ($.data/long juice-expand)
                                                              $.run.kw/juice-compile ($.data/long juice-compile)
                                                              $.run.kw/juice-exec    ($.data/long juice-exec)
                                                              $.run.kw/result        res}))
                           trx
                           res)))))))))



(defn trx+

  "Processes all transactions under `:convex.run/trx+` using [[trx]].
  
   Stops when any of them results in an error."

  
  ([env]

   (trx+ env
         (env :convex.run/trx+)))


  ([env trx+]

   (reduce (fn [env-2 cell]
             (let [env-3 (trx env-2
                              cell)]
               (if (env-3 :convex.run/error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))


;;;;;;;;;; Notifying a failure


(defn fail

  ""

  [env]

  (if-some [hook (when-not (env :convex.run/hook.error?)
                   (.get ($.cvm/env (env :convex.sync/ctx)
                                    $.run.ctx/addr-env)
                         $.run.sym/hook-error))]
    (-> env
        (assoc :convex.run/fail
               (fn [env]
                 ($.run.stream/out! env
                                    (env :convex.run/err)
                                    (env :convex.run/error))))
        (dissoc :convex.run/error)
        (trx hook)
        (assoc :convex.run/error (env :convex.run/error)
               :convex.run/fail  (env :convex.run/fail)))
    ($.run.stream/out! env
                       (env :convex.run/err)
                       (env :convex.run/error))))


;;;;;;;;;;


(defn cycle

  "Runs a whole cycle of transactions using [[trx+]].
  
   Does some preparatory work such as calling [[convex.run.ctx/cycle]] and finally calls
   the end hook."

  [env]

  (-> env
      (dissoc :convex.run/restore
              :convex.run/state-stack)
      (merge (env :convex.run/restore))
      (assoc :convex.run/err 4
             :convex.run/in  0
             :convex.run/out 2)
      $.run.ctx/cycle
      trx+
      (as->
        env-2
        (if-some [hook (.get ($.cvm/env (env-2 :convex.sync/ctx)
                                        $.run.ctx/addr-env)
                             $.run.sym/hook-end)]
          (trx env-2
               hook)
          env-2))))
