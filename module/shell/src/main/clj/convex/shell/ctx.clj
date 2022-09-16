(ns convex.shell.ctx

  "Altering and quering informations about the CVM context attached to an env.
  
   All CVX Shell libraries are pre-compiled in advance and a base context is defined in a top-level
   form as well. This significantly improves the start-up time of native images since all of those
   are precomputed at build time instead of run time (~4x improvement)."

  {:author "Adam Helinski"}

  (:import (convex.core.data AList)
           (java.io InputStreamReader
                    PushbackReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.edn      :as edn]
            [clojure.java.io  :as java.io]
            [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.read      :as $.read]
            [convex.shell.err :as $.shell.err]
            [convex.shell.sym :as $.shell.sym]
            [convex.std       :as $.std]))


(declare lib-address)


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



(def ctx-genesis

  "Genesis state with default Convex libraries."
  
  ($.cvm/juice-refill ($.cvm/ctx)))



(def compiled-lib+

  "Pre-compiled CVX Shell libraries."

  (reduce (fn [acc [sym path]]
            (if-some [resource (java.io/resource path)]
              ;; CVX file on classpath.
              (let [ctx-2 ($.cvm/expand-compile
                            ($.cvm/fork ctx-genesis)
                            (let [cell ($.cell/list (cons ($.cell/* do)
                                                          (try
                                                            (-> resource
                                                                (.openStream)
                                                                (InputStreamReader. StandardCharsets/UTF_8)
                                                                ($.read/stream))
                                                            (catch Throwable ex
                                                              (throw (ex-info "While reading CVX library"
                                                                              {::ex   ex
                                                                               ::path path}))))))]
                              (if sym
                                ($.cell/* (def ~sym
                                               (deploy (quote ~cell))))
                                cell)))
                    ex    ($.cvm/exception ctx-2)]
                (when ex
                  (throw (ex-info "CVM exception while compiling CVX library"
                                  {::base :eval
                                   ::ex   ex
                                   ::path path})))
                (conj acc
                      [path
                       ($.cvm/result ctx-2)]))
              ;; CVX file not on classpath.
              (throw (ex-info "Mandatory CVX library is not on classpath"
                              {::base :not-found
                               ::path path}))))
          ;;
          []
          ;;
          [;; Not a library but points to other libraries.
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
           [$.shell.sym/$-process
            "convex/shell/process.cvx"]
           ;; No deps.
           [$.shell.sym/$-db
            "convex/shell/db.cvx"]
           ;; Requires `$`.
           [$.shell.sym/$-trx
            "convex/shell/trx.cvx"]
           ;; Requires `$.trx`
           [$.shell.sym/$-stream
            "convex/shell/stream.cvx"]
           ;; Requires `$` + `$.stream`.
           [$.shell.sym/$-term
            "convex/shell/term.cvx"]
           ;; Requires `$` + `$.catch` + `$.stream` + `$.trx`.
           [$.shell.sym/$-catch
            "convex/shell/catch.cvx"]
           ;; Requires `$.trx`.
           [$.shell.sym/$-code
            "convex/shell/code.cvx"]
           ;; Requires `$.catch` + `$.trx`.
           [$.shell.sym/$-state
            "convex/shell/state.cvx"]
           ;; Requires `$.code` + `$.state`
           [$.shell.sym/$-juice
            "convex/shell/juice.cvx"]
           ;; Requires `$.state` + `$.trx`.
           [$.shell.sym/$-file
            "convex/shell/file.cvx"]
           ;; Requires `$` + `$.stream` + `$.term` + `$.trx`
           [$.shell.sym/$-help
            "convex/shell/help.cvx"]
           ;; Requires `$` + `$.catch` + `$.stream` + `$.term` + `$.trx`.
           [$.shell.sym/$-repl
            "convex/shell/repl.cvx"]
           ;; Requires `$.trx`.
           [$.shell.sym/$-time
            "convex/shell/time.cvx"]
           ;; Requires `$` + `$.catch` + `$.process` + `$.term` + `$.time` + `$.trx`
           [$.shell.sym/$-test
            "convex/shell/test.cvx"]]))



(defn deploy-lib+
  
  "Deploys [[compiled-lib+]] on the given CVM `ctx`."

  [ctx]

  (let [ctx-2 (reduce (fn [ctx-2 [path compiled-lib]]
                        (let [ctx-3 ($.cvm/exec ctx-2
                                                compiled-lib)
                              ex    ($.cvm/exception ctx-3)]
                          (if ex
                            (reduced {::err {:cvm-ex ($.shell.err/mappify ex)
                                             :path   path}})
                            ($.cvm/juice-refill ctx-3))))

                      ctx
                      compiled-lib+)]
    (if (::err ctx-2)
      ctx-2
      (-> ctx-2
          ($.cvm/def ($.cvm/look-up ctx-2
                                    $.shell.sym/$)
                     (let [[version
                            version-convex] (-version+*)]
                       {$.shell.sym/line           ($.cell/string (System/lineSeparator))
                        $.shell.sym/version        ($.cell/string version)
                        $.shell.sym/version-convex ($.cell/string (or version-convex
                                                                      "Local artifact"))}))
          ($.cvm/def ($.cvm/look-up ctx-2
                                    $.shell.sym/$-state)
                     {$.shell.sym/genesis ($.cvm/state ctx-genesis)})))))



(def ctx-base

  "Base CVM context for the CVX shell."

  (let [x   (deploy-lib+ ($.cvm/fork ctx-genesis))
        err (::err x)]
    (when err
      (throw (ex-info "CVM exception while deploying library in the base context"
                      {:convex.shell.cvm/exception (err :cvm-ex)
                       :convex.shell.lib/path      (err :path)})))
    x))


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
                       (lib-address env
                                    $.shell.sym/$)
                       {$.shell.sym/result* result}))))



(defn def-trx+

  "Defines the given CVX list of transactions under `$.trx/*list*`."

  [env trx+]

  (update env
          :convex.shell/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       (lib-address env
                                    $.shell.sym/$-trx)
                       {$.shell.sym/list* trx+}))))


;;;;;;;;;; Operations on transactions


(defn current-trx+

  "Returns the current list of transactions under `$.trx/*list*`."

  ^AList

  [env]

  ($.std/get ($.cvm/env (env :convex.shell/ctx)
                        (lib-address env
                                     $.shell.sym/$-trx))
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

  [env trx]

  (def-trx+ env
            ($.std/cons trx
                        (current-trx+ env))))


;;;;;;;;;; Retrieving information from the context


(defn active-repl?

  "Is the REPL currently running?"

  [env]

  (-> env
      (:convex.shell/ctx)
      ($.cvm/look-up (lib-address env
                                  $.shell.sym/$-repl)
                     $.shell.sym/active?*)
      ($.std/true?)))



(defn lib-address

  "Retrieves the address of a shell library by symbol."

  [env sym-lib]

  (-> (env :convex.shell/ctx)
      ($.cvm/look-up sym-lib)))



(defn result

  "Retrieves the last result available to users."

  [env]

  (-> env
      (:convex.shell/ctx)
      ($.cvm/look-up (lib-address env
                                  $.shell.sym/$)
                     $.shell.sym/result*)))


;;;;;;;;;; Miscellaneous


(defn exit

  "Prepares for a clean process exit."

  [env exit-code]

  (-> env
      (def-result nil)
      (def-trx+ ($.cell/* ()))
      (assoc :convex.shell/exit-code
             exit-code)))
