(ns convex.run.err

  "Error handling for the [[convex.run]] namespace.
  
   When any exception (CVM or Java) or other error occurs, it is turned into a descriptive CVM map.

   This map is passed to [[signal]] so that it becomes attached to the current environment and forwarded to the error hook.
  
   In some extreme case where normal error reporting does not work (eg. user output hook failing), the error is passed to [[fatal]]."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
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

  "Attaches the given `err` under `:convex.run/error`.
  
   Also clears the CVM execption of the current context (if present)."

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

  "Associates a `cause` to the given `err`.
  
   Ressembling Java exceptions, a cause is another error that preceded `err`."

  ^AMap

  [^AMap err ^ACell cause]

  (.assoc err
          $.run.kw/cause
          cause))



(defn assoc-phase

  "Associates a `phase` to the given `err`.
  
   A `phase` is a CVM keyword which provides an idea of what stage the error occured in."

  ^AMap

  [^AMap err ^ACell phase]

  (.assoc err
          $.run.kw/phase
          phase))



(defn error

  "Transforms the given CVM error value into a CVM map meant to be ultimately used with [[signal]].
  
   If prodived, associates to the resulting error map a [[phase]] and the current, responsible transaction."


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

  "Uses [[attach] and ultimately passes the environment to the error hook.
  
   Arity 2 and 3 are shortcuts to [[convex.code/error]] for building an error map on the spot."

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

  "Prepares an error map describing an error that occured when performing an operation for a special request."

  [code ^ACell trx message]

  (-> ($.code/error code
                    message)
      (.assoc $.run.kw/trx
              trx)
      (assoc-phase $.run.kw/sreq)))



(defn fatal

  "In some extreme cases, normal error reporting via [[signal]] does not work or cannot be done.
  
   For instance, user provided error hook failed, no way to actually report the error.
  
   In that case the error os forwarded directly to the output hook after using [[attach]]."


  ([env err]

   ((env :convex.run.hook/out)
    (attach env
            err)
    err))


  ([env ^ACell form message cause]

   (fatal env
          (-> ($.code/error ($.cvm/code-std* :FATAL)
                            message)
              ;(.assoc $.run.kw/form
              ;        form)
              (assoc-cause cause)))))
