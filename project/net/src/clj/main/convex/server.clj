(ns convex.server

  ""

  {:author "Adam Helinski"}

  (:import (convex.core Peer)
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

  ""


  (^Server [keypair]

   (create keypair
           nil))


  (^Server [keypair option+]

   (Server/create (let [h     (HashMap. 10)
                        state (:convex.server/state option+)]
                    (case (or (first state)
                              :genesis)
                      :genesis nil
                      :store   (.put h
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
                    (some->> (:convex.server/host option+)
                             (.put h
                                   Keywords/HOST))
                    (.put h
                          Keywords/KEYPAIR
                          keypair)
                    (some->> (:convex.server/n-peer option+)
                             (.put h
                                   Keywords/OUTGOING_CONNECTIONS))
                    (some->> (:convex.server/persist-at-stop? option+)
                             (.put h
                                   Keywords/PERSIST))
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

                    (some->> (:convex.server/url option+)
                             (.put h
                                   Keywords/URL))
                    h))))


;;;;;;;;;; Lifecycle


(defn start

  ""

  ^Server

  [^Server server]

  (.launch server)
  server)



(defn stop

  ""

  [^Server server]

  (.close server)
  nil)


;;;;;;;;;; Informations


(defn controller

  ""

  ^Address

  [^Server server]

  (.getPeerController server))



(defn db

  ""

  ^AStore

  [^Server server]

  (.getStore server))



(defn host

  ""

  [^Server server]

  (.getHostString (.getHostAddress server)))



(defn peer

  ""

  ^Peer

  [^Server server]

  (.getPeer server))



(defn port

  ""

  [^Server server]

  (.getPort server))
