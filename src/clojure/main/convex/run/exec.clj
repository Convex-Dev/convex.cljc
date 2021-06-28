(ns convex.run.exec

  "All aspects of executing transactions in the runner."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [compile
                            cycle
                            eval])
  (:require [convex.code    :as $.code]
            [convex.cvm     :as $.cvm]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]))


(declare run)


;;;;;;;;;; Miscellaneous


(defn result

  ""

  [env]

  (-> env
      :convex.sync/ctx
      $.cvm/result))



(defn update-ctx

  ""

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
  
  ""

  ([result]

   (when (and ($.code/vector? result)
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

  ""

  ;; Implentation of special requests is in the [[convex.run.sreq]] namespace.

  sreq-dispatch

  :default :unknown)


;;;;;;;;;; Evaluation


(defn compile

  ""


  ([env]

   (compile env
            (result env)))


  ([env form]

   (update-ctx env
               $.run.kw/compile
               $.cvm/compile
               form)))



(defn compile-run

  ""


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

  ""


  ([env]

   (expand env
           (result env)))


  ([env form]

   (update-ctx env
               $.run.kw/expand
               $.cvm/expand
               form)))



(defn run

  ""


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

  ""


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

  ""


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
             ($.run.err/signal env-3
                               ($.cvm/code-std* :FATAL)
                               ($.code/string "Unknown error happened while finalizing transaction")))))))))



(defn trx+

  ""

  
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

  ""

  [env]

  (-> env
      (dissoc :convex.run/restore)
      (merge (env :convex.run/restore))
      (assoc :convex.run/i-trx       -1
             :convex.run/juice-total 0)
      $.run.ctx/cycle
      trx+
      (as->
        env-2
        ((env-2 :convex.run.hook/end)
         env-2))))
