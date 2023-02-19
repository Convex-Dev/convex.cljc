(ns convex.shell.req.testnet

  "Requests for REST methods provided by `convex.world`."

  {:author "Adam Helinski"}

  (:import (convex.core.lang.impl ErrorValue))
  (:require [clojure.data.json  :as json]
            [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]
            [hato.client        :as http]
            [promesa.core       :as P]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(defn- -post

  ;; Issues a POST request.

  [ctx url body f-body error-message]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create
                      (-> (http/post url
                                      {:async?             true
                                       :body               (json/write-str body)
                                       :connection-timeout 20000
                                       :timeout            120000})
                          (P/then (fn [resp]
                                    [true
                                     (-> resp
                                         (:body)
                                         (json/read-str)
                                         (f-body))]))
                          (P/catch (fn [_ex]
                                     [false
                                      (doto
                                        (ErrorValue/create ($.cell/keyword "SHELL.TESTNET")
                                                           ($.cell/string error-message))
                                        (.setAddress ($.cvm/address ctx)))]))))))


;;;;;;;;;; Requests


(defn create-account

  "Request for creating a new account."

  [ctx [public-key]]

  (or (when-not (and ($.std/blob? public-key)
                     (= ($.std/count public-key)
                        32))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Public key must be a 32-byte Blob")))
      (-post ctx
             "https://convex.world/api/v1/createAccount"
             {"accountKey" ($.clj/blob->hex public-key)}
             (fn [body]
               (-> body
                   (get "address")
                   ($.cell/address)))
             "Unable to create a new account")))



(defn faucet

  "Request for receiving Convex Coins."

  [ctx [address amount]]

  (or (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provid an Address")))
      (when-not ($.std/long? amount)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Amount must be a Long")))
      (let [amount-2 ($.clj/long amount)]
        (or (when (neg? amount-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Amount must be >= 0")))
            (when (> amount-2
                     100000000)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Amount must be <= 100000000")))
            (-post ctx
                   "https://convex.world/api/v1/faucet"
                   {"address" ($.clj/address address)
                    "amount"  ($.clj/long amount)}
                   (fn [body]
                     (-> body
                         (get "value")
                         ($.cell/long)))
                   "Unable to call the Faucet API for the request account")))))
