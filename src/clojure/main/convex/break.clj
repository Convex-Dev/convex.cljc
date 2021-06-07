(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.cvm/import {"src/convex/break/util.cvx" '$})
      ($.cvm/set-juice Long/MAX_VALUE)))
