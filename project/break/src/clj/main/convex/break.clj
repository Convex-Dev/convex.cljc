(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.read :as $.read]))


;;;;;;;;;;


(def ctx

  "Prepares a default context for dev and testing."

  (-> ($.cvm/ctx)
      ($.cvm/eval ($.cell/* (def $
                                 (deploy (quote ~($.read/resource "convex/break.cvx"))))))
      $.cvm/juice-refill))
