(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [convex.clj.eval  :as $.clj.eval]
            [convex.cvm       :as $.cvm]
            [convex.sync      :as $.sync]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.sync/disk {'$ (-> "convex/break.cvx"
                           clojure.java.io/resource
                           .openStream)})
      :convex.sync/ctx
      ($.clj.eval/ctx '(def $
                            (deploy (first $))))
      $.cvm/juice-refill))


($.clj.eval/alter-ctx-default (ctx))
