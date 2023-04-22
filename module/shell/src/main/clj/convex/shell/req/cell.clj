(ns convex.shell.req.cell

  "More advanced requestes relating to cells."

  (:import (convex.core.data ACell))
  (:refer-clojure :exclude [compile
                            str])
  (:require [convex.cell  :as $.cell]
            [convex.clj   :as $.clj]
            [convex.cvm   :as $.cvm]
            [convex.std   :as $.std]
            [convex.write :as $.write]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn compile

  "Request for pre-compiling a `cell` for the given `address` which might
   not exist in the Shell."

  [ctx [state addr cell]]

  (or (when-not ($.std/state? state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Need a State for compilation")))
      (when-not ($.std/address? addr)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide an Address")))
      ($.cvm/result-set ctx
                        (-> ctx
                            ($.cvm/fork-to addr)
                            ($.cvm/state-set state)
                            ($.cvm/expand-compile cell)
                            ($.cvm/result)))))



(defn ref-stat

  "Requests for providing stats about the `cell`'s refs."

  [ctx [cell]]

  ($.cvm/result-set ctx
                    ($.cell/any ($.std/ref-stat cell))))



(defn size

  "Requests for getting the full memory size of a cell."

  [ctx [cell]]

  (or (when (nil? cell)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Cell to measure cannot be `nil`")))
      ($.cvm/result-set ctx
                        ($.cell/long ($.std/memory-size cell)))))



(defn str

  "Request for printing a cell to a string with a user given size limit
   instead of the default one.
  
   Also, chars and strings print in their cell form." 

  [ctx [limit cell]]

  (or (when-not ($.std/long? limit)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Byte limit must be a Long")))
      ($.cvm/result-set ctx
                        ($.write/string (max 0
                                             ($.clj/long limit))
                                        cell))))
