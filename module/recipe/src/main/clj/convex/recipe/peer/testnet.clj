(ns convex.recipe.peer.testnet

  "These examples create a peer connected to the current test network.

   It takes care of all necessary steps, notably:

   - Generating a key pair and storing it in a PFX file for reuse
   - Creating a \"controller\" account for the peer and using it to declare a new peer on the network

   Embedding a peer server in an application has some interesting advantages. For instance, having direct access
   to its Etch instance. `https://convex.world` is a Clojure application embedding a peer.

   Before attempting these examples, ensure you are comfortable with [[convex.recipe.peer.local]]."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.db              :as $.db]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.recipe.rest     :as $.recipe.rest]
            [convex.server          :as $.server]
            [convex.sign            :as $.sign]))


;;;;;;;;;;


(comment


  ;; Directory for storing files: DB, EDN info about peer, and key pair
  ;;
  ;; Everything is prepared the first time. We can always delete those files if we want to start again from scratch.
  ;;
  (def dir
       "private/recipe/peer/testnet")


  ;; As described in [[convex.recipe.peer.local]], a peer needs a key pair for signing blocks as well
  ;; as a controller account which in turn needs a key pair for transacting on-chain.
  ;;
  ;; For the sake of simplicity, let's create one and reuse it.
  ;;
  (def key-pair
       ($.recipe.key-pair/retrieve dir))


  ;; Our Etch instance.
  ;;
  (def db
       (-> (str dir "/db.etch")
           ($.db/open)
           ($.db/current-set)))


  ;; The first concrete step is declaring our peer on chain.
  ;; The next form shows one way of handling that part.
  ;;
  ;; If the peer has already been declared on-chain, then an EDN file should exist in `dir` and we are done.
  ;;
  ;; If not, a new account with a 100 million coins is created using the `https://convex.world` REST API
  ;; (see [[convex.recipe.rest]]). We could use any account but since we do not own one in this example, it
  ;; must be created.
  ;;
  ;; Then, the new peer is registered on the network by using that account to issue a transaction on the network.
  ;; This transaction calls the Convex Lisp function `create-peer`, specifies the public key of the peer, and stakes
  ;; 50 millions coins so that the peer can participate in consensus (must be > 0). Stake gives weight when peer
  ;; continuously vote on the order of transaction blocks.
  ;;
  ;; Note: the account that calls `create-peer` becomes the controller of the declared peer.
  ;;
  ;; Lastly, the address of that account is saved in the aforementioned EDN file so that it can be reused."
  ;;
  ;; To learn more about on-chain peer operations:
  ;;
  ;;   https://convex.world/cvm/peer-operations
  ;;
  (let [path (str dir
                  "/peer.edn")]
    (when-not (.exists (File. path))
      (let [address   ($.recipe.rest/create-account key-pair)
            _         ($.recipe.rest/request-coin+ address
                                                   100000000)
            client    ($.client/connect {:convex.server/host "convex.world"
                                         :convex.server/port 18888})
            result    (-> ($.client/transact client
                                              key-pair
                                              ($.cell/invoke ($.cell/address address)
                                                             ;;
                                                             ;; Sequence ID for that account.
                                                             ;; It's new so we know its the first transaction.
                                                             1  
                                                             ($.cell/* (create-peer ~($.sign/account-key key-pair)
                                                                                    50000000))))
                           (deref 4000
                                  nil))]
        ($.client/close client)
        (cond
          (nil? result)
          (throw (ex-info "Timeout when declaring peer!"
                          {}))
          ;;
          ($.client/result->error-code result)
          (throw (ex-info "Error while declaring peer!"
                          {:result result})))
        (spit path
              (pr-str {:address address})))))
     

  ;; Knowing that our peer is declared on the test network, we can prepare the server.
  ;;
  ;; If you want the peer to fully participate in the consensus, it must be accessible to the outside world. This usually
  ;; involves some port mapping in your router, so that the router can redirect outside traffic to the machine that runs
  ;; the peer. The option `:convex.server/url` is what will be posted on the network so that other peers can broadcast
  ;; new information to your peer using that URL.
  ;;
  ;; If your peer is not accessible from the outside, it will not be able to take active part in the consensus because
  ;; other peers will not be able to broadcast information to it. However, it can still broadcast blocks that it gets
  ;; and keeps it state more or less up-to-date with a polling mechanism under the hood.
  ;;
  ;; A peer server needs a CVM state. Since we aim to join the test network, we will get it by syncing directly with the
  ;; `convex.world` peer. Syncing means that the state of the network is polled from a trusted peer to catch up. This can
  ;; take anywhere from seconds to days depending on the size of the network state.
  ;;
  ;; We recommend launching the REPL with the environment variable 'TIMBRE_LEVEL' set to ':debug'.
  ;; This will log everything happening on the peer server, such as incoming new data, which is interesting when trying
  ;; it out.
  ;;
  (time
    (def server
         ($.server/create key-pair
                          {:convex.server/db    db
                           :convex.server/host  "0.0.0.0"
                           :convex.server/port  18888
                           :convex.server/state [:sync
                                                 {:convex.server/host "convex.world"
                                                  :convex.server/port 18888}]
                           :convex.server/url   "MY_PUBLIC_IP:18888"})))


  ;; Starts the server.
  ;;
  ;; Will get up to date if needed and then starts a cycle of "belief polling".
  ;;
  ;; A belief is essentially a peer view of the current state of the network. Polling every second or so helps in being up
  ;; to date even if it misses broadcasts from other peers.
  ;;
  ($.server/start server)

  
  ;; Now, we can try anything in the sandbox which is connected to `convex.world` and each time, see our own peer
  ;; getting up-to-date.


  ;; Creates a new client connected to our peer.
  ;;
  ;; By default, unless specified otherwise, a client connects to `localhost:18888`, so we are good.
  ;;
  (def client
       ($.client/connect))


  ;; We can query our peer as we want.
  ;;
  ;; For instance, defines something in an account using the sandbox.
  ;; Then, you can query your peer to ensure it is up-to-date.
  ;;
  (-> ($.client/query client
                      ($.cell/address 1)  ;; "Execute query as", any address can be used, this is only a read operation.
                      ($.cell/* (+ 2 2)))
      (deref))


  ;; When done, we can stop our resources.
  ;;
  (do
    ($.client/close client)
    ($.server/stop server)
    ($.server/persist server)
    ($.db/close))


  )
