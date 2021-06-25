(ns convex.run.exec

  "All aspects of executing transactions in the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.data AHashMap)
           (convex.core.lang Symbols))
  (:refer-clojure :exclude [compile
                            cycle
                            eval])
  (:require [clojure.string]
            [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.run.ctx  :as $.run.ctx]
            [convex.run.err  :as $.run.err]
            [convex.run.kw   :as $.run.kw]
            [convex.run.sym  :as $.run.sym]))


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
              (= (.get result
                       0)
                 $.run.kw/cvm-strx))
     (.get result
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

   (let [env-2 (eval env
                     form)]
     (if (env-2 :convex.run/error)
       env-2
       (strx env-2
             (result env-2))))))



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
                 (let [juice-last (- Long/MAX_VALUE ;; Juice set by [[update-ctx]].
                                     ($.cvm/juice (env-3 :convex.sync/ctx)))]
                   (-> env-3
                       (assoc :convex.run/juice-last
                              juice-last)
                       (update :convex.run/juice-total
                               +
                               juice-last)
                       (update :convex.run/i-trx
                               inc))))))
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
