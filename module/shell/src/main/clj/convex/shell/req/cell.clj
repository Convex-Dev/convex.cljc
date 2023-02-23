(ns convex.shell.req.cell

  "More advanced requestes relating to cells."

  (:refer-clojure :exclude [compile])
  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


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
