(ns convex.shell.exec

  "All aspects of actually executing transactions.
  
   When an error is detected, [[convex.shell.exec.fail/err]] is called."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [compile
                            eval])
  (:require [convex.cell            :as $.cell]
            [convex.clj             :as $.clj]
            [convex.cvm             :as $.cvm]
            [convex.db              :as $.db]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.err       :as $.shell.err]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.kw        :as $.shell.kw]
            [convex.shell.stream    :as $.shell.stream]
            [convex.shell.sym       :as $.shell.sym]
            [convex.std             :as $.std]))


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
         (:convex.shell/ctx)
         ($.cvm/juice))))



(defn result

  "Extracts a result from the current context attached to `env`."

  [env]

  (-> env
      (:convex.shell/ctx)
      ($.cvm/result)))



(defn update-ctx

  "Refills the current context with maximum juice and calls `f` with that context and `trx`.
  
   The context is then reattached to `env`."

  [env kw-phase f trx]

  (let [ctx (f (-> env
                   (:convex.shell/ctx)
                   ($.cvm/juice-refill))
               trx)
        ex  ($.cvm/exception ctx)]
    (-> env
        (assoc :convex.shell/ctx
               ctx)
        (cond->
          ex
          ($.shell.exec.fail/err (-> ($.shell.err/mappify ex)
                                     ($.shell.err/assoc-phase kw-phase)
                                     ($.shell.err/assoc-trx trx)))))))


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


;;;;;;;;;; Execution steps


(defn expand

  "Expands the given `trx` using the current context."


  ([env]

   (expand env
           (result env)))


  ([env trx]

   (update-ctx env
               $.shell.kw/expand
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
               $.shell.kw/compile
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
               $.shell.kw/exec
               $.cvm/exec
               trx-compiled)))



(defn eval

  "Evaluates `trx`."

  ([env]

   (eval env
         (result env)))


  ([env trx]

   (update-ctx env
               $.shell.kw/eval
               $.cvm/eval
               trx)))


;;;;;;;;;; Transactions


(defn trx

  "Evaluates `trx` and forwards result to [[sreq]] unless an error occured."

  [env trx]
  
  (let [env-2 (eval env
                    trx)]
    (if (env-2 :convex.shell/error)
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
    (if (env-2 :convex.shell/error)
      env-2
      (let [juice-expand (juice env-2)
            env-3        (compile env-2)]
        (if (env-3 :convex.shell/error)
          env-3
          (let [juice-compile (juice env-3)
                env-4         (exec env-3)]
            (if (env-4 :convex.shell/error)
              env-4
              (let [juice-exec (juice env-4)]
                ($.shell.ctx/def-result env-4
                                      ($.cell/map {$.shell.kw/juice         ($.cell/long (+ juice-expand
                                                                                            juice-compile
                                                                                            juice-exec))
                                                   $.shell.kw/juice-expand  ($.cell/long juice-expand)
                                                   $.shell.kw/juice-compile ($.cell/long juice-compile)
                                                   $.shell.kw/juice-exec    ($.cell/long juice-exec)
                                                   $.shell.kw/result        (result env-2)}))))))))))



(defn trx+

  "Executes transactions located in `$.trx/*list*` in the context until that list becomes empty."

  [env]

  (loop [env-2 env]
    (let [trx+ ($.shell.ctx/current-trx+ env-2)]
      (if (pos? (count trx+))
        (let [env-3 (trx ($.shell.ctx/def-trx+ env-2
                                               (.drop trx+
                                                      1))
                         (.get trx+
                               0))]
          (recur (dissoc env-3
                         :convex.shell/error)))
        (do
          (when (env-2 :convex.shell.db/instance)
            ($.db/close))
          (or (let [ctx (env-2 :convex.shell/ctx)]
                (when ($.std/false? ($.cvm/look-up ctx
                                                   $.shell.ctx/addr-$-repl
                                                   $.shell.sym/active?*))
                  (when-some [result ($.cvm/look-up ctx
                                                    $.shell.ctx/addr-$
                                                    $.shell.sym/result*)]
                    (let [id-stream (-> ctx
                                        ($.cvm/look-up $.shell.ctx/addr-$-stream
                                                       $.shell.sym/out*)
                                        ($.clj/long))]
                      (-> env-2
                          ($.shell.stream/out id-stream
                                              (if ($.std/string? result)
                                                (str \"
                                                     (str result)
                                                     \")
                                                result))
                          ($.shell.stream/flush id-stream))))))
              env-2))))))
