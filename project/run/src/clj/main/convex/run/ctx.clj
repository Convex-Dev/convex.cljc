(ns convex.run.ctx

  "Preparing CVM contextes for runner operations.
  
   Mosting about defining symbols at key moments, all those dynamic values in the `help`
   account (eg. `help/*error*`, `help/*trx.form*`, ...)."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [cycle])
  (:require [clojure.java.io]
            [convex.cvm       :as $.cvm]
            [convex.data      :as $.data]
            [convex.read      :as $.read]
            [convex.run.sym   :as $.run.sym]))


;;;;;;;;;;


(let [preload   (fn [ctx path]
                  (if-some [resource (clojure.java.io/resource path)]
                    (let [ctx-2 ($.cvm/eval ctx
                                            (-> resource
                                                .openStream
                                                $.read/input-stream
                                                $.data/do
                                                $.data/deploy))
                          ex    ($.cvm/exception ctx)]
                      (when ex
                        (throw (ex-info "Unable to preload CVX file"
                                        {::base :eval
                                         ::ex   ex
                                         ::path path})))
                      ($.cvm/juice-refill ctx-2))
                    (throw (ex-info "Mandatory CVX file is not on classpath"
                                    {::base :not-found
                                     ::path path}))))
      ctx       ($.cvm/ctx)
      ctx-2     (preload ctx
                         "convex/run/sreq.cvx")
      addr-sreq ($.cvm/result ctx-2)
      ctx-3     (preload ctx-2
                         "convex/run/help.cvx")
      addr-help ($.cvm/result ctx-2)]


  (def base

    "Base context used when initiating a run.
    
     Prepares the `help` and `sreq` accounts."

    ($.cvm/def ctx-3
               {$.run.sym/help addr-help
                $.run.sym/sreq addr-sreq}))


  (def addr-help

    "Address of the `help` account fetched from [[base]]."

    addr-help)


  (def addr-sreq
  
    "Address of the `sreq` account fetched from [[base]]."

    addr-sreq))


;;;;;;;;;;


(defn def-current

  "Defines symbols in the current, default account.
  
   Uses [[convex.cvm/def]]."

  [env sym->value]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-help

  "Like [[def-current]] but defines symbols in the `help` account.
  
   By default, operates over the context in `:convex.sync/ctx`.

   It is sometimes useful to operate over another context (eg. base one), hence an alternate
   keyword can be provided."


  ([env sym->value]

   (def-help env
             :convex.sync/ctx
             sym->value))


  ([env kw-ctx sym->value]

   (update env
           kw-ctx
           (fn [ctx]
             ($.cvm/def ctx
                        addr-help
                        sym->value)))))



(defn def-result

  "Defines `help/*trx.last.result*` with the given `result`."

  [env result]

  (def-help env
            {$.run.sym/trx-last-result result}))


;;;;;;;;;; Miscellaneous utilities


(defn cycle

  "Used before each run (which can happen more than once in watch mode).
  
   Defines `help/*cycle*`, a number incremented on each run."

  [env]

  (def-help env
            {$.run.sym/cycle ($.data/long (or (env :convex.watch/cycle)
                                              0))}))



(defn error

  "Used whenever an error occurs.
  
   Defines `help/*error*`, the error translated into a map."

  [env err]

  (def-help env
            {$.run.sym/error err}))



(defn init

  "Used once at the very beginning for preparing [[base]].
  
   `Defines `help/*file*`, the canonical path of the main file (unless in eval mode)."

  [env]

  (if-some [path (env :convex.run/path)]
    (def-help env
              :convex.sync/ctx-base
              {$.run.sym/file ($.data/string path)})
    env))



(defn trx-begin

  "Used before executing each transaction.
  
   `Defines:
  
    - `help/*trx.form*`, the form of the current transaction
    - `help/*frx.id*`, the number of the current transaction (incremented each time)"

  [env form]

  (def-help env
            {$.run.sym/trx-form form
             $.run.sym/trx-id   ($.data/long (env :convex.run/i-trx))}))



(defn trx-end

  "Used after executing each transaction which becomes the \"previous\" form.
  
   Defines:

   - `help/*juice*`, the total amount of juice consumed since the beginning of the run
   - `help/*trx.last.form*`, previous transaction
   - `help/*trx.last.id*, id ofthe previous transaction (see [[trx-begin]])
   - `help/*trx.last.juice*, juice consumed by the previous transaction
   - `help/*trx.last.result*, result of the previous transaction"

  [env form juice-last result]

  (def-help env
            {$.run.sym/juice-total     ($.data/long (env :convex.run/juice-total))
             $.run.sym/trx-form        nil
             $.run.sym/trx-id          nil
             $.run.sym/trx-last-form   form
             $.run.sym/trx-last-id     ($.data/long (env :convex.run/i-trx))
             $.run.sym/trx-last-juice  ($.data/long juice-last)
             $.run.sym/trx-last-result result}))
