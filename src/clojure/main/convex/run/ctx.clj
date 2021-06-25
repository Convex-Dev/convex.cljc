(ns convex.run.ctx

  "Preparing CVM contextes for runner operarions."

  {:author "Adam Helinski"}

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


(def addr-strx

  ""

  ;; Last form of the 'run.cvx' file is the address ofr the STRX library.

  ($.cvm/result base))


;;;;;;;;;;


(defn def-current

  ""

  [env sym->value]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       sym->value))))



(defn def-special

  ""


  ([env sym->value]

   (def-special env
                :convex.sync/ctx
                sym->value))


  ([env kw-ctx sym->value]

   (update env
           kw-ctx
           (fn [ctx]
             ($.cvm/def ctx
                        addr-strx
                        sym->value)))))

;;;;;;;;;; Miscellaneous utilities


(defn cycle

  ""

  [env]

  (def-special env
               {$.run.sym/cycle ($.code/long (or (env :convex.watch/cycle)
                                                 0))}))



(defn error

  ""

  [env err]

  (def-special env
               {$.run.sym/error err}))



(defn init

  ""

  [env]

  (def-special env
               :convex.sync/ctx-base
               {$.run.sym/file ($.code/string (env :convex.run/path))}))



(defn trx

  ""

  [env form]

  (def-special env
               {$.run.sym/juice-last  ($.code/long (env :convex.run/juice-last))
                $.run.sym/juice-total ($.code/long (env :convex.run/juice-total))
                $.run.sym/trx-form    form
                $.run.sym/trx-id      ($.code/long (env :convex.run/i-trx))}))
