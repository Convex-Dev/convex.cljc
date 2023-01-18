(ns convex.shell.flow

  "Sometimes, when failing to execute a request for any reason, it is easier
   throwing the context in an exception caught and returned to the user at
   a strategic point."

  (:import (convex.core.lang Context))
  (:require [convex.cvm :as $.cvm]))


(set! *warn-on-reflection*
      true)


(declare return)


;;;;;;;;;;


(defn fail

  "Attaches a CVM exception to the context and forwards it to [[return]]."


  ([^Context ctx cvm-ex]

   (return (.withException ctx
                           cvm-ex)))


  ([ctx code message]

   (return ($.cvm/exception-set ctx
                                code
                                message))))



(defn return

  "Throws the context in an exception that can be catched using [[safe]]."

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

