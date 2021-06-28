(ns convex.run.ctx

  "Preparing CVM contextes for runner operarions."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [cycle])
  (:require [clojure.java.io]
            [convex.code      :as $.code]
            [convex.cvm       :as $.cvm]
            [convex.run.sym   :as $.run.sym]))


;;;;;;;;;;


(def base

  ""

  (if-some [resource (clojure.java.io/resource "run.cvx")]
    (let [ctx ($.cvm/eval ($.cvm/ctx)
                          (first ($.cvm/read (slurp resource))))
          ex  ($.cvm/exception ctx)]
      (when ex
        (throw (ex-info "Unable to execute 'run.cvx'"
                        {::base :eval
                         ::ex   ex})))
      ($.cvm/juice-refill ctx))
    (throw (ex-info "Mandatory 'run.cvx' file is not on classpath"
                    {::base :not-found}))))



;;;;;;;;;; Miscellaneous values


(let [^AVector -result ($.cvm/result base)]

  (def addr-help

    ""

    (.get -result
          0))



  (def addr-sreq
  
    ""

    (.get -result
          1)))


;;;;;;;;;;


(defn def-current

  ""

  [env sym->value]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-help

  ""


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

;;;;;;;;;; Miscellaneous utilities


(defn cycle

  ""

  [env]

  (def-help env
            {$.run.sym/cycle ($.code/long (or (env :convex.watch/cycle)
                                              0))}))



(defn error

  ""

  [env err]

  (def-help env
            {$.run.sym/error err}))



(defn init

  ""

  [env]

  (if-some [path (env :convex.run/path)]
    (def-help env
              :convex.sync/ctx-base
              {$.run.sym/file ($.code/string path)})
    env))



(defn trx

  ""

  [env form]

  (def-help env
            {$.run.sym/juice-last  ($.code/long (env :convex.run/juice-last))
             $.run.sym/juice-total ($.code/long (env :convex.run/juice-total))
             $.run.sym/trx-form    form
             $.run.sym/trx-id      ($.code/long (env :convex.run/i-trx))}))
