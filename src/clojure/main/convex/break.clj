(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.clj.eval :as $.clj.eval]
            [convex.cvm      :as $.cvm]
            [convex.disk     :as $.disk]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.disk/load {'$ "src/convex/break/util.cvx"})
      :ctx
      ($.clj.eval/ctx '(def $
                            (deploy $)))
      $.cvm/juice-refill))


($.clj.eval/alter-ctx-default (ctx))