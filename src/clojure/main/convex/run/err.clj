(ns convex.run.err

  "Error handling for the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.data ACell
                             AMap)
           (convex.core.lang.impl ErrorValue))
  (:require [convex.code    :as $.code]
            [convex.cvm     :as $.cvm]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.kw  :as $.run.kw]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn attach

  ""

  [env ^AMap err]

  (-> env
      (assoc :convex.run/error
             (.assoc err
                     $.run.kw/exception?
                     ($.code/boolean true)))
      (cond->
        (env :convex.sync/ctx)
        (-> (update :convex.sync/ctx
                    $.cvm/exception-clear)
            ($.run.ctx/error err)))))



(defn assoc-cause

  ""

  ^AMap

  [^AMap err ^ACell cause]

  (.assoc err
          $.run.kw/cause
          cause))



(defn assoc-phase

  ""

  ^AMap

  [^AMap err ^ACell phase]

  (.assoc err
          $.run.kw/phase
          phase))



(defn error

  ""


  (^AMap [^ErrorValue ex]

   ($.code/error (.getCode ex)
                 (.getMessage ex)
                 ($.code/vector (.getTrace ex))))


  (^AMap [ex phase ^ACell trx]

   (-> ex
       error
       (.assoc $.run.kw/trx
               trx)
       (assoc-phase phase))))



(defn signal

  ""

  ([env err]

   ((env :convex.run.hook/error)
    (attach env
            err)))


  ([env code message]

   (signal env
           ($.code/error code
                         message)))


  ([env code message trace]

   (signal env
           ($.code/error code
                         message
                         trace))))



(defn sreq

  ""

  [code ^ACell trx message]

  (-> ($.code/error code
                    message)
      (.assoc $.run.kw/trx
              trx)
      (assoc-phase $.run.kw/sreq)))



(defn fatal

  ""


  ([env err]

   ((env :convex.run.hook/out)
    (attach env
            err)
    err))


  ([env ^ACell form message cause]

   (fatal env
          (-> ($.code/error ErrorCodes/FATAL
                            message)
              ;(.assoc $.run.kw/form
              ;        form)
              (assoc-cause cause)))))
