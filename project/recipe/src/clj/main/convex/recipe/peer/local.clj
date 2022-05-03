(ns convex.recipe.peer.local

  "This example creates a standalone peer which do not talk to any other peer.
  
   Might be useful for development. Overall, it is a stepping stone for understanding the more complex
   setup of a peer participating in the test network. See `convex.recipe.peer.testnet`."

  {:author "Adam Helinski"}

  (:require [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.db              :as $.db]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.server          :as $.server]))


;;;;;;;;;; Creating a local server


(defn server

  "Creates a new peer server meant to run only locally, not connected to any other peer."

  [dir option+]

  ;; To retrieve the key pair from a file (or generate it in the first place), we reuse the
  ;; recipe from `convex.recipe.key-pair`.
  ;;
  ;; A database file is also created in the given `dir` or reused.
  ;;
  ($.server/create ($.recipe.key-pair/retrieve dir)
                   (merge {:convex.server/db ($.db/open (str dir
                                                             "/db.etch"))}
                          option+)))


;;;;;;;;;;


(comment


  ;; Directory for storing files: DB and key pair
  ;;
  ;; Everything is prepared the first time. We can always delete those files if we want to start again from scratch.
  ;;
  (def dir
       "private/recipe/peer/local")


  ;; Creates a peer server running locally, without any connection to any other peer.
  ;;
  ;; A genesis state is create: minimal state loading Convex libraries and actors (automated accounts).
  ;;
  (def s
       (server dir
               {:convex.server/host "localhost"
                :convex.server/port 18888}))


  ;; Starts the server
  ;;
  ($.server/start s)


  ;; Creates a new client connected to our peer.
  ;;
  ;; We could use the same client as presented in the [[convex.recipe.peer.local]] recipe which connects to `localhost18888`
  ;; by default.
  ;; However, let us use a variant optimized for talking to a peer server running in the same process.
  ;; Some applications will operate like this and it is useful having such an optimized client.
  ;;
  (def c
       ($.client/connect-local s))


  ;; We can use account 12. It is created as a controller account for the peer, as mandated, and it has the same key pair
  ;; as the peer it controls.
  ;;
  (def addr
       ($.cell/address 12))


  ;; Let us retrieve the key pair we used for the peer.
  ;; Same as its controller account, hence we can use to transact.
  ;;
  (def kp
       ($.recipe.key-pair/retrieve dir))


  ;; Transaction example using account #12.
  ;;
  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (deref ($.client/sequence c
                                                                  addr))
                                        ($.read/string "(def foo 42)")))
      deref
      str)


  ;; Query proving that our transaction worked.
  ;;
  (-> ($.client/query c
                      addr
                      ($.cell/symbol "foo"))
      deref
      str)
                         

  ;; When done, we can stop our resources.
  ;;
  (do
    ($.client/close c)
    ($.server/stop s))


  )
