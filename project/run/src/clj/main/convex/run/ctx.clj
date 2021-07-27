(ns convex.run.ctx

  "Preparing CVM contextes for runner operations.
  
   Mosting about defining symbols at key moments, all those dynamic values in the `env`
   account (eg. `env/*error*`, ...)."

  {:author "Adam Helinski"}

  (:import (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:refer-clojure :exclude [cycle])
  (:require [clojure.java.io]
            [convex.cvm       :as $.cvm]
            [convex.data      :as $.data]
            [convex.read      :as $.read]
            [convex.run.sym   :as $.run.sym]))


;;;;;;;;;; Loading libraries


(let [preload   (fn [ctx path]
                  (if-some [resource (clojure.java.io/resource path)]
                    (try
                      (let [ctx-2 ($.cvm/eval ctx
                                              (-> resource
                                                  .openStream
                                                  (InputStreamReader. StandardCharsets/UTF_8)
                                                  $.read/stream+
                                                  $.data/do
                                                  $.data/deploy))
                            ex    ($.cvm/exception ctx)]
                        (when ex
                          (throw (ex-info "While deploying prelude CVX file"
                                          {::base :eval
                                           ::ex   ex
                                           ::path path})))
                        ($.cvm/juice-refill ctx-2))
                      (catch Throwable ex
                        (throw (ex-info "While reading prelude CVX file"
                                        {::base :read
                                         ::ex   ex
                                         ::path path}))))
                    (throw (ex-info "Mandatory CVX file is not on classpath"
                                    {::base :not-found
                                     ::path path}))))
      ctx       ($.cvm/ctx)
      ctx-2     (preload ctx
                         "convex/run/sreq.cvx")
      addr-sreq ($.cvm/result ctx-2)
      ctx-3     (preload ctx-2
                         "convex/run/env.cvx")
      addr-env  ($.cvm/result ctx-2)
      ctx-4     (preload ctx-3
                         "convex/run/test.cvx")]


  (def base

    "Base context used when initiating a run.
    
     Prepares the `env` and `sreq` accounts."

    (-> ctx-4
        ($.cvm/def {$.run.sym/env addr-env
                    $.run.sym/sreq addr-sreq})
        ($.cvm/def addr-env
                   {$.run.sym/line ($.data/string (System/lineSeparator))})))


  (def addr-env

    "Address of the `env` account fetched from [[base]]."

    addr-env)


  (def addr-sreq
  
    "Address of the `sreq` account fetched from [[base]]."

    addr-sreq))


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
                        addr-env
                        sym->value)))))



(defn def-result

  "Defines `env/*result*` with the given `result`."

  [env result]

  (def-env env
            {$.run.sym/result result}))


;;;;;;;;;; Miscellaneous utilities


(defn cycle

  "Used before each run (which can happen more than once in watch mode).
  
   Defines `env/*cycle*`, a number incremented on each run."

  [env]

  (def-env env
           {$.run.sym/cycle ($.data/long (or (env :convex.watch/cycle)
                                             0))}))



(defn error

  "Used whenever an error occurs.
  
   Defines `env/*error*`, the error translated into a map."

  [env err]

  (def-env env
           {$.run.sym/error err}))



(defn init

  "Used once at the very beginning for preparing [[base]].
  
   `Defines `env/*file*`, the canonical path of the main file (unless there is none)."

  [env]

  (if-some [path (env :convex.run/path)]
    (def-env env
             :convex.sync/ctx-base
             {$.run.sym/file ($.data/string path)})
    env))



      ; (as->
      ;   env-2
      ;   ($.run.ctx/def-env env-2
      ;                      :convex.sync/ctx-base
      ;                      {$.run.sym/single-run? ($.data/boolean (env-2 :convex.run/single-run?))}))
