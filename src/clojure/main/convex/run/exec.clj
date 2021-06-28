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


(defn strx-dispatch
  
  ""

  ([result]

   (when (and ($.code/vector? result)
              (>= (count result)
                  2)
              (= (.get ^AVector result
                       0)
                 $.run.kw/cvm-strx))
     (.get ^AVector result
           1)))


  ([_env result]

   (strx-dispatch result)))



(defmulti strx

  ""

  ;; Implentation of special transactions is in the [[convex.run.strx]] namespace.

  strx-dispatch

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
                   ($.run.ctx/trx form)
                   (eval form))]
     (if (env-2 :convex.run/error)
       env-2
       (let [juice-last (- Long/MAX_VALUE  ;; Juice is always refilled to max prior to evaluation.
                           ($.cvm/juice (env :convex.sync/ctx)))]
         (-> env-2
             (assoc :convex.run/juice-last
                    juice-last)
             (update :convex.run/juice-total
                     +
                     juice-last)
             (update :convex.run/i-trx
                     inc)
             (strx (result env-2))))))))



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
      (assoc :convex.run/i-trx       0
             :convex.run/juice-last  0
             :convex.run/juice-total 0)
      $.run.ctx/cycle
      trx+
      (as->
        env-2
        ((env-2 :convex.run.hook/end)
         env-2))))
