(ns convex.server

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data Keywords)
           (convex.peer Server)
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

   (Server/create (doto (HashMap. 10)
                    (.put Keywords/KEYPAIR
                          keypair)
                    (.put Keywords/PERSIST
                          true)
                    (.put Keywords/PORT
                          (:convex.server/port option+))
                    (.put Keywords/RESTORE
                          (:convex.server/restore? option+))
                    (.put Keywords/SOURCE
                          (when-some [host (:convex.server.sync/host option+)]
                            (InetSocketAddress. ^String host
                                                (long (or (:convex.server.sync/port option+)
                                                          Server/DEFAULT_PORT)))))
                    (.put Keywords/STATE
                          (:convex.server/state option+))
                    (.put Keywords/STORE
                          (:convex.server/db option+))
                    (.put Keywords/TIMEOUT
                          (:convex.server.sync/timeout option+))))))


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
