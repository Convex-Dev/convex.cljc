(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (convex.core.crypto AKeyPair)
           (convex.core.data ACell
                             Address
                             SignedData)
           (convex.core.transactions ATransaction)
           (convex.peer Server)
           (java.net InetSocketAddress))
  (:refer-clojure :exclude [resolve
                            sequence]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Lifecycle


(defn close

  ""

  [^Convex connection]

  (.close connection))



(defn connect

  ""


  ([]

   (connect "convex.world"
            43579))


  ([host]

   (connect host
            Server/DEFAULT_PORT))


  ([^String host ^long port]

   (Convex/connect (InetSocketAddress. host
                                       port))))


;;;;;;;;;; Networking


(defn peer-status

  ""

  [^Convex client]

  (.requestStatus client))



(defn resolve

  ""


  ([^Convex client hash]

   (.acquire client
             hash))


  ([^Convex client hash store]

   (.acquire client
             hash
             store)))



(defn query

  ""

  [^Convex client cell]

  (.query client
          cell))



(defn query-as

  ""

  [^Convex client address cell]

  (.query client
          cell
          address))



(defn state

  ""

  [^Convex client]

  (.acquireState client))



(defn transact

  ""

  [^Convex client ^SignedData signed-transaction]

  (.transact client
             signed-transaction))
