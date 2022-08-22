(ns convex.shell.err

  "Errors are CVX maps, either mappified CVM exceptions or built from scratch.

   Using [[convex.shell.exec/fail]], they are reported back to the CVX executing environment
   and can be handled from CVX.

   This namespace provides functions for building recurrent error maps."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AMap)
           (convex.core.lang.impl ErrorValue)
           ; (java.nio.file Files)
           ; (java.nio.file.attribute FileAttribute)
           )
  (:require [clojure.java.io]
            [clojure.pprint]
            [convex.cell      :as $.cell]
            [convex.shell.kw  :as $.shell.kw]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Altering error maps


(defn assoc-phase

  "Associates a `phase` to the given `err`.
  
   A `phase` is a CVM keyword which provides an idea of what stage the error occured in."

  ^AMap

  [^AMap err ^ACell phase]

  (.assoc err
          $.shell.kw/phase
          phase))



(defn assoc-trx

  "Associates a transaction to the given `err` map. under `:trx`."
  
  [^AMap err ^ACell trx]

  (.assoc err
          $.shell.kw/trx
          trx))


;;;;;;;;;; Creating error maps


(defn fatal

  "Creates a `:FATAL` error map."
  
  [message]
  
  ($.cell/error ($.cell/code-std* :FATAL)
                message))


(defn mappify

  "Transforms the given CVM exception into a map.
  
   If prodived, associates to the resulting error map a [[phase]] and the current transaction that caused this error."


  (^AMap [^ErrorValue ex]

   ($.cell/error (.getCode ex)
                 (.getMessage ex)
                 ($.cell/vector (.getTrace ex))))


  (^AMap [ex phase ^ACell trx]

   (-> ex
       (mappify)
       (.assoc $.shell.kw/trx
               trx)
       (assoc-phase phase))))



(defn reader

  "Creates a `:READER` error map, for when the CVX reader fails."

  ^AMap

  []

  ($.cell/error $.shell.kw/err-reader
                ($.cell/string "String cannot be read as Convex Lisp")))





(defn sreq

  "Error map describing an error that occured when performing an operation for a request."

  ^AMap

  [code message trx]

  (-> ($.cell/error code
                    message)
      (assoc-phase $.shell.kw/sreq)
      (assoc-trx trx)))


;;;;;;;;;; Fatal


; (defn report
; 
;   "Uses [[fail]] with `err` but associates to it a `:report` key pointing to a temp file
;    where an EDN file has been written.
;   
;    This EDN file pretty-prints the given `env` with `ex` under `:convex.shell/exception` (the Java exception
;    that caused the failure).
;   
;    The error in Convex data under `:convex.shell/error` is stringified for better readibility."
; 
;   [env ^AMap err ex]
; 
;   (let [path  (str (Files/createTempFile "cvx_report_"
;                                          ".edn"
;                                          (make-array FileAttribute
;                                                      0)))
;         env-2 (fail env
;                     (.assoc err
;                             $.shell.kw/report
;                             ($.cell/string path)))]
;     (clojure.pprint/pprint (-> env-2
;                                (update :convex.shell/error
;                                        str)
;                                (assoc :convex.shell/exception
;                                       ex))
;                            (clojure.java.io/writer path))
;     env-2))
