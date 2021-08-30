(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (convex.core.data SignedData)
           (convex.core.lang Symbols)
           (convex.peer Server)
           (java.net InetSocketAddress))
  (:refer-clojure :exclude [resolve
                            sequence])
  (:require [convex.cvm.db :as $.cvm.db]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Lifecycle


(defn close

  ""

  [^Convex connection]

  (.close connection))



(defn connect

  ""

  [option+]

  (Convex/connect (InetSocketAddress. (or ^String (:convex.server/host option+)
                                                  "localhost")
                                      (long (or (:convex.server/port option+)
                                                Server/DEFAULT_PORT)))
                  nil
                  nil
                  (or (:convex.client/db option+)
                      ($.cvm.db/local))))



(defn connected?

  ""

  [^Convex client]

  (.isConnected client))


;;;;;;;;;; Networking - Performed directly by the client


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


;;;;;;;;;; Networking - Higher-level


(defn balance

  ""

  [^Convex client address]

  (query client
         address
         Symbols/STAR_BALANCE))
            


(defn sequence

  ""

  [^Convex client address]

  (query client
         address
         Symbols/STAR_SEQUENCE))
