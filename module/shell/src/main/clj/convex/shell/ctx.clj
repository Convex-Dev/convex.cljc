(ns convex.shell.ctx

  "Altering and quering informations about the CVM context attached to an env."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AList)
           (java.io InputStreamReader
                    PushbackReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.edn      :as edn]
            [clojure.java.io  :as java.io]
            [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.read      :as $.read]
            [convex.shell.sym :as $.shell.sym]))


;;;;;;;;;; Preparing a base context, loading libraries


(defmacro ^:private -version+*
  ;; For embedding the Convex version in the shell during compile time.
  []
  (let [alias+ (-> "deps.edn"
                   (java.io/reader)
                   (PushbackReader.)
                   (edn/read)
                   (:aliases))]
    [(get-in alias+
             [:module/shell
              :convex.shell/version])
     (get-in alias+
             [:ext/convex-core
              :extra-deps
              'world.convex/convex-core
              :mvn/version])]))



(let [{:keys [ctx
              sym->addr]} (reduce (fn [acc [sym path]]
                                    (if-some [resource (java.io/resource path)]
                                      (try

                                        (let [ctx-2 ($.cvm/eval (acc :ctx)
                                                                (let [cell ($.cell/list (cons ($.cell/* do)
                                                                                              (-> resource
                                                                                                  (.openStream)
                                                                                                  (InputStreamReader. StandardCharsets/UTF_8)
                                                                                                  ($.read/stream+))))]
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
                                    "convex/shell/self.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-account
                                    "convex/shell/account.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$
                                    "convex/shell.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-log
                                    "convex/shell/log.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-perf
                                    "convex/shell/perf.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-process
                                    "convex/shell/process.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-stream
                                    "convex/shell/stream.cvx"]
                                   ;; No deps.
                                   [$.shell.sym/$-trx
                                    "convex/shell/trx.cvx"]
                                   ;; Requires `$`.
                                   [$.shell.sym/$-term
                                    "convex/shell/term.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.trx`.
                                   [$.shell.sym/$-catch
                                    "convex/shell/catch.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.shell.sym/$-code
                                    "convex/shell/code.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.shell.sym/$-file
                                    "convex/shell/file.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.term` + `$.trx`
                                   [$.shell.sym/$-help
                                    "convex/shell/help.cvx"]
                                   ;; Requires `$` + `$.stream` + `$.term` + `$.trx`.
                                   [$.shell.sym/$-repl
                                    "convex/shell/repl.cvx"]
                                   ;; Requires `$.trx`.
                                   [$.shell.sym/$-time
                                    "convex/shell/time.cvx"]
                                   ;; Requires `$` + `$.catch` + `$.process` + `$.term` + `$.time` + `$.trx`
                                   [$.shell.sym/$-test
                                    "convex/shell/test.cvx"]
                                   ])]

  (def addr-$

    "Address of the `convex.shell` account in [[base]]."

    (sym->addr $.shell.sym/$))
  

  (def addr-$-trx

    "Address of the `convex.shell.trx` account in [[base]]."

    (sym->addr $.shell.sym/$-trx))


  (def base

    "Base context used by default when executing transactions.
    
     Interns all the CVX Shell libraries."

    (-> ctx
        ($.cvm/def sym->addr)
        ($.cvm/def addr-$
                   (let [[version
                          version-convex] (-version+*)]
                   {$.shell.sym/line           ($.cell/string (System/lineSeparator))
                    $.shell.sym/version        ($.cell/string version)
                    $.shell.sym/version-convex ($.cell/string version-convex)})))))


;;;;;;;;;; Defining symbols in the environment's context


(defn def-current

  "Defines symbols in the current, default account.
  
   Uses [[convex.cvm/def]]."

  [env sym->value]

  (update env
          :convex.shell/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-result

  "Defines `$/*result*` with the given CVX `result`."

  [env result]

  (update env
          :convex.shell/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-$
                       {$.shell.sym/result* result}))))



(defn def-trx+

  "Defines the given CVX list of transactions under `$.trx/*list*`."

  [env trx+]

  (update env
          :convex.shell/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-$-trx
                       {$.shell.sym/list* trx+}))))


;;;;;;;;;;


(defn current-trx+

  "Returns the current list of transactions under `$.trx/*list*`."

  ^AList

  [env]

  (.get ($.cvm/env (env :convex.shell/ctx)
                   addr-$-trx)
        $.shell.sym/list*))



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
