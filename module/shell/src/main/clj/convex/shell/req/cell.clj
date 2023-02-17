(ns convex.shell.req.cell

  "More advanced requestes relating to cells."

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


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
