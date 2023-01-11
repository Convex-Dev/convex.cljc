(ns convex.shell.req.account

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn switch

  [ctx [address]]

  (or (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide an address for switching to another account")))
      (when-not ($.cvm/account ctx
                               address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :NOBODY)
                             ($.cell/* "Cannot switch to an inexistent account")))
      (-> ctx
          ($.cvm/fork-to address)
          ($.cvm/result-set address))))
