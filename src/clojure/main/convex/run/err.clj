(ns convex.run.err

  "Error handling for the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.lang.impl ErrorValue))
  (:require [convex.code    :as $.code]
            [convex.cvm     :as $.cvm]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.kw  :as $.run.kw]))


;;;;;;;;;;


(defn attach

  ""

  [env err]

  (-> env
      (assoc :convex.run/error
             (.assoc err
                     $.run.kw/exception?
                     ($.code/boolean true)))
      (update :convex.sync/ctx
              $.cvm/exception-clear)
      ($.run.ctx/def-error err)))



(defn assoc-phase

  ""

  [error phase]

  (.assoc error
          $.run.kw/phase
          phase))



(defn error

  ""


  ([^ErrorValue ex]

   ($.code/error (.getCode ex)
                 (.getMessage ex)
                 ($.code/vector (.getTrace ex))))


  ([ex phase trx]

   (-> ex
       error
       (.assoc $.run.kw/trx
               ($.code/quote trx))
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



(defn strx

  ""

  [code trx message]

  (-> ($.code/error code
                    message)
      (.assoc $.run.kw/trx
              trx)
      (assoc-phase $.run.kw/strx)))



(defn fatal

  ""


  ([env err]

   ((env :convex.run.hook/out)
    (attach env
            err)
    err))


  ([env form message cause]

   (fatal env
          (-> ($.code/error ErrorCodes/FATAL
                            message)
              (.assoc $.run.kw/form
                      form)
              (.assoc $.run.kw/cause
                                cause)))))
