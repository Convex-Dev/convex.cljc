(ns convex.shell.req.testnet

  "Requests for REST methods provided by `convex.world`."

  (:require [clojure.data.json :as json]
            [convex.cell       :as $.cell]
            [convex.clj        :as $.clj]
            [convex.cvm        :as $.cvm]
            [convex.std        :as $.std]
            [hato.client       :as http]))


;;;;;;;;;;


(defn create-account

  "Request for creating a new account."

  [ctx [public-key]]

  (or (when-not (and ($.std/blob? public-key)
                     (= ($.std/count public-key)
                        32))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Public key must be a 32-byte Blob")))
      (try
        ($.cvm/result-set ctx
                          (-> (http/post "https://convex.world/api/v1/createAccount"
                                         {:body               (json/write-str {"accountKey" ($.clj/blob->hex public-key)})
                                          ;:connection-timeout 4000
                                          ;:timeout            1
                                          })
                              (:body)
                              (json/read-str)
                              (get "address")
                              ($.cell/address)))
        (catch Exception _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.TESTNET)
                               ($.cell/* "Unable to create a new account, is testnet up?"))))))



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
            (try
              ($.cvm/result-set ctx
                                (-> (http/post "https://convex.world/api/v1/faucet"
                                               {:body               (json/write-str {"address" ($.clj/address address)
                                                                                     "amount"  ($.clj/long amount)})
                                                ;:connection-timeout 4000
                                                })
                                    (:body)
                                    (json/read-str)
                                    (get "value")
                                    ($.cell/long)))
              (catch Exception _ex
                ($.cvm/exception-set ctx
                                     ($.cell/* :SHELL.TESTNET)
                                     ($.cell/* "Unable to call the Faucet API for the request account"))))))))
