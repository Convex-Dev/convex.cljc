(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [convex.clj.eval :as $.clj.eval]
            [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [convex.read     :as $.read]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.cvm/ctx)
      ($.cvm/eval ($.cell/* (def $
                                 (deploy (quote ~($.read/resource "convex/break.cvx"))))))
      $.cvm/juice-refill))


($.clj.eval/alter-ctx-default (ctx))
