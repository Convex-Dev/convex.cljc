(ns convex.server

  ""

  {:author "Adam Helinski"}

  (:import (convex.core Belief
                        Peer)
           (convex.core.data Keywords)
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

   (Server/create (let [h (HashMap. 10)]
                    (when-some [hook (:convex.server/hook option+)]
                      (.put h
                            Keywords/EVENT_HOOK
                            (reify IServerEvent
                              (onServerChange [_this event]
                                (hook event)))))
                    (.put h
                          Keywords/KEYPAIR
                          keypair)
                    (.put h
                          Keywords/PERSIST
                          true)
                    (.put h
                          Keywords/PORT
                          (if-some [port (:convex.server/port option+)]
                            (if (identical? port
                                            :random)
                              nil
                              port)
                            Server/DEFAULT_PORT))
                    (.put h
                          Keywords/RESTORE
                          (boolean (:convex.server/restore? option+)))
                    (when-some [host (:convex.server.sync/host option+)]
                      (.put h
                            Keywords/SOURCE
                            (InetSocketAddress. ^String host
                                                (long (or (:convex.server.sync/port option+)
                                                          Server/DEFAULT_PORT)))))
                    (when-some [state (:convex.server/state option+)]
                      (.put h
                            Keywords/STATE
                            state))
                    (when-some [store (:convex.server/db option+)]
                      (.put h
                            Keywords/STORE
                            store))
                    (when-some [sync-timeout (:convex.server.sync/timeout option+)]
                      (.put h
                            Keywords/TIMEOUT
                            sync-timeout))
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

  ^Server

  [^Server server]

  (.close server)
  server)


;;;;;;;;;; Informations


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



(defn store

  ""

  ^AStore

  [^Server server]

  (.getStore server))
