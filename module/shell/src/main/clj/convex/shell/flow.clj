(ns convex.shell.flow

  (:import (convex.core.lang Context))
  (:require [convex.cvm :as $.cvm]))


(set! *warn-on-reflection*
      true)


(declare return)


;;;;;;;;;;


(defn fail


  ([^Context ctx cvm-ex]

   (return (.withException ctx
                           cvm-ex)))


  ([ctx code message]

   (return ($.cvm/exception-set ctx
                                code
                                message))))



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

