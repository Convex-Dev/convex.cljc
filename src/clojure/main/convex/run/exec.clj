(ns convex.run.exec

  "All aspects of executing transactions in the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes))
  (:refer-clojure :exclude [compile
                            cycle])
  (:require [clojure.string]
            [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
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


(defmulti strx

  ""

  ;; Implentations are in the [[convex.run.strx]] namespace.

  (fn disptach [_env trx]
    (when ($.code/list? trx)
      (let [sym-string (str (first trx))]
        (when (clojure.string/starts-with? sym-string
                                           "cvm.")
          sym-string))))

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


;;;;;;;;;; Transactions


(defn inject-value+

  ""

  [env]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            (-> ctx
                ($.cvm/def $.run.sym/juice-last
                           ($.code/long (env :convex.run/juice-last)))
                ($.cvm/def $.run.sym/trx-id
                           ($.code/long (env :convex.run/i-trx)))))))


(defn trx

  ""


  ([env]

   (trx env
        (result env)))


  ([env form]

   (or (strx env
             form)
       (let [env-2 (-> env
                       inject-value+
                       (expand form))]
         (if (env-2 :convex.run/error)
           env-2
           (let [form-2 (result env-2)]
             (or (strx env-2
                       form-2)
                 ((env-2 :convex.run.hook/trx)
                  env-2
                  form-2))))))))



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
                 (-> env-3
                     (assoc :convex.run/juice-last (- Long/MAX_VALUE ;; Juice set by [[update-ctx]].
                                                      ($.cvm/juice (env-2 :convex.sync/ctx))))
                     (update :convex.run/i-trx
                             inc)))))
           env
           trx+)))


;;;;;;;;;;


(defn cycle

  ""

  [env]

  (-> env
      (dissoc :convex.run/restore)
      (merge (env :convex.run/restore))
      (assoc :convex.run/i-trx      0
             :convex.run/juice-last 0)
      (update :convex.sync/ctx
              (fn [ctx]
                ($.cvm/def ctx
                           $.run.sym/cycle
                                    ($.code/long (or (env :convex.watch/cycle)
                                                     0)))))
      trx+
      (as->
        env-2
        ((env-2 :convex.run.hook/end)
         env-2))))
