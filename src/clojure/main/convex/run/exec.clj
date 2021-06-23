(ns convex.run.exec

  "All aspects of executing transactions in the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes))
  (:require [clojure.string]
            [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.run.err  :as $.run.err]
            [convex.run.kw   :as $.run.kw]
            [convex.run.sym  :as $.run.sym]))


;;;;;;;;;; Preparation


(defn expand

  ""

  [env form]

  (let [ctx ($.cvm/expand (env :convex.sync/ctx)
                               form)
        ex  ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/expand
                                         form)))))



(defn inject-value+

  ""

  [env]

  (let [form  ($.code/do [($.code/def $.run.sym/juice-last
                                      ($.code/long (env :convex.run/juice-last)))
                          ($.code/def $.run.sym/trx-id
                                      ($.code/long (env :convex.run/i-trx)))])
        ctx   ($.cvm/eval (env :convex.sync/ctx)  
                          form)
        ex    ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/trx-prepare
                                         form)))))


;;;;;;;;;; Evaluation


(defn strx

  "Special transaction"

  [env trx]

  (when ($.code/list? trx)
    (let [sym-string (str (first trx))]
      (when (clojure.string/starts-with? sym-string
                                         "cvm.")
        (or (get-in env
                    [:convex.run/strx
                     sym-string])
            (fn [env _trx]
              ($.run.err/signal env
                                ($.run.err/strx ErrorCodes/ARGUMENT
                                                trx
                                                ($.code/string "Unsupported special transaction")))))))))

(defn eval-form

  ""

  [env form]

  (let [ctx   ($.cvm/juice-refill (env :convex.sync/ctx))
        juice ($.cvm/juice ctx)
        ctx-2 ($.cvm/eval ctx
                          form)
        ex    ($.cvm/exception ctx-2)]
    (cond->
      (-> env
          (assoc :convex.run/juice-last (- juice
                                           ($.cvm/juice ctx-2))
                 :convex.sync/ctx       ctx-2)
          (update :convex.run/i-trx
                  inc))
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/trx-eval
                                         form)))))



(defn eval-trx

  ""

  [env trx]

  (if-some [f-strx (strx env
                         trx)]
    (f-strx env
            trx)
    (let [env-2 (inject-value+ env)]
      (if (env-2 :convex.run/error)
        env-2
        (let [env-3 (expand env-2
                            trx)]
          (if (env-3 :convex.run/error)
            env-3
            (let [trx-2 (-> env-3
                            :convex.sync/ctx
                            $.cvm/result)]
              (if-some [f-strx-2 (strx env-3
                                       trx-2)]
                (f-strx-2 env-3
                          trx-2)
                (if-some [hook (env-3 :convex.run.hook/trx)]
                  (let [env-4 (eval-form env-3
                                         ($.code/list [hook
                                                       ($.code/quote trx-2)]))]
                    (if (env-4 :convex.run/error)
                      env-4
                      (eval-form env-4
                                 (-> env-4
                                     :convex.sync/ctx
                                     $.cvm/result))))
                  (eval-form env-3
                             trx-2))))))))))



(defn eval-trx+

  ""

  
  ([env]

   (eval-trx+ env
              (env :convex.run/trx+)))


  ([env trx+]

   (reduce (fn [env-2 trx]
             (let [env-3 (eval-trx env-2
                                   trx)]
               (if (env-3 :convex.run/error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))



(defn exec-trx+

  ""

  [env]

  (-> env
      (dissoc :convex.run/restore
              :convex.run.hook/trx)
      (merge (env :convex.run/restore))
      (assoc :convex.run/i-trx      0
             :convex.run/juice-last 0)
      (update :convex.sync/ctx
              (fn [ctx]
                ($.cvm/eval ctx
                            ($.code/def $.run.sym/cycle
                                        ($.code/long (or (env :convex.watch/cycle)
                                                         0))))))
      eval-trx+
      (as->
        env-2
        ((env-2 :convex.run/end)
         env-2))))



