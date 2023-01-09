(ns convex.shell.ctx

  (:require [convex.cvm :as $.cvm]))


(declare return)


;;;;;;;;;;


(defn fail

  [ctx code message]

  (return ($.cvm/exception-set ctx
                               code
                               message)))



(defn return

  [ctx]

  (throw (ex-info ""
                  {:convex.shell/ctx ctx})))



(defn safe

  [*d]

  (try
    @*d
    (catch clojure.lang.ExceptionInfo ex
      (or (:convex.shell/ctx (ex-data ex))
          (throw ex)))))
