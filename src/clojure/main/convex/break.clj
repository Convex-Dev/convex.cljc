(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.raw  :as $.cvm.raw]
            [convex.disk     :as $.disk]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (:ctx ($.disk/load [["src/convex/break/util.cvx"
                       {:wrap (partial $.cvm.raw/deploy
                                       '$)}]]
                     {:after-run $.cvm/juice-refill})))
