(ns convex.shell.req.client

  "Requests relating to the binary client."
  
  (:import (convex.api Convex))
  (:require [convex.cell        :as $.cell]
            [convex.client      :as $.client]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.db          :as $.db]
            [convex.shell.async :as $.shell.async]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]))


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
                    ($.shell.async/return ctx
                                          (delay
                                            ($.client/query client-2
                                                            address
                                                            code))
                                          (fn [_ex]
                                            [($.cell/* :SHELL.CLIENT)
                                             ($.cell/* "Unable to issue a query")]))))))
