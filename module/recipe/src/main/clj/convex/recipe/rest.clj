(ns convex.recipe.rest

  "`convex.world` offers a REST API for consumers who cannot use the fact binary client, like the browser.
  
   Since Clojure applications can, it is overall not particularly useful since the binary client is much more convenient
   and efficient.
  
   However, a couple of methods are still useful and are showed here. A Clojure API is not provided because it is hard providing
   an interface for a REST API without forcing users to select a particular HTTP client, and there are quite a few Clojure libraries
   in that space.

   More information about the REST API: https://convex.world/tools/rest-api"

  {:author "Adam Helinski"}

  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]
            [convex.key-pair   :as $.key-pair]))


;;;;;;;;;; Useful methods


(defn create-account

  "Creates a new account using the `convex.world` REST API.

   New accounts can be created in a transaction using the Convex Lisp `create-account` function. However,
   we need an account to do a transaction, inducing a chicken or egg problem. This is why we use the REST
   API, it creates an account for us.

   More information on the REST API: https://convex.world/tools/rest-api/create-an-account"

  [key-pair]

  (-> (http/post "https://convex.world/api/v1/createAccount"
                 {:body               (json/write-str {"accountKey" ($.key-pair/hex-string key-pair)})
                  :connection-timeout 4000})
      :body
      json/read-str
      (get "address")))



(defn request-coin+

  "Calls the `convex.world` REST API to an amount of coins to be transferred on the given account (address
   provided as a long).

   A request can be made every 5 minutes at most with a maximum amount of 100 millions coins.
  
   More information on the REST API: https://convex.world/tools/rest-api/request-coins"

  [address amount]

  (-> (http/post "https://convex.world/api/v1/faucet"
                 {:body               (json/write-str {"address" address
                                                       "amount"  amount})
                  :connection-timeout 4000})
      :body
      json/read-str))


;;;;;;;;;;


(comment


  ;; A key pair is needed when creating an account.
  ;;
  ;; Also see `convex.recipe.key-pair`.
  ;;
  (def key-pair
       ($.key-pair/ed25519))


  ;; Creates a new account.
  ;;
  (def addr
       (create-account key-pair))


  ;; Initially, a new account has 0 coins. Let's request for a 100 millions.
  ;; Sounds like a like but total supply if 1e18.
  ;;
  (request-coin+ addr
                 100000000)


  )
