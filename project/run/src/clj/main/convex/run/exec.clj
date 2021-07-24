(ns convex.run.exec

  "All aspects of executing transactions for the [[convex.run]] namespace.."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [compile
                            cycle
                            eval])
  (:require [convex.cvm     :as $.cvm]
            [convex.data    :as $.data]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]))


(declare run)


;;;;;;;;;; Miscellaneous


(defn result

  "Extracts a result from the current context attached to `env`."

  [env]

  (-> env
      :convex.sync/ctx
      $.cvm/result))



(defn update-ctx

  "Refills the current context with maximum juice and calls `f` with that context and `form`.
  
   The context is then reattached to `env`.
  
   In case of a CVM exception, a descriptive error map is created passed to [[convex.run.err/signal]]."

  [env kw-phase f form]

  (let [ctx (f (-> env
                   :convex.sync/ctx
                   $.cvm/juice-refill)
               form)
        ex  ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/signal ($.run.err/error ex
                                         kw-phase
                                         form)))))


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


;;;;;;;;;; Evaluation


(defn compile

  "Compiles the given, previously expandend `form` using the current context.

   See [[expand]]."


  ([env]

   (compile env
            (result env)))


  ([env form]

   (update-ctx env
               $.run.kw/compile
               $.cvm/compile
               form)))



(defn compile-run

  "Successively runs [[compile]] and [[run]], stopping in case of error."


  ([env]

   (compile-run env
                (result env)))


  ([env form]

   (let [env-2 (compile env
                        form)]
     (if (env-2 :convex.run/error)
       env-2
       (run env-2)))))



(defn expand

  "Expands the given `form` using the current context."


  ([env]

   (expand env
           (result env)))


  ([env form]

   (update-ctx env
               $.run.kw/expand
               $.cvm/expand
               form)))



(defn run

  "Runs the given, previously compiled `form` using the current context.
  
   See [[compile]]."


  ([env]

   (run env
        (result env)))


  ([env form]

   (update-ctx env
               $.run.kw/run
               $.cvm/run
               form)))


;;;


(defn eval

  "Successively runs [[expand]], [[compile]], and [[run]] on the given `form`."


  ([env]

   (eval env
         (result env)))


  ([env form]

   (let [env-2 (expand env
                       form)]
     (if (env-2 :convex.run/error)
       env-2
       (compile-run env-2)))))


;;;;;;;;;; Transactions


(defn trx

  "Evaluates the given `form` as a transaction.
  
   Result from current context is used if not provided.
  
   Essentially, setups the situation with [[convex.run.ctx/trx-begin]], runs [[eval]], updates important
   definitions using [[convex.run.ctx/trx-end]] and processes special request if needed."


  ([env]

   (trx env
        (result env)))


  ([env form]

   (let [env-2 (-> env
                   (update :convex.run/i-trx
                           inc)
                   ($.run.ctx/trx-begin form)
                   (eval form))]
     (if (env-2 :convex.run/error)
       env-2
       (let [ctx        (env :convex.sync/ctx)
             res        ($.cvm/result ctx)
             juice-last (- Long/MAX_VALUE  ;; Juice is always refilled to max prior to evaluation.
                           ($.cvm/juice ctx))
             env-3      (-> env-2
                            (update :convex.run/juice-total
                                    +
                                    juice-last)
                            ($.run.ctx/trx-end form
                                               juice-last
                                               res))]
         (try
           (sreq env-3
                 res)
           (catch Throwable _ex
             (println :EX _ex)
             ($.run.err/signal env-3
                               ($.data/code-std* :FATAL)
                               ($.data/string "Unknown error happened while finalizing transaction")))))))))



(defn trx+

  "Processes all transactions under `:convex.run/trx+` using [[trx]].
  
   Stops when any of them results in an error."

  
  ([env]

   (trx+ env
         (env :convex.run/trx+)))


  ([env trx+]

   (reduce (fn [env-2 form]
             (let [env-3 (trx env-2
                              form)]
               (if (env-3 :convex.run/error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))


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
      (assoc :convex.run/i-trx       -1
             :convex.run/in          0
             :convex.run/juice-total 0
             :convex.run/out         2)
      $.run.ctx/cycle
      trx+
      (as->
        env-2
        ((env-2 :convex.run.hook/end)
         env-2))))
