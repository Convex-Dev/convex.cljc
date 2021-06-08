(ns convex.break

  "Miscellaneous utilities related to the Break project."

  {:author "Adam Helinski"}

  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.file :as $.cvm.file]))


;;;;;;;;;;


(defn ctx

  "Prepares a default context for dev and testing."

  []

  (-> ($.cvm/ctx)
      $.cvm/juice-refill
      ($.cvm.file/deploy '$
                         "src/convex/break/util.cvx")))
