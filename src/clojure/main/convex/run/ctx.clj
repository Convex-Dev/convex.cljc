(ns convex.run.ctx

  "Preparing CVM contextes for runner operarions."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
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

  (get ($.cvm/env base)
       $.run.sym/strx))

;;;;;;;;;; Miscellaneous utilities


(defn def-error

  ""

  [env err]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/def ctx
                       addr-strx
                       {$.run.sym/error err}))))
