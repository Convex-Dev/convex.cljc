(ns convex.shell.req.trx

  "Requests relating to creating and applying transactions."

  (:import (convex.core.transactions ATransaction))
  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn exec

  "Request for applying an unsigned transaction."

  [ctx [^ATransaction trx]]

  (or (when-not ($.std/transaction? trx)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Not a transaction")))
      (when ($.cvm/actor? ctx
                          (.getOrigin trx))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Cannot transact for an actor")))
      ($.cvm/transact ctx
                      trx)))



(defn trx

  "Request for creating a transaction."

  [ctx [address sequence-id code]]

  (or (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "A transaction requires an address")))
      (when-not ($.std/long? sequence-id)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "A transaction needs a sequence ID for the issuing account")))
      ($.cvm/result-set ctx
                        ($.cell/invoke address
                                       ($.clj/long sequence-id)
                                       code))))
