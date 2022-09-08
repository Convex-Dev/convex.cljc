(ns convex.shell.err

  "Errors are CVX maps, either mappified CVM exceptions or built from scratch.

   Using [[convex.shell.exec.fail/err]], they are reported back to the CVX executing environment
   and can be handled from CVX.

   This namespace provides functions for building recurrent error maps."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AMap)
           (convex.core.lang.impl ErrorValue))
  (:require [convex.cell      :as $.cell]
            [convex.shell.kw  :as $.shell.kw]
            [convex.std       :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Altering error maps


(defn assoc-phase

  "Associates a `phase` to the given `err`.
  
   A `phase` is a CVM keyword which provides an idea of what stage the error occured in."

  [err phase]

  ($.std/assoc err
               $.shell.kw/phase
               phase))



(defn assoc-trx

  "Associates a transaction to the given `err` map. under `:trx`."
  
  [err trx]

  ($.std/assoc err
               $.shell.kw/trx
               trx))


;;;;;;;;;; Creating error maps


(defn arg

  "Error map for a bad argument."

  [message arg-symbol]

  (-> ($.cell/error ($.cell/code-std* :ARGUMENT)
                    message)
      ($.std/assoc $.shell.kw/arg
                   arg-symbol)))



(defn db

  "Error map for a generic Etch error."

  [message]

  ($.cell/error $.shell.kw/err-db
                message))



(defn filesystem

  "Error map for a generic filesystem error."

  [message]

  ($.cell/error $.shell.kw/err-filesystem
                message))



(defn mappify

  "Transforms the given CVM exception into a map.
  
   If prodived, associates to the resulting error map a [[phase]] and the current transaction that caused this error."


  ([^ErrorValue ex]

   ($.cell/error (.getCode ex)
                 (.getMessage ex)
                 ($.cell/vector (.getTrace ex))))


  ([ex phase trx]

   (-> ex
       (mappify)
       ($.std/assoc $.shell.kw/trx
                    trx)
       (assoc-phase phase))))



(defn reader-string

  "Creates a `:READER` error map, for when the CVX reader fails on a string."


  ([src]

   (reader-string src))


  ([src reason]

   (-> ($.cell/error $.shell.kw/err-reader
                     (or reason
                         ($.cell/string "String cannot be read as Convex Lisp")))
       ($.std/assoc $.shell.kw/src
                    src))))



(defn reader-stream

  "Creates a `:READER` error map, for when the CVX reader fails on a stream."


  ([id-stream]

   (reader-stream id-stream
                  nil))


  ([id-stream reason]

   (-> ($.cell/error $.shell.kw/err-reader
                     (or reason
                         ($.cell/string "Stream cannot be read as Convex Lisp")))
       ($.std/assoc $.shell.kw/stream
                    id-stream))))



(defn state

  "Error map for a state exception."

  [message]

  ($.cell/error ($.cell/code-std* :STATE)
                message))



(defn state-load

  "Error map for when library deployment fails when loading a new state."

  [library-path message ex]

  (-> (state message)
      ($.std/assoc $.shell.kw/cause
                   ex)
      ($.std/assoc $.shell.kw/library-path 
                   library-path)))



(defn sreq

  "Error map describing an error that occured when performing an operation for a request."

  [code message trx]

  (-> ($.cell/error code
                    message)
      (assoc-phase $.shell.kw/sreq)
      (assoc-trx trx)))



(defn stream

  "Error map for a generic stream error."

  [id-stream message]

  (-> ($.cell/error $.shell.kw/err-stream
                    message)
      ($.std/assoc $.shell.kw/stream
                   id-stream)))
