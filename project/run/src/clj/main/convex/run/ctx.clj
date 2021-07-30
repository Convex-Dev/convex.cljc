(ns convex.run.ctx

  "Preparing CVM contextes for runner operations.
  
   Mosting about defining symbols at key moments, all those dynamic values in the `env`
   account (eg. `env/*error*`, ...)."

  {:author "Adam Helinski"}

  (:import (convex.core.data AList)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:refer-clojure :exclude [cycle])
  (:require [clojure.java.io]
            [convex.cvm       :as $.cvm]
            [convex.data      :as $.data]
            [convex.read      :as $.read]
            [convex.run.sym   :as $.run.sym]))


;;;;;;;;;; Loading libraries


(let [{:keys [ctx
              sym->addr]} (reduce (fn [acc [sym path]]
                                    (if-some [resource (clojure.java.io/resource path)]
                                      (try
                                        (let [ctx-2 ($.cvm/eval (acc :ctx)
                                                                (-> resource
                                                                    .openStream
                                                                    (InputStreamReader. StandardCharsets/UTF_8)
                                                                    $.read/stream+
                                                                    $.data/do
                                                                    $.data/deploy))
                                              ex    ($.cvm/exception ctx-2)]
                                          (when ex
                                            (throw (ex-info "While deploying prelude CVX file"
                                                            {::base :eval
                                                             ::ex   ex
                                                             ::path path})))
                                          (-> acc
                                              (assoc :ctx
                                                     ($.cvm/juice-refill ctx-2))
                                              (assoc-in [:sym->addr
                                                         sym]
                                                        ($.cvm/result ctx-2))))
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
                                  [[$.run.sym/$
                                    "convex/run.cvx"]
                                   [$.run.sym/$-trx
                                    "convex/run/trx.cvx"]  ;; Must be before others
                                   [$.run.sym/$-catch
                                    "convex/run/catch.cvx"]
                                   [$.run.sym/$-file
                                    "convex/run/file.cvx"]
                                   [$.run.sym/$-main
                                    "convex/run/main.cvx"]
                                   [$.run.sym/$-process
                                    "convex/run/process.cvx"]
                                   [$.run.sym/$-repl
                                    "convex/run/repl.cvx"]
                                   [$.run.sym/$-stream
                                    "convex/run/stream.cvx"]
                                   [$.run.sym/$-time
                                    "convex/run/time.cvx"]
                                   ;;
                                   [$.run.sym/$-test
                                    "convex/run/test.cvx"]
                                   ])
      addr-$              (sym->addr $.run.sym/$)]

  (def base

    "Base context used when initiating a run.
    
     Prepares the `env` and `sreq` accounts."

    (-> ctx
        ($.cvm/def sym->addr)
        ($.cvm/def addr-$
                   {$.run.sym/line ($.data/string (System/lineSeparator))})))


  (def addr-$

    "Address of the `env` account fetched from [[base]]."

    addr-$)
  


  (def addr-$-catch

    ""

    (sym->addr $.run.sym/$-catch))



  (def addr-$-main

    ""

    (sym->addr $.run.sym/$-main))



  (def addr-$-repl

    ""

    (sym->addr $.run.sym/$-repl))



  (def addr-$-stream

    ""

    (sym->addr $.run.sym/$-stream))



  (def addr-$-trx

    ""

    (sym->addr $.run.sym/$-trx)))


;;;;;;;;;; Defining symbols in the environment's context


(defn def-current

  "Defines symbols in the current, default account.
  
   Uses [[convex.cvm/def]]."

  [env sym->value]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-env

  "Like [[def-current]] but defines symbols in the `env` account.
  
   By default, operates over the context in `:convex.sync/ctx`.

   It is sometimes useful to operate over another context (eg. base one), hence an alternate
   keyword can be provided."


  ([env sym->value]

   (def-env env
            :convex.sync/ctx
            sym->value))


  ([env kw-ctx sym->value]

   (update env
           kw-ctx
           (fn [ctx]
             ($.cvm/def ctx
                        addr-$
                        sym->value)))))



(defn def-result

  "Defines `env/*result*` with the given `result`."

  [env result]

  (def-env env
            {$.run.sym/result result}))




(defn def-trx+

  ""

  [env trx+]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-$-trx
                       {$.run.sym/list trx+}))))


;;;;;;;;;; Miscellaneous utilities


(defn cycle

  "Used before each run (which can happen more than once in watch mode).
  
   Defines `env/*cycle*`, a number incremented on each run."

  [env]

  (def-env env
           {$.run.sym/cycle ($.data/long (or (env :convex.watch/cycle)
                                             0))}))



(defn main

  "Used once at the very beginning for preparing [[base]].
  
   `Defines `env/*file*`, the canonical path of the main file (unless there is none)."

  [env]
      
  (update env
          :convex.sync/ctx-base
          (fn [ctx]
            ($.cvm/def ctx
              addr-$-main
                       {$.run.sym/main?  ($.data/boolean true)
                        $.run.sym/watch? (-> env
                                             :convex.run/watch?
                                             boolean
                                             $.data/boolean)}))))


;;;;;;;;;;


(defn current-trx+

  ;;


  (^AList [env]

   (current-trx+ env
                 :convex.sync/ctx))


  (^AList [env kw-ctx]

   (.get ($.cvm/env (env kw-ctx)
                    addr-$-trx)
         $.run.sym/list)))



(defn drop-trx

  ""


  [env]

  (def-trx+ env
            (.drop (current-trx+ env)
                   1)))




(defn precat-trx+

  ""


  [env ^AList trx+]

  (if (seq trx+)
    (def-trx+ env
              (.concat trx+
                       (current-trx+ env)))
    env))
