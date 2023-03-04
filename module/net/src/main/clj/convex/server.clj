(ns convex.server

  "Running a peer server.

   Can either:

   - Run alone for dev and test
   - Run locally, synced with other local peers
   - Run locally but synced with the test network on `convex.world`
  
   See [README](../)."

  {:author "Adam Helinski"}

  (:import (convex.core Belief
                        Peer
                        State)
           (convex.core.data Address
                             Keywords)
           (convex.core.store AStore)
           (convex.peer IServerEvent
                        Server)
           (java.net InetSocketAddress)
           (java.util HashMap)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Creating a new server


(defn create

  "Returns a new peer server.
  
   Can be started using [[start]] when required.

   A key pair is mandatory. See the [[convex.key-pair]].

   An map of options may be provided:

   | Key                               | Value                                                      | Default                                     |
   |-----------------------------------|------------------------------------------------------------|---------------------------------------------|
   | `:convex.server/bind`             | Bind address (string)                                      | `\"localhost\"`                             |
   | `:convex.server/state`            | See below                                                  | `[:genesis]`                                |
   | `:convex.server/controller`       | Controller account address                                 | Retrieved from state                        |
   | `:convex.server/db`               | Database (see `:module/cvm`)                               | Default temp instance created automatically |
   | `:convex.server/n-peer`           | Maximum number of other peers this one should broadcast to | `20`                                        |
   | `:convex.server/persist-at-stop?` | True if peer data should be persisted in DB when stopped   | `true`                                      |
   | `:convex.server/port`             | Port                                                       | `18888`                                     |
   | `:convex.server/url`              | URL of this peer (string) that will be registered on chain | /                                           |

   The URL, if given, is stored on-chain so that other peers can use it to broadcast beliefs and state updates.
   It is typically different from `:convex.server/bind` and `:convex.server/port`. For instance, `convex.world`
   has registered URL `convex.world:18888` in on-chain peer data, it is publicly accessible to all peers which
   wants to broadcast data to it.

   A peer needs initial state optionally specified in `:convex.server/state` which is a vector. Either:

   | Item 0     | Item 1     | Does                                          |
   |------------|------------|-----------------------------------------------|
   | `:genesis` | /          | Creates new genesis state from scratch        |
   | `:db`      | /          | Restores state from `:convex.server/db`       |
   | `:sync`    | Option map | Performs peer syncing (see below)             |
   | `:use`     | State cell | Advanced. Uses given `convex.core.State` cell |

   Peer syncing retrieves state from the given peer and connection will automatically be formed to that
   other peer at [[start]], forming a network. The option map may specify:

   | Key                   | Value                      | Default         |
   |-----------------------|----------------------------|-----------------|
   | `:convex.server/host` | Address of the remote peer | `\"localhost\"` |
   | `:convex.server/port` | Port of the remote peer    | `18888`         |"

  ;; Following options are undocumented for the time being:
  ;;
  ;;   :convex.server/hook
  ;;   :convex.server/poll-delay


  (^Server [keypair]

   (create keypair
           nil))


  (^Server [keypair option+]

   (Server/create (let [h     (HashMap. 10)
                        state (:convex.server/state option+)]
                    (case (or (first state)
                              :genesis)
                      :genesis nil
                      :db      (.put h
                                     Keywords/RESTORE
                                     true)
                      :sync    (let [{:convex.server/keys      [host
                                                                port]
                                      :convex.server.sync/keys [timeout]} (second state)]
                                 (.put h
                                       Keywords/SOURCE
                                       (InetSocketAddress. (or ^String host
                                                               "localhost")
                                                           (long (or port
                                                                     Server/DEFAULT_PORT))))
                                 (when timeout
                                   (.put h
                                         Keywords/TIMEOUT
                                         timeout)))
                      :use     (.put h
                                     Keywords/STATE
                                     (second state)))
                    (some->> (:convex.server/bind option+)
                             (.put h
                                   Keywords/BIND_ADDRESS))
                    (some->> (:convex.server/controller option+)
                             (.put h
                                   Keywords/CONTROLLER))
                    (some->> (:convex.server/db option+)
                             (.put h
                                   Keywords/STORE))
                    (when-some [hook (:convex.server/hook option+)]
                      (.put h
                            Keywords/EVENT_HOOK
                            (reify IServerEvent
                              (onServerChange [_this event]
                                (hook event)))))
                    (.put h
                          Keywords/KEYPAIR
                          keypair)
                    (some->> (:convex.server/n-peer option+)
                             (.put h
                                   Keywords/OUTGOING_CONNECTIONS))
                    (.put h
                          Keywords/PERSIST
                          (not (= (:convex.server/persist-at-stop? option+)
                                  false)))
                    (some->> (:convex.server/poll-delay option+)
                             (.put h
                                   Keywords/POLL_DELAY))
                    (.put h
                          Keywords/PORT
                          (if-some [port (:convex.server/port option+)]
                            (if (identical? port
                                            :random)
                              nil
                              port)
                            Server/DEFAULT_PORT))
                    (let [url (:convex.server/url option+)]
                      (.put h
                            Keywords/AUTO_MANAGE
                            (boolean url))
                      (when url
                        (.put h
                              Keywords/URL
                              url)))
                    h))))


;;;;;;;;;; Lifecycle


(defn persist

  "Persists peer data at the root of the server's Etch instance.

   Persisted data can be recovered when creating a server with the same Etch instance (see `:convex.server/state`
   option in [[create]]).

   Done automatically at [[stop]] is `:convex.server/persist-at-stop?` as set to `true` at [[create]].

   However, the database is not flushed. See `convex.db/flush` from `:module/cvm`.
  
   Returns the `server`."

  ^Server

  [^Server server]

  (.persistPeerData server)
  server)



(defn start

  "Starts `server`.

   See [[create]] first.

   If peer syncing was configured in [[create]], also connects to remote peer.
  
   Returns the `server`."

  ^Server

  [^Server server]

  (.launch server)
  server)



(defn stop

  "Stops `server`.
  
   Previously started with [[start]].
  
   Does not close the Etch instance optionally provided when starting."

  [^Server server]

  (.close server)
  nil)


;;;;;;;;;; Informations


(defn belief

  "Returns the current belief of `server`."

  ^Belief

  [^Server server]

  (.getBelief server))



(defn controller

  "Returns the controller associated with `server`.
  
   It was either explicitly specified in [[create]] or retrieved from the state."

  ^Address

  [^Server server]

  (.getPeerController server))



(defn db

  "Returns the Etch instance used by the `server`."

  ^AStore

  [^Server server]

  (.getStore server))



(defn host

  "Returns bind address used by the `server` as a string."

  [^Server server]

  (.getHostString (.getHostAddress server)))



(defn peer

  "Returns the peer object wrapped by the server.
   
   For advanced users only."

  ^Peer

  [^Server server]

  (.getPeer server))



(defn port

  "Returns the port used by the `server`."

  [^Server server]

  (.getPort server))



(defn state

  "Returns the consensus state held by the `server`."

  ^State

  [^Server server]

  (.getConsensusState (.getPeer server)))
