(ns convex.recipe.peer.testnet

  "This example creates a peer connected to the current test network.

   It takes care of all necessary steps, notably:

   - Generating a key pair and storing it in a PFX file for reuse
   - Creating a \"controller\" account for the peer and use it to declare a new peer on the network

   Embedding a peer server in an application has some interesting advantages. For instance, having direct access
   to its database.

   For instance, `convex.world` is a Clojure application embedding a peer: https://convex.world/
  
   <!> When launching the REPL, set the environment variable 'TIMBRE_LEVEL=:debug'.
       This will log everything that happens on the peer, such as seeing new data arrive, which is interesting."

  {:author "Adam Helinski"}

  (:require [clj-http.client        :as http]
            [clojure.data.json      :as json]
            [clojure.edn            :as edn]
            [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.db              :as $.db]
            [convex.form            :as $.form]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.server          :as $.server]
            [convex.sign            :as $.sign]))


;;;;;;;;;; Creating a new controller account for the peer and declaring the peer on the network


(defn declare-peer

  "Any new peer must be declared on the network using the `create-peer` Convex Lisp function in a
   transaction.

   Account that signs this transaction become the \"controller\" of the peer. Currently, both must
   share the same key pair.

   More information about peer operations: https://convex.world/cvm/peer-operations

   API for clients: https://cljdoc.org/d/world.convex/net.clj/0.0.0-alpha0/api/convex.client

   API for creating cells and transactions: https://cljdoc.org/d/world.convex/cvm.clj/0.0.0-alpha1/api/convex"

  [key-pair address]

  ;; Client to `convex.world` is created to transact the creating of the peer.
  ;;
  ;; We know the account is brand new (see [[create-account]]).
  ;; It has 100000000 coins, we stake half of it on the peer to give it weight during consensus (must be > 0).
  ;; We know it is the first transaction of this account, hence a sequence number of 1 is provided for that transaction.

  (let [client ($.client/connect {:convex.server/host "convex.world"
                                  :convex.server/port 18888})]
    (try

      (deref ($.client/transact client
                                key-pair
                                ($.cell/invoke address
                                               1
                                               ($.form/create-peer ($.sign/account-key key-pair)
                                                                   ($.cell/long 50000000))))
             4000
             nil)

      (finally
        ($.client/close client)))))



(defn request-coin+

  "Calls the `convex.world` REST API to add 100 million coins to the account with the given address
   (provided as a long).
  
   More information on the REST API: https://convex.world/tools/rest-api/request-coins"

  [address-long]

  (http/post "https://convex.world/api/v1/faucet"
             {:body               (json/write-str {"address" address-long
                                                   "amount"  100000000})
              :connection-timeout 4000}))


(defn create-account

  "Creates a new account using the `convex.world` REST API.

   New accounts can be created in a transaction using the Convex Lisp `create-account` function. However,
   we need an account to do a transaction. This is why we use the REST API, it creates an account for us.

   After creation, [[request-coin+]] funds that account with 100 million coins.

   Then, a new peer is registered on the network using [[declare-peer]] and coins at staked on it so that
   it can participate in consensus.

   Lastly, the address of the account is saved in a file in `dir`. When restarting, [[address]] is used and tries
   to read that file first. A new account, with this whole ceremony, is created only when required.
  
   More information on the REST API: https://convex.world/tools/rest-api/create-an-account"

  [dir key-pair]

  (let [address-long (-> (http/post "https://convex.world/api/v1/createAccount"
                                    {:body               (json/write-str {"accountKey" ($.sign/hex-string key-pair)})
                                     :connection-timeout 4000})
                         :body
                         json/read-str
                         (get "address"))
        address      ($.cell/address address-long)]
    (request-coin+ address-long)
    (when (nil? (declare-peer key-pair
                              address))
      (throw (ex-info "Timeout when declaring peer!"
                      {})))
    (spit (str dir
               "/peer.edn")
          (pr-str {:address address-long}))
    address))


;;;;;;;;;; Retrieves the controller account of the peer or creates a new one


(defn controller

  "Tries to read from a file in `dir` the account that was used for declaring a peer.
  
   When not found, uses [[create-account]] to create an new account and declare a new peer on the network."

  [dir key-pair]

  (try

    (-> (slurp (str dir
                    "/peer.edn"))
        edn/read-string
        (get :address)
        $.cell/address)
    
    (catch Throwable _ex
      (create-account dir
                      key-pair))))


;;;;;;;;;; Peer server


(defn server

  "Creates a new server.

   Takes care of generating a key pair, creating a controller account, and declaring the peer on the network
   if needed. See above utilities.

   A map of additional server options may be provided (see API and below).

   API for peer servers: https://cljdoc.org/d/world.convex/net.clj/0.0.0-alpha0/api/convex.server"


  ([dir]

   (server dir
           nil))


  ([dir option+]

   ;; To retrieve the key pair from a file (or generate it in the first place), we reuse the
   ;; recipe from `convex.recipe.key-pair`.
   ;;
   (let [kp ($.recipe.key-pair/retrieve dir)]
     ;;
     ;; Ensures a peer associated with the owned key pair has been declared on the network by its
     ;; controller account.
     ;;
     (controller dir
                 kp)
     ($.server/create kp
                      (merge {:convex.server/db    ($.db/open (str dir
                                                                   "/db.etch"))
                              :convex.server/state [:sync
                                                    {:convex.server/host "convex.world"}]}
                             option+)))))


;;;;;;;;;; Now we can run the peer!


(comment


  ;; Directory for storing files: DB, EDN info about peer, and key pair
  ;;
  ;; Everything is prepared the first time. We can always delete those files if we want to start again from scratch.
  ;;
  (def dir
       "private/recipe/peer/")


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


  ;; Creates a new client to our peer.
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


  ;; When done, we can stop the server.
  ;;
  ($.server/stop s)


  )
