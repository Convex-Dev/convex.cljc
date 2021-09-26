(ns convex.recipe.peer.testnet

  "This example creates a peer connected to the current test network.

   It takes care of all necessary steps, notably:

   - Generating a key pair and storing it in a PFX file for reuse
   - Creating a \"controller\" account for the peer and use it to declare a new peer on the network

   Embedding a peer server in an application has some interesting advantages. For instance, having direct access
   to its database. For instance, `convex.world` is a Clojure application embedding a peer: https://convex.world/

   API: https://cljdoc.org/d/world.convex/net.clj/0.0.0-alpha0/api/convex.server
   More information about peer operations: https://convex.world/cvm/peer-operations
  
   <!> When launching the REPL, set the environment variable 'TIMBRE_LEVEL=:debug'.
       This will log everything that happens on the peer, such as seeing new data arrive, which is interesting."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.db              :as $.db]
            [convex.form            :as $.form]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.recipe.rest     :as $.recipe.rest]
            [convex.server          :as $.server]
            [convex.sign            :as $.sign]))


;;;;;;;;;; Peer must be declared and staked on the network


(defn ensure-declared

  "If peer has already been declared, then an EDN file should exit in `dir`.
  
   When not found, a new account with a 100 million coins is created using the `convex.world` REST API
   (see `convex.recipe.rest`)
  
   Then, a new peer is registered on the network by using that account to issue a transaction on the network.
   Transaction call the Convex Lisp function `create-peer`, specifies the public key, and stake 50 millions coins
   so that the peer can participate in consensus (must be > 0).

   The account that calls `create-peer` becomes the controller of the declared peer.
  
   Lastly, the address of that account is saved in an EDN file so that it can be reused."

  [dir key-pair]

  (let [path (str dir
                  "/peer.edn")]
    (when-not (.exists (File. path))
      (let [addr   ($.recipe.rest/create-account key-pair)
            _      ($.recipe.rest/request-coin+ addr
                                                100000000)
            client ($.client/connect {:convex.server/host "convex.world"
                                      :convex.server/port 18888})
            resp   (-> ($.client/transact client
                                          key-pair
                                          ($.cell/invoke ($.cell/address addr)
                                                         1  ;; sequence ID for that account, it's new so we know its the first trx.
                                                         ($.form/create-peer ($.sign/account-key key-pair)
                                                                             ($.cell/long 50000000))))
                       (deref 4000
                              nil))]
        ($.client/close client)
        (when (nil? resp)
          (throw (ex-info "Timeout when declaring peer!"
                          {})))
        (spit path
              (pr-str {:address addr})))))
  nil)


;;;;;;;;;; Peer server


(defn server

  "Creates a new peer server that will participate in the test network.

   Takes care of generating a key pair, creating a controller account, and declaring the peer on the network
   if needed. See above utilities.

   A map of additional server options may be provided (see API and below).

   API for peer servers: https://cljdoc.org/d/world.convex/net.clj/0.0.0-alpha0/api/convex.server"

  [dir option+]

  ;; To retrieve the key pair from a file (or generate it in the first place), we reuse the
  ;; recipe from `convex.recipe.key-pair`.
  ;;
  (let [key-pair ($.recipe.key-pair/retrieve dir)]
    ;;
    ;; Ensures a peer associated with the owned key pair has been declared on the network by its
    ;; controller account.
    ;;
    (ensure-declared dir
                     key-pair)
    ;;
    ;; We mention that initial state must be retrieved from `convex.world` on port 18888.
    ;;
    ($.server/create key-pair
                     (merge {:convex.server/db    ($.db/open (str dir
                                                                  "/db.etch"))
                             :convex.server/state [:sync
                                                   {:convex.server/host "convex.world"
                                                    :convex.server/port 18888}]}
                            option+))))


;;;;;;;;;; Now we can run the peer!


(comment


  ;; Directory for storing files: DB, EDN info about peer, and key pair
  ;;
  ;; Everything is prepared the first time. We can always delete those files if we want to start again from scratch.
  ;;
  (def dir
       "private/recipe/peer/testnet")


  ;; Prepares server for running our peer.
  ;;
  ;; If you want the peer to fully participate in the consensus, it must be accessible to the outside world. This usually
  ;; involves some port mapping in your router, so that the router can redirect outside traffic to the machine that runs
  ;; the peer. The option `convex.server/url` is what will be posted on the network so that other peers can broadcast
  ;; new information to your peer using that URL.
  ;;
  ;; If your peer is not accessible from the outside, it will not be able to take active part in the consensus because
  ;; other peers will not be able to broadcast information to it. However, it will still work, broadcast the new information
  ;; that it gets, and be able to do belief polling as described below.
  ;;
  ;; Does a "peer sync" against `convex.world`. Peer syncing means that the state of the network is polled from a trusted peer
  ;; so that we can catch up with what is going on. Can take anywhere in between seconds to minutes, depending on how big the
  ;; network state is, how catch up there is to do, and issual network delays.
  ;;
  (time
    (def s
         (server dir
                 {:convex.server/host "0.0.0.0"
                  :convex.server/port 18888
                  :convex.server/url  "MY_PUBLIC_IP:18888"})))


  ;; Starts the server.
  ;;
  ;; Will get up to date if needed and then starts a cycle of "belief polling".
  ;;
  ;; A belief is essentially a peer view of the current state of the network. Polling every second or so helps in being up
  ;; to date even if it misses broadcasts from other peers.
  ;;
  ($.server/start s)

  
  ;; Now, we can try anything in the sandbox which is connected to `convex.world` and each time, see our own peer
  ;; getting up-to-date.


  ;; Creates a new client connected to our peer.
  ;;
  ;; By default, unless specified otherwise, a client connects to `localhost:18888`, so we are good.
  ;;
  (def c
       ($.client/connect))


  ;; We can query our peer as we want.
  ;;
  ;; For instance, defines something in an account using the sandbox.
  ;; Then, you can query your peer to ensure it is up-to-date.
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)  ;; "Execute query as", any address can be used, this is only a read operation.
                      ($.read/string "(+ 2 2)"))
      deref
      str)


  ;; When done, we can stop our resources.
  ;;
  (do
    ($.client/close c)
    ($.server/stop s))


  )
