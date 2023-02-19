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

  "Request for creating a new account on `convex.world`."

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
                                          :throw-exceptions?   false
                                          })
                              (:body)
                              (json/read-str)
                              (get "address")
                              ($.cell/address)))
        (catch Exception _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.TESTNET)
                               ($.cell/* "Unable to create a new account, is testnet up?"))))))
