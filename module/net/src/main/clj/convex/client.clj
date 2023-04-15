(ns convex.client

  "Interacting with a peer server via the binary protocol.

   After creating a client with [[connect]], main interactions are [[query]] and [[transact]].

   All IO functions return a future which ultimately resolves to a result received from the peer.
   Information from result can be extracted using:

   - [[result->error-code]]
   - [[result->trace]]
   - [[result->value]]
  
   Clients need an access to Etch. See `convex.db` from `:module/cvm`."

  {:author "Adam Helinski"}

  (:import (convex.api Convex
                       ConvexRemote)
           (convex.core Result)
           (convex.core.crypto AKeyPair)
           (convex.core.data ACell
                             AVector
                             SignedData)
           (convex.core.lang Symbols)
           (convex.core.transactions ATransaction)
           (convex.peer Server)
           (java.net InetSocketAddress)
           (java.util.concurrent CompletableFuture)
           (java.util.function Function))
  (:refer-clojure :exclude [resolve])
  (:require [convex.db       :as $.db]
            [convex.clj      :as $.clj]
            [convex.key-pair :as $.key-pair]
            [convex.server   :as $.server]))


(set! *warn-on-reflection*
      true)


(declare result->error-code
         result->value)


;;;;;;;;;; Lifecycle


(defn close

  "Closes the given `client`.
  
   See [[connect]]."

  [^Convex connection]

  (.close connection)
  nil)



(defn connect

  "Connects to a peer server as a client using the binary protocol.

   Will use the Etch instance found with `convex.db/current`. It important keeping the client
   on a thread that has always access to the very same instance.
  
   A map of options may be provided:

   | Key                   | Value           | Default       |
   |-----------------------|-----------------|---------------|
   | `:convex.server/host` | Peer IP address | \"localhost\" |
   | `:convex.server/port` | Peer port       | 18888         |"


  (^Convex []

   (connect nil))


  (^Convex [option+]

   (Convex/connect (InetSocketAddress. (or ^String (:convex.server/host option+)
                                                   "localhost")
                                       (long (or (:convex.server/port option+)
                                                 Server/DEFAULT_PORT)))
                   nil
                   nil
                   ($.db/current))))



(defn connect-local

  "Connects to an in-process peer server.

   If an application embeds a peer server, using a \"local\" client for interacting with it
   will be a lot more efficient.

   It is important the client is always on a thread that has the same store being returned on
   `convex.db/current` (from `:module/cvm`) as the store used by the `server`.
  
   See [[convex.server]]."

  ^Convex

  [^Server server]

  (Convex/connect server
                  nil
                  nil))



(defn connected?

  "Returns true if the given `client` is still connected.
   
   Attention. Currently, does not detect severed connections (eg. server shutting down).
  
   See [[close]]."

  [^Convex client]

  (.isConnected client))



(defn endpoint

  "Given a remote `client` (i.e. not a client from [[connect-local]]), returns a map:

   | Key                   | Value                                            |
   |-----------------------|--------------------------------------------------|
   | `:convex.server/host` | Hostname of the peer this client is connected to |
   | `:convex.server/port` | Port of the peer this client is connected to     |"

  [^ConvexRemote client]

  ($.server/-socket-address->map (.getRemoteAddress client)))


;;;;;;;;;; Networking - Performed directly by the client


(defn peer-status

  "Returns a future resolving to the status of the connected peer.

   See [[convex.server/status]] for the definition of a status."

  ^CompletableFuture

  [^Convex client]

  (.thenApply (.requestStatus client)
              (reify Function
                (apply [_this result]
                  (when-not (result->error-code result)
                    ($.server/-status->map (result->value result)))))))



(defn query

  "Performs a query, `cell` representing code to execute.
  
   Queries are a dry run: executed only by the peer, without consensus, and any state change is discarded.
   They do not incur fees.
  
   Returns a future resolving to a result."

  ^CompletableFuture

  [^Convex client address cell]

  (.query client
          cell
          address))



(defn resolve

  "Sends the given `hash` to the peer to resolve it as a cell using its Etch instance.

   See `convex.db` from `:module/cvm` for more about hashes and values in the context of Etch.
  
   Returns a future resolving to a result."


  ^CompletableFuture
  
  [^Convex client hash]

  (.acquire client
            hash))



(defn state

  "Requests the currrent network state from the peer.

   Returns a future resolving to a result."

  ^CompletableFuture

  [^Convex client]

  (.acquireState client))



(defn transact

  "Performs a transaction.

   3 types of transactions exists in [`module/cvm`](../../cvm/doc/API.md):
 
   - `convex.cell/call` for an actor call
   - `convex.cell/invoke` for executing code
   - `convex.cell/transfer` for executing a transfer of Convex Coins

   Transaction must be either pre-signed beforehand or a key pair must be provided to sign it.
   See the [[convex.key-pair]] namespace to learn more about key pairs.

   It is important that transactions are created for the account matching the key pair and that the right
   sequence ID is used. See [[sequence-id]]."


  (^CompletableFuture [^Convex client ^SignedData signed-transaction]

   (.transact client
              signed-transaction))


  (^CompletableFuture [client ^AKeyPair key-pair ^ATransaction transaction]

   (transact client
             ($.key-pair/sign key-pair
                              transaction))))


;;;;;;;;;; Results


(defn result->error-code

  "Given a result de-referenced from a future, returns the error code.
  
   Could be any cell but typically a CVX keyword.
  
   Returns nil if the result is not an error."

  ^ACell

  [^Result result]

  (.getErrorCode result))



(defn result->trace

  "Given a result de-referenced from a future, returns the stacktrace.
   
   A CVX vector of strings.
  
   Returns nil if the result is not an error."

  ^AVector

  [^Result result]

  (.getTrace result))



(defn result->value 

  "Given a result de-referenced from a future, returns its value.

   Could be any cell.

   In case of error, this will be the error message (often a CVX string but can be any value)."

  ^ACell

  [^Result result]

  (.getValue result))


;;;;;;;;;; Networking - Higher-level


(defn sequence-id

  "Retrieves the next sequence ID required for a transaction.

   Uses [[query]].
  
   Each account has a sequence ID, a number being incremented on each successful transaction to prevent replay
   attacks. Providing a transaction (eg. `convex.cell/invoke` from `:module/cvm`) with a wrong sequence ID
   number will fail."

  [^Convex client address]

  (.thenApply (query client
                      address
                      Symbols/STAR_SEQUENCE)
               (reify Function
                 (apply [_this result]
                   (if-some [ec (result->error-code result)]
                     (throw (ex-info "Unable to fetch next sequence ID"
                                     {:convex.cell/address address
                                      :convex.error/code   ec
                                      :convex.error/trace  (result->trace result)}))
                     ($.clj/long (result->value result)))))))
