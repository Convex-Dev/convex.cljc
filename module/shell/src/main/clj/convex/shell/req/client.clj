(ns convex.shell.req.client

  "Requests relating to the binary client."

  (:import (convex.api Convex)
           (convex.core.lang.ops Special)
           (java.nio.channels ClosedChannelException))
  (:refer-clojure :exclude [resolve
                            sequence])
  (:require [convex.cell           :as $.cell]
            [convex.client         :as $.client]
            [convex.clj            :as $.clj]
            [convex.cvm            :as $.cvm]
            [convex.db             :as $.db]
            [convex.key-pair       :as $.key-pair]
            [convex.shell.async    :as $.shell.async]
            [convex.shell.req.kp   :as $.shell.req.kp]
            [convex.shell.req.peer :as $.shell.req.peer]
            [convex.shell.resrc    :as $.shell.resrc]
            [convex.std            :as $.std]
            [promesa.core          :as P]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(defn- -do-client

  ;; Executes `f` with an unwrapped client.

  [ctx client f]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   client)]
    (if ok?
      (let [client-2 x]
        (or (when-not (instance? Convex
                                 client-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Not a client")))
            (f client-2)))
      (let [ctx-2 x]
        ctx-2))))



(defn- -return

  ;; Handles returning a Future for a query or a transaction

  [ctx d*future error-message]

  ($.shell.async/return ctx
                        d*future
                        (fn [ex]
                          [($.cell/* :SHELL.CLIENT)
                           ($.cell/string (if (instance? ClosedChannelException
                                                         ex)
                                            "Connection severed"
                                            error-message))])))



(defn- -return-trx

  ;; Handles returning a Future for a transaction.

  [ctx d*future]

  (-return ctx
           d*future
           "Unable to issue this transaction"))


;;;;;;;;;; Requests


(defn close

  "Request for closing a client."

  [ctx [client]]

  (-do-client ctx
              client
              (fn [client-2]
                (try
                  ;;
                  ($.client/close client-2)
                  ($.cvm/result-set ctx
                                    nil)
                  ;;
                  (catch Exception _ex
                    ($.cvm/exception-set ctx
                                         ($.cell/* :SHELL.CLIENT)
                                         ($.cell/* "Unable to close client")))))))



(defn connect

  "Request for connecting to a peer."

  [ctx [host port]]

  (or (when-not ($.std/string? host)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Hostname must be a String")))
      (when-not ($.std/long? port)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Port must be a Long")))
      (when-not ($.db/current)
        ($.cvm/exception-set ctx
                             ($.cell/* :SHELL.CLIENT)
                             ($.cell/* "An Etch instance must be open, see `(?.shell '.db)`")))
      (let [port-2 ($.clj/long port)]
        (or (when-not (<= 1
                          port-2
                          65535)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Port must be >= 1 and <= 65535")))
            (try
              ($.cvm/result-set ctx
                                ($.shell.resrc/create
                                  ($.client/connect {:convex.server/host ($.clj/string host)
                                                     :convex.server/port port-2})))
              (catch Exception _ex
                ($.cvm/exception-set ctx
                                     ($.cell/* :SHELL.CLIENT)
                                     ($.cell/* "Unable to connect"))))))))



(defn peer-endpoint

  "Request for retrieving the endpoint the client is connected to."

  [ctx [client]]

  (-do-client ctx
              client
              (fn [client-2]
                ($.cvm/result-set ctx
                                  (-> client-2
                                      ($.client/endpoint)
                                      ($.shell.req.peer/-endpoint->map))))))



(defn peer-status

  "Request for retrieving the current peer status"

  [ctx [client]]

  (-do-client ctx
              client
              (fn [client-2]
                (-return ctx
                         (delay
                           ($.client/peer-status client-2))
                         "Unable to retrieve the current peer status"))))



(defn query

  "Request for issuing a query."

  [ctx [client address code]]

  (or (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Not an Address")))
      (-do-client ctx
                  client
                  (fn [client-2]
                    (-return ctx
                             (delay
                               ($.client/query client-2
                                               address
                                               code))
                             "Unable to issue this query")))))



(defn query-state

  "Request for fetching the peer's `State`."

  [ctx [client]]

  (-do-client ctx
              client
              (fn [client-2]
                (-return ctx
                         (delay
                           ($.client/state client-2))
                         "Unable to fetch the State"))))



(defn resolve

  "Request for resolving a hash to a cell."

  [ctx [client hash]]

  (or (when-not (and ($.std/blob? hash)
                     (= ($.std/count hash)
                        32))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Hash to resolve must be a 32-byte Blob")))
      (-do-client ctx
                  client
                  (fn [client-2]
                    (-return ctx
                             (delay
                               ($.client/resolve client-2
                                                 ($.cell/hash<-blob hash)))
                             "Unable to resolve hash")))))



(let [op (Special/forSymbol ($.cell/* *sequence*))]

  (defn sequence

    "Request for retrieving the next sequence ID."

    [ctx [client address]]

    (or (when-not ($.std/address? address)
          ($.cvm/exception-set ctx
                               ($.cell/code-std* :ARGUMENT)
                               ($.cell/* "Need an Address to retrieve the sequence ID")))
        (-do-client ctx
                    client
                    (fn [client-2]
                      (-return ctx
                               (delay
                                 (-> ($.client/query client-2
                                                     address
                                                     op)
                                     (P/then (fn [q]
                                               (when-not ($.client/result->error-code q)
                                                 ($.std/inc ($.client/result->value q)))))))
                               "Unable to retrieve next sequence ID"))))))



(defn transact

  "Request for signing and issuing a transaction."

  [ctx [client kp trx]]

  (or (when-not ($.std/transaction? trx)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Requires a transaction, see `(?.shell '.trx)`")))
      (-do-client ctx
                  client
                  (fn [client-2]
                    ($.shell.req.kp/do-kp ctx
                                          kp
                                          (fn [kp-2]
                                            (-return-trx ctx
                                                         (delay
                                                           ($.client/transact client-2
                                                                              ($.key-pair/sign kp-2
                                                                                               trx))))))))))



(defn transact-signed

  "Request for issuing a signed transaction."

  [ctx [client signed-trx]]

  (or (when (or (not ($.std/signed? signed-trx))
                (not ($.std/transaction? ($.key-pair/signed->cell signed-trx))))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Not a signed transaction")))
      (-do-client ctx
                  client
                  (fn [client-2]
                    (-return-trx ctx
                                 (delay
                                    ($.client/transact client-2
                                                       signed-trx)))))))
