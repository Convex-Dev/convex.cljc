(ns convex.shell.req.account

  "Requests relating to accounts."

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn switch

  "Requests for switching the context to another address.
  
   Returns a context which has lost local bindings and such useful information.
   However, this is fine when this request is called via `.account.switch`, a real
   CVX function which will automatically restore all that. Things do go wrong if the
   user calls `(.shell.invoke 'account.switch ...)` directly (but shouldn't have to do
   that."

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
