(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (convex.core Result)
           (convex.core.crypto AKeyPair)
           (convex.core.data ACell
                             AVector
                             SignedData)
           (convex.core.data.prim CVMLong)
           (convex.core.lang Symbols)
           (convex.core.store Stores)
           (convex.core.transactions ATransaction)
           (convex.peer Server)
           (java.net InetSocketAddress)
           (java.util.concurrent CompletableFuture)
           (java.util.function Function))
  (:refer-clojure :exclude [resolve
                            sequence])
  (:require [convex.sign   :as $.sign]))



(set! *warn-on-reflection*
      true)


;;;;;;;;;; Lifecycle


(defn close

  ""

  [^Convex connection]

  (.close connection)
  nil)



(defn connect

  ""


  (^Convex []

   (connect nil))


  (^Convex [option+]

   (Convex/connect (InetSocketAddress. (or ^String (:convex.server/host option+)
                                                   "localhost")
                                       (long (or (:convex.server/port option+)
                                                 Server/DEFAULT_PORT)))
                   nil
                   nil
                   (or (:convex.client/db option+)
                       (Stores/current)))))



(defn connected?

  ""

  [^Convex client]

  (.isConnected client))


;;;;;;;;;; Networking - Performed directly by the client


(defn peer-status

  ""

  ^CompletableFuture

  [^Convex client]

  (.requestStatus client))



(defn resolve

  ""


  (^CompletableFuture [^Convex client hash]

   (.acquire client
             hash))


  (^CompletableFuture [^Convex client hash store]

   (.acquire client
             hash
             store)))



(defn query

  ""

  ^CompletableFuture

  [^Convex client address cell]

  (.query client
          cell
          address))



(defn state

  ""

  ^CompletableFuture

  [^Convex client]

  (.acquireState client))



(defn transact

  ""


  (^CompletableFuture [^Convex client ^SignedData signed-transaction]

   (.transact client
              signed-transaction))


  (^CompletableFuture [client ^AKeyPair key-pair ^ATransaction transaction]

   (transact client
             ($.sign/signed key-pair
                            transaction))))


;;;;;;;;;; Results


(defn error-code

  ""

  ^ACell

  [^Result result]

  (.getErrorCode result))



(defn result

  ""

  ^ACell

  [^Result result]

  (.getValue result))



(defn trace

  ""

  ^AVector

  [^Result result]

  (.getTrace result))


;;;;;;;;;; Networking - Higher-level


(defn sequence

  ""

  [^Convex client address]

  (.thenApply (query client
                      address
                      Symbols/STAR_SEQUENCE)
               (reify Function

                 (apply [_this res]
                   (if-some [ec (error-code res)]
                     (throw (ex-info "Unable to fetch next sequence"
                                     {:convex.cell/address address
                                      :convex.error/code   ec
                                      :convex.error/trace  (trace res)}))
                     (inc (.longValue ^CVMLong (result res))))))))
