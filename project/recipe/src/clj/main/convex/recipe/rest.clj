(ns convex.recipe.rest

  ""

  {:author "Adam Helinski"}

  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]
            [convex.sign       :as $.sign]))


;;;;;;;;;; Useful methods


(defn create-account

  ""

  [key-pair]

  (-> (http/post "https://convex.world/api/v1/createAccount"
                 {:body               (json/write-str {"accountKey" ($.sign/hex-string key-pair)})
                  :connection-timeout 4000})
      :body
      json/read-str
      (get "address")))



(defn request-coin+

  ""

  [address]

  (-> (http/post "https://convex.world/api/v1/faucet"
                 {:body               (json/write-str {"address" address
                                                       "amount"  100000000})
                  :connection-timeout 4000})
      :body
      json/read-str))


;;;;;;;;;;


(comment


  (def kp
       ($.sign/ed25519))


  (def addr
       (create-account kp))


  (request-coin+ addr)


  )
