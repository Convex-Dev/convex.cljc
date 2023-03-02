(ns convex.shell.req.trx

  "Requests relating to creating and applying transactions."

  (:import (convex.core.transactions ATransaction))
  (:refer-clojure :exclude [sequence])
  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private helpers


(defn- -ensure-origin

  [ctx origin]

  (when-not ($.std/address? origin)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/* "Origin of a transaction must be an address"))))



(defn- -ensure-sequence-id

  [ctx sequence-id]

  (when-not ($.std/long? sequence-id)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/* "Sequence ID must be an incrementing Long"))))



(defn ^:no-doc -ensure-trx

  [ctx ^ATransaction trx]

  (or (when-not ($.std/transaction? trx)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Not a transaction")))
      (when ($.cvm/actor? ctx
                          (.getOrigin trx))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Cannot transact for an actor")))))


;;;;;;;;;; Requests for applying transactions


(defn trx

  "Request for applying an unsigned transaction."

  [ctx [trx]]

  (or (-ensure-trx ctx
                   trx)
      ($.cvm/transact ctx
                      trx)))



(defn trx-noop

  "Request with the same overhead as [[trx]] but does not apply the transaction.
   Probably only useful for benchmarking."

  [ctx [trx]]

  (or (-ensure-trx ctx
                   trx)
      ($.cvm/result-set ctx
                        nil)))


;;;;;;;;;; Requests for creating transactions


(defn new-call

  "Request for creating a new call transaction."

  [ctx [origin sequence-id target offer function arg+]]

  (or (-ensure-origin ctx
                      origin)
      (-ensure-sequence-id ctx
                           sequence-id)
      (when-not ($.std/address? target)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Callable target must be an address")))
      (when-not ($.std/long? offer)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Offer must be a Long")))
      (let [offer-2 ($.clj/long offer)]
        (or (when (neg? offer-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Offer must be >= 0")))
            (when-not ($.std/symbol? function)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Function to call must be a symbol")))
            (when-not (or (nil? arg+)
                          ($.std/vector? arg+))
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Arguments must be Nil or in a Vector")))
            ($.cvm/result-set ctx
                              ($.cell/call origin
                                           ($.clj/long sequence-id)
                                           target
                                           offer-2
                                           function
                                           arg+))))))



(defn new-invoke

  "Request for creating a new invoke transaction."

  [ctx [origin sequence-id command]]

  (or (-ensure-origin ctx
                      origin)
      (-ensure-sequence-id ctx
                           sequence-id)
      ($.cvm/result-set ctx
                        ($.cell/invoke origin
                                       ($.clj/long sequence-id)
                                       command))))



(defn new-transfer

  "Request for creating a new transfer transaction."

  [ctx [origin sequence-id target amount]]

  (or (-ensure-origin ctx
                      origin)
      (-ensure-sequence-id ctx
                           sequence-id)
      (when-not ($.std/address? target)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Recipient must be an address")))
      (when-not ($.std/long? amount)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Amount to send must be a Long")))
      (let [amount-2 ($.clj/long amount)]
        (or (when-not (>= amount-2
                          0)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Amount to transfer must be >= 0")))
            ($.cvm/result-set ctx
                              ($.cell/transfer origin
                                               ($.clj/long sequence-id)
                                               target
                                               amount-2))))))


;;;;;;;;;; Transaction parameters


(defn origin

  "Request returning the origin of the given `trx`."

  [ctx [^ATransaction trx]]

  (or (-ensure-trx ctx
                   trx)
      ($.cvm/result-set ctx
                        (.getOrigin trx))))



(defn sequence

  "Request returning the sequence ID of the given `trx`."

  [ctx [^ATransaction trx]]

  (or (-ensure-trx ctx
                   trx)
      ($.cvm/result-set ctx
                        ($.cell/long (.getSequence trx)))))



(defn with-sequence

  "Request for returning `trx` as a new transaction with an updated sequence ID."

  [ctx [^ATransaction trx sequence-id]]

  (or (-ensure-trx ctx
                   trx)
      (-ensure-sequence-id ctx
                           sequence-id)
      ($.cvm/result-set ctx
                        (.withSequence trx
                                       ($.clj/long sequence-id)))))
