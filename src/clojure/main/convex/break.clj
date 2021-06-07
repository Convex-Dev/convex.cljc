(ns convex.break

  ""

  {:author "Adam Helinski"}

  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(defn ctx

  ""

  []

  (-> ($.cvm/import {"src/convex/break/util.cvx" '$})
      ($.cvm/set-juice Long/MAX_VALUE)))
