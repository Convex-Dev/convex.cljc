(ns convex.run.ctx

  "Preparing CVM contextes for runner operations.
  
   Mosting about defining symbols at key moments, all those dynamic values in the `env`
   account (eg. `env/*error*`, `env/*trx.form*`, ...)."

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
                                                  $.read/stream-txt+
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



(defn def-mode

  "Defines the evaluation mode in the `env` account under `*mode*`.

   See [[convex.run.exec/trx]]."


  ([env mode-f mode-kw]

   (def-mode env
             :convex.sync/ctx
             mode-f
             mode-kw))


  ([env kw-ctx mode-f mode-kw]

   (-> env
       (assoc :convex.run/mode
              mode-f)
       (def-env kw-ctx
                {$.run.sym/mode mode-kw}))))



(defn def-result

  "Defines `env/*trx.last.result*` with the given `result`."

  [env result]

  (def-env env
            {$.run.sym/trx-last-result result}))


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
  
   `Defines `env/*file*`, the canonical path of the main file (unless in eval mode)."

  [env]

  (if-some [path (env :convex.run/path)]
    (def-env env
             :convex.sync/ctx-base
             {$.run.sym/file ($.data/string path)})
    env))



(defn trx-begin

  "Used before executing each transaction.
  
   `Defines:
  
    - `env/*trx.form*`, the form of the current transaction
    - `env/*frx.id*`, the number of the current transaction (incremented each time)"

  [env form]

  (def-env env
           {$.run.sym/trx-form form
            $.run.sym/trx-id   ($.data/long (env :convex.run/i-trx))}))



(defn trx-end

  "Used after executing each transaction which becomes the \"previous\" form.
  
   Defines:

   - `env/*juice*`, the total amount of juice consumed since the beginning of the run
   - `env/*trx.last.form*`, previous transaction
   - `env/*trx.last.id*, id ofthe previous transaction (see [[trx-begin]])
   - `env/*trx.last.juice*, juice consumed by the previous transaction
   - `env/*trx.last.result*, result of the previous transaction"

  [env form juice-last result]

  (def-env env
           {$.run.sym/juice-total     ($.data/long (env :convex.run/juice-total))
            $.run.sym/trx-form        nil
            $.run.sym/trx-id          nil
            $.run.sym/trx-last-form   form
            $.run.sym/trx-last-id     ($.data/long (env :convex.run/i-trx))
            $.run.sym/trx-last-juice  ($.data/long juice-last)
            $.run.sym/trx-last-result result}))
