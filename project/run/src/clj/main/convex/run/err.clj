(ns convex.run.err

  "Error handling for the [[convex.run]] namespace.
  
   When any exception (CVM or Java) or other error occurs, it is turned into a descriptive CVM map. Standard keys are:

   | Key | Value | Mandatory?
   |---|---|---|
   | `:cause` | Points to error that occured before this one | False |
   | `:code` | CVM exception code (often a keyword) | True |
   | `:message| CVM exception message (often a string) | True |
   | `:trace` | Stacktrace, vector of strings | False |

   This map is passed to [[fail]] so that it becomes attached to the current environment and forwarded to `:convex.run/fail` in env.
   Consumers can control what `:convex.run/fail` does, default is [[convex.run.exec/fail]]."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AMap)
           (convex.core.lang.impl ErrorValue)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute))
  (:refer-clojure :exclude [sync])
  (:require [clojure.java.io]
            [clojure.pprint]
            [convex.cvm       :as $.cvm]
            [convex.data      :as $.data]
            [convex.run.ctx   :as $.run.ctx]
            [convex.run.kw    :as $.run.kw]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Altering error maps


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



(defn assoc-trx

  "Associates a transaction to the given `err` map. under `:trx`."
  
  [^AMap err ^ACell trx]

  (.assoc err
          $.run.kw/trx
          trx))


;;;;;;;;;; Creating error maps


(defn fatal

  "Creates a `:FATAL` error map."
  
  [message]
  
  ($.data/error ($.data/code-std* :FATAL)
                message))


(defn mappify

  "Transforms the given CVM error value into a CVM map meant to be ultimately used with [[fail]].
  
   If prodived, associates to the resulting error map a [[phase]] and the current, responsible transaction."


  (^AMap [^ErrorValue ex]

   ($.data/error (.getCode ex)
                 (.getMessage ex)
                 ($.data/vector (.getTrace ex))))


  (^AMap [ex phase ^ACell trx]

   (-> ex
       mappify
       (.assoc $.run.kw/trx
               trx)
       (assoc-phase phase))))



(defn main-src

  "Error map describing failure when preparing the main source."

  ^AMap

  [message]

  (-> (fatal message)
      (assoc-phase $.run.kw/main)))



(defn main-src-access

  "Error map describing failure when accessing the main file."

  ^AMap

  []

  (main-src ($.data/string "Main file not found or not accessible")))



(defn sreq

  "Error map describing an error that occured when performing an operation for a special request."

  ^AMap

  [code message trx]

  (-> ($.data/error code
                    message)
      (assoc-phase $.run.kw/sreq)
      (assoc-trx trx)))



(defn sync

  "Error map describing failure during syncing.
  
   See 'sync' project, the [[convex.sync]] namespace."


  (^AMap [[kind arg]]

   (sync kind
         arg))


  (^AMap [kind arg]

   (-> ($.data/error ($.data/code-std* :FATAL)
                     (case kind

                       :eval
                       ($.data/map {($.data/string arg)
                                    ($.data/string "Fail to evaluate")})

                       :load
                       (reduce-kv (fn [^AMap path->reason path reason]
                                    (.assoc path->reason
                                            ($.data/string path)
                                            ($.data/string (case (first reason)
                                                             :not-found "Dependency file not found or inaccessible"
                                                             :unknown   "Unknown error while loading and parsing dependency file"))))

                                  ($.data/map)
                                  arg)

                       "Unknown error while loading dependency file"))
       (assoc-phase $.run.kw/dep))))



(defn watcher-setup

  "Error map describing unknown failure when setting up the file watcher."

  ^AMap

  []

  (-> ($.data/error ($.data/code-std* :FATAL)
                    ($.data/string "Unknown error occured while setting up the file watcher"))
      (assoc-phase $.run.kw/watch)))


;;;;;;;;;; Signaling errors in Convex Lisp


(defn fail

  ""

  [env ^AMap err]

  ((env :convex.run/fail)
   (-> env
       (assoc :convex.run/error
              (.assoc err
                      $.run.kw/exception?
                      ($.data/boolean true)))
       (cond->
         (env :convex.sync/ctx)
         (-> (update :convex.sync/ctx
                     $.cvm/exception-clear)
             ($.run.ctx/def-result nil)
             ($.run.ctx/error err))))))



(defn report

  "Uses [[fail]] with `err` but associates to it a `:report` key pointing to a temp file
   where an EDN file has been written.
  
   This EDN file pretty-prints the given `env` with `ex` under `:convex.run/exception` (the Java exception
   that caused the failure).
  
   The error in Convex data under `:convex.run/error` is stringified for better readibility."

  [env ^AMap err ex]

  (let [path  (str (Files/createTempFile "cvx_report_"
                                         ".edn"
                                         (make-array FileAttribute
                                                     0)))
        env-2 (fail env
                    (.assoc err
                            $.run.kw/report
                            ($.data/string path)))]
    (clojure.pprint/pprint (-> env-2
                               (update :convex.run/error
                                       str)
                               (assoc :convex.run/exception
                                      ex))
                           (clojure.java.io/writer path))
    env-2))
