(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [convex.clj.eval :as $.clj.eval]
            [convex.cvm      :as $.cvm]
            [convex.data     :as $.data]
            [convex.read     :as $.read]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.cvm/ctx)
      ($.cvm/eval ($.data/def ($.data/symbol "$")
                              ($.data/deploy ($.read/resource "convex/break.cvx"))))
      $.cvm/juice-refill))


($.clj.eval/alter-ctx-default (ctx))
