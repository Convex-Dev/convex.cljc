(ns convex.recipe.peer.local

  "This example creates a standalone peer which do not talk to any other peer.
  
   Might be useful for development. Overall, it is a stepping stone for understanding the more complex
   setup of a peer participating in the test network. See `convex.recipe.peer.testnet`."

  {:author "Adam Helinski"}

  (:require [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.cvm             :as $.cvm]
            [convex.db              :as $.db]
            [convex.key-pair        :as $.key-pair]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.server          :as $.server]))


;;;;;;;;;;


(comment


  ;; Directory for storing files: DB and key pair
  ;;
  ;; Everything is prepared the first time. We can always delete those files if we want to start again from scratch.
  ;;
  (def dir
       "private/recipe/peer/local")


  ;; Our peer will need a controller account.
  ;;
  ;; We will need a key pair both for the controller and the peer.
  ;; Controller needs one to interact with the network while the peer needs one for signing blocks.
  ;;
  ;; In this example, to keep things simple, let's use one key pair.
  ;; This is fine for testing purposes but quite insecure for production.
  ;; Let us create a key-pair for it.
  ;;
  (def key-pair
       ($.recipe.key-pair/retrieve dir))

  ;; Let us create our own genesis state.
  ;;
  ;; The public key of the key pair above is provided for the genesis account that will act as our controller.
  ;;
  (def genesis-state
       (-> ($.cvm/ctx {:convex.cvm/genesis-key+ [($.key-pair/account-key key-pair)]})
           ($.cvm/state)))

  ;; Hence this is the address of the controller.
  ;;
  (def controller
       $.cvm/genesis-user)


  ;; Lastly, we need an Etch instance.
  ;;
  (def db
       (-> (str dir "/db.etch")
           ($.db/open)
           ($.db/current-set)))


  ;; Now a server can be created by providing what has been prepared.
  ;; Key pair will be used for signing blocks of transactions.
  ;;
  (def server
       ($.server/create key-pair
                        {:convex.server/controller controller
                         :convex.server/db         db
                         :convex.server/host       "localhost"
                         :convex.server/port       18888  ;; default port
                         :convex.server/state      [:use genesis-state]}))

  ;; And finally, let's start the peer.
  ;;
  ($.server/start server)




  ;; Creates a new client connected to our peer.
  ;;
  ;; We could use the same client as presented in the [[convex.recipe.peer.local]] recipe which connects to `localhost:18888`
  ;; by default.
  ;; However, let us use a variant optimized for talking to a peer server running in the same process.
  ;; Some applications will operate like this and it is useful having such an optimized client.
  ;;
  (def client
       ($.client/connect-local server))


  ;; Note: because the client runs on the same thread as the server, it will use the same Etch instance.


  ;; For the sake of simplicity, we can reuse our controller for testing the network.
  ;;
  ;; For learning about transaction, see `convex.recipe.client`.
  ;;
  (-> ($.client/transact client
                         ($.key-pair/sign key-pair
                                          ($.cell/invoke controller
                                                         (deref ($.client/sequence-id client
                                                                                      controller))
                                                         ($.cell/* (def foo (inc 41))))))
      (deref))


  ;; Query proving that our transaction worked.
  ;;
  (-> ($.client/query client
                      controller
                      ($.cell/* foo))
      (deref))
                         

  ;; When done, we can stop our resources.
  ;;
  (do
    ($.client/close client)
    ($.server/stop server)
    ($.db/close))


  )
