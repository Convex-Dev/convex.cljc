(ns convex.run.ctx

  "Altering and quering informations about the CVM context attached to an env."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AList)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io]
            [clojure.string]
            [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.read      :as $.read]
            [convex.run.sym   :as $.run.sym]))


;;;;;;;;;; Preparing a base context, loading libraries


(let [{:keys [ctx
              sym->addr]} (reduce (fn [acc [sym path]]
                                    (if-some [resource (clojure.java.io/resource path)]
                                      (try

                                        (let [ctx-2 ($.cvm/eval (acc :ctx)
                                                                (let [cell ($.cell/list (cons ($.cell/* do)
                                                                                              (-> resource
                                                                                                  .openStream
                                                                                                  (InputStreamReader. StandardCharsets/UTF_8)
                                                                                                  $.read/stream+)))]
                                                                  (if sym
                                                                    ($.cell/* (def ~sym
                                                                                   (deploy (quote ~cell))))
                                                                    cell)))
                                              ex    ($.cvm/exception ctx-2)]
                                          (when ex
                                            (throw (ex-info "While deploying prelude CVX file"
                                                            {::base :eval
                                                             ::ex   ex
                                                             ::path path})))
                                          (-> acc
                                              (assoc :ctx
                                                     ($.cvm/juice-refill ctx-2))
                                              (cond->
                                                sym
                                                (assoc-in [:sym->addr
                                                           sym]
                                                          ($.cvm/result ctx-2)))))

                                        (catch Throwable ex
                                          (throw (ex-info "While reading prelude CVX file"
                                                          {::base :read
                                                           ::ex   ex
                                                           ::path path}))))
                                      (throw (ex-info "Mandatory CVX file is not on classpath"
                                                      {::base :not-found
                                                       ::path path}))))
                                  {:ctx       ($.cvm/ctx)
                                   :sym->addr {}}
                                  [;; Not a library.
                                   [nil
                                    "convex/run/self.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-account
                                    "convex/run/account.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$
                                    "convex/run.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-log
                                    "convex/run/log.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-kp
                                    "convex/run/key_pair.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-perf
                                    "convex/run/perf.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-process
                                    "convex/run/process.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-stream
                                    "convex/run/stream.cvx"]
                                   ;; No deps.
                                   [$.run.sym/$-trx
                                    "convex/run/trx.cvx"]
                                   ;; Requires `$`.
                                   [$.run.sym/$-term
                                    "convex/run/term.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.trx`.
                                   [$.run.sym/$-catch
                                    "convex/run/catch.cvx"]
                                   ;; Reqyures `$.kp`
                                   [$.run.sym/$-client
                                    "convex/run/client.cvx"]
                                   ;; Requires `$.kp`
                                   [$.run.sym/$-testnet
                                    "convex/run/testnet.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.run.sym/$-code
                                    "convex/run/code.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.run.sym/$-file
                                    "convex/run/file.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.term` + `$.trx`
                                   [$.run.sym/$-help
                                    "convex/run/help.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.term` + `$.trx`.
                                   [$.run.sym/$-repl
                                    "convex/run/repl.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.run.sym/$-time
                                    "convex/run/time.cvx"]
                                   ;; Requires `$` + `$.catch` + `$.process` + `$.term` + `$.time` + `$.trx`
                                   [$.run.sym/$-test
                                    "convex/run/test.cvx"]
                                   ])]

  (def addr-$

    "Address of the `convex.run` account in [[base]]."

    (sym->addr $.run.sym/$))
  

  (def addr-$-trx

    "Address of the `convex.run.trx` account in [[base]]."

    (sym->addr $.run.sym/$-trx))


  (def base

    "Base context used by default when executing transactions.
    
     Interns all the CVX runner libraries."

    (-> ctx
        ($.cvm/def sym->addr)
        ($.cvm/def addr-$
                   {$.run.sym/line    ($.cell/string (System/lineSeparator))
                    $.run.sym/version (-> "convex/run/version.txt"
                                          clojure.java.io/resource
                                          slurp
                                          clojure.string/trim-newline
                                          $.cell/string)}))))


;;;;;;;;;; Defining symbols in the environment's context


(defn def-current

  "Defines symbols in the current, default account.
  
   Uses [[convex.cvm/def]]."

  [env sym->value]

  (update env
          :convex.run/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-result

  "Defines `$/*result*` with the given CVX `result`."

  [env result]

  (update env
          :convex.run/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-$
                       {$.run.sym/result* result}))))



(defn def-trx+

  "Defines the given CVX list of transactions under `$.trx/*list*`."

  [env trx+]

  (update env
          :convex.run/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-$-trx
                       {$.run.sym/list* trx+}))))


;;;;;;;;;;


(defn current-trx+

  "Returns the current list of transactions under `$.trx/*list*`."

  ^AList

  [env]

  (.get ($.cvm/env (env :convex.run/ctx)
                   addr-$-trx)
        $.run.sym/list*))



(defn drop-trx

  "Drops the next transaction under `$.trx/*list*`."

  [env]

  (def-trx+ env
            (.drop (current-trx+ env)
                   1)))



(defn precat-trx+

  "Prepends the given CVX list of transactions to the current list under `$.trx/*list*`."

  [env ^AList trx+]

  (if (seq trx+)
    (def-trx+ env
              (.concat trx+
                       (current-trx+ env)))
    env))



(defn prepend-trx

  "Prepends a single transaction to the current list under `$.trx/*list*`."

  [env ^ACell trx]

  (def-trx+ env
            (.cons (current-trx+ env)
                   trx)))
