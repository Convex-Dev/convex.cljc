(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.disk     :as $.disk]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (:ctx ($.disk/load [["src/convex/break/util.cvx"
                       {:wrap (partial $.code/deploy
                                       '$)}]]
                     {:after-run $.cvm/juice-refill})))
