(ns convex.shell.req.log

  {:author "Adam Helinski"}

  (:require [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [taoensso.timbre :as log]))


;;;;;;;;;;


(defn level

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/any (:min-level log/*config*))))

