# Table of contents
-  [`convex.client`](#convex.client)  - Interacting with a peer server via the binary protocol.
    -  [`close`](#convex.client/close) - Closes the given <code>client</code>.
    -  [`connect`](#convex.client/connect) - Opens a new client connection to a peer server using the binary protocol.
    -  [`connect-local`](#convex.client/connect-local) - Like [[connect]] but the returned client is optimized to talk to a peer <code>server</code> running in the same process.
    -  [`connected?`](#convex.client/connected?) - Returns true if the given <code>client</code> is still connected.
    -  [`peer-status`](#convex.client/peer-status) - Advanced feature.
    -  [`query`](#convex.client/query) - Performs a query, <code>cell</code> representing code to execute.
    -  [`resolve`](#convex.client/resolve) - Sends the given <code>hash</code> to the peer to resolve it as a cell using its Etch instance.
    -  [`result->error-code`](#convex.client/result->error-code) - Given a result dereferenced from a future, returns the error code (a cell, typically a CVX keyword).
    -  [`result->trace`](#convex.client/result->trace) - Given a result dereferenced from a future, returns the stacktrace (a CVX vector of strings).
    -  [`result->value`](#convex.client/result->value) - Given a result dereferenced from a future, returns its value (a cell).
    -  [`sequence-id`](#convex.client/sequence-id) - Uses [[query]] to retrieve the next sequence ID required for a transaction.
    -  [`state`](#convex.client/state) - Requests the currrent network state from the peer.
    -  [`transact`](#convex.client/transact) - Performs a transaction which is one of the following (from <code>:module/cvm</code>): - <code>convex.cell/call</code> for an actor call - <code>convex.cell/invoke</code> for executing code - <code>convex.cell/transfer</code> for executing a transfer of Convex Coins Transaction must be either pre-signed beforehand or a key pair must be provided to sign it.
-  [`convex.key-pair`](#convex.key-pair)  - Signing cells using public key cryptography, most notably transactions as required prior to submission.
    -  [`account-key`](#convex.key-pair/account-key) - Returns the account key of the given <code>key-pair</code>.
    -  [`ed25519`](#convex.key-pair/ed25519) - Creates an Ed25519 key pair.
    -  [`hex-string`](#convex.key-pair/hex-string) - Returns the public key of the given <code>key-pair</code> as a hex-string (64-char string where each pair of chars represents a byte in hexadecimal).
    -  [`key-private`](#convex.key-pair/key-private) - Returns the <code>java.security.PrivateKey</code> of the given <code>key-pair</code>.
    -  [`key-public`](#convex.key-pair/key-public) - Returns the <code>java.security.PublicKey</code> of the given <code>key-pair</code>.
    -  [`seed`](#convex.key-pair/seed) - Returns the seed of the given <code>key-pair</code>.
    -  [`sign`](#convex.key-pair/sign) - Returns the given <code>cell</code> as data signed by <code>key-pair</code>.
    -  [`sign-hash`](#convex.key-pair/sign-hash) - Signs the given <code>hash</code> with the given <code>key-pair</code>.
    -  [`signed->account-key`](#convex.key-pair/signed->account-key) - Given signed data, returns the [[account-key]] of the signer.
    -  [`signed->cell`](#convex.key-pair/signed->cell) - Given signed data, returns the cell that was signed.
    -  [`signed->signature`](#convex.key-pair/signed->signature) - Given signed data, returns the signature as a blob cell.
    -  [`verify`](#convex.key-pair/verify) - Returns true if the given <code>cell</code> has indeed been signed by the given [[account-key]].
    -  [`verify-hash`](#convex.key-pair/verify-hash) - Verifies that the given <code>signature</code> is indeed the given <code>hash</code> signed by the given [[account-key]].
-  [`convex.pfx`](#convex.pfx)  - Creating and managing a key store for storing key pairs in a file.
    -  [`create`](#convex.pfx/create) - Creates a new key store in the file under <code>path</code>.
    -  [`key-pair-get`](#convex.pfx/key-pair-get) - Retrieves a key pair from the given <code>key-store</code>.
    -  [`key-pair-set`](#convex.pfx/key-pair-set) - Adds the given <code>key-pair</code> to the <code>key-store</code>, protected by a mandatory <code>passphrase</code>.
    -  [`load`](#convex.pfx/load) - Loads a key store from the file under <code>path</code>.
    -  [`save`](#convex.pfx/save) - Saves the given <code>key-store</code> to the file under <code>path</code>.
-  [`convex.server`](#convex.server)  - Creating a peer which can either: - Run alone for dev and test - Run locally, synced with other local peers - Run locally but synced with the test network on <code>convex.world</code> See README.
    -  [`controller`](#convex.server/controller) - Returns the controller associated with <code>server</code>.
    -  [`create`](#convex.server/create) - Returns a new server that can be started using [[start]] when required.
    -  [`db`](#convex.server/db) - Returns the Etch instance used by the <code>server</code>.
    -  [`host`](#convex.server/host) - Returns bind address used by the <code>server</code> as a string.
    -  [`peer`](#convex.server/peer) - Advanced feature.
    -  [`persist`](#convex.server/persist) - Persists peer data at the root of the server's Etch instance Persisted data can be recovered when creating a server with the same Etch instance (see <code>:convex.server/state</code> option in [[create]]).
    -  [`port`](#convex.server/port) - Returns the port used by the <code>server</code>.
    -  [`start`](#convex.server/start) - Starts <code>server</code> created in [[create]].
    -  [`stop`](#convex.server/stop) - Stops <code>server</code> previously started with <code>start</code>.

-----
# <a name="convex.client">convex.client</a>


Interacting with a peer server via the binary protocol.

   After creating a client with [`connect`](#convex.client/connect), main interactions are [`query`](#convex.client/query) and [`transact`](#convex.client/transact).

   All IO functions return a future which ultimately resolves to a result received from the peer.
   Information from result can be extracted using:

   - [`result->error-code`](#convex.client/result->error-code)
   - [`result->trace`](#convex.client/result->trace)
   - [`result->value`](#convex.client/result->value)
  
   Clients need an access to Etch. See `convex.db` from `:module/cvm`.




## <a name="convex.client/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L43-L52) `close`</a>
``` clojure

(close connection)
```


Closes the given `client`.
  
   See [`connect`](#convex.client/connect).

## <a name="convex.client/connect">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L56-L84) `connect`</a>
``` clojure

(connect)
(connect option+)
```


Opens a new client connection to a peer server using the binary protocol.

   Will use the Etch instance found with `convex.db/current`. It important keeping the client
   on a thread that has always access to the very same instance.
  
   A map of options may be provided:

   | Key                   | Value           | Default       |
   |-----------------------|-----------------|---------------|
   | `:convex.server/host` | Peer IP address | "localhost" |
   | `:convex.server/port` | Peer port       | 18888         |

## <a name="convex.client/connect-local">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L88-L104) `connect-local`</a>
``` clojure

(connect-local server)
```


Like [`connect`](#convex.client/connect) but the returned client is optimized to talk to a peer `server` running
   in the same process.

   It is important the client is always on a thread that has the same store being returned on
   `convex.db/current` (from `:module/cvm`) as the store used by the `server`.
  
   See [`convex.server`](#convex.server).

## <a name="convex.client/connected?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L108-L118) `connected?`</a>
``` clojure

(connected? client)
```


Returns true if the given `client` is still connected.
   
   Attention. Currently, does not detect severed connections (eg. server shutting down).
  
   See [`close`](#convex.client/close).

## <a name="convex.client/peer-status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L124-L134) `peer-status`</a>
``` clojure

(peer-status client)
```


Advanced feature. The peer status is a vector of blobs which are hashes to data about the peer.
   For instance, blob 4 is the hash of the state. That is how [`state`](#convex.client/state) works, retrieving the hash
   from the peer status and then using [`resolve`](#convex.client/resolve).

## <a name="convex.client/query">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L156-L171) `query`</a>
``` clojure

(query client address cell)
```


Performs a query, `cell` representing code to execute.
  
   Queries are a dry run: executed only by the peer, without consensus, and any state change is discarded.
   They do not incur fees.
  
   Returns a future resolving to a result.

## <a name="convex.client/resolve">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L138-L152) `resolve`</a>
``` clojure

(resolve client hash)
```


Sends the given `hash` to the peer to resolve it as a cell using its Etch instance.

   See `convex.db` from `:module/cvm` for more about hashes and values in the context of Etch.
  
   Returns a future resolving to a result.

## <a name="convex.client/result->error-code">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L220-L230) `result->error-code`</a>
``` clojure

(result->error-code result)
```


Given a result dereferenced from a future, returns the error code (a cell, typically a CVX keyword).
  
   Returns nil if no error occured.

## <a name="convex.client/result->trace">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L234-L244) `result->trace`</a>
``` clojure

(result->trace result)
```


Given a result dereferenced from a future, returns the stacktrace (a CVX vector of strings).
  
   Returns nil if no error occured.

## <a name="convex.client/result->value">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L248-L258) `result->value`</a>
``` clojure

(result->value result)
```


Given a result dereferenced from a future, returns its value (a cell).

   In case of error, this will be the error message (often a CVX string but can be any value).

## <a name="convex.client/sequence-id">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L264-L285) `sequence-id`</a>
``` clojure

(sequence-id client address)
```


Uses [`query`](#convex.client/query) to retrieve the next sequence ID required for a transaction.
  
   Eacht account has a sequence ID, a number being incremented on each successful transaction to prevent replay
   attacks. Providing a transaction (eg. `convex.cell/invoke` from `:module/cvm`) with a wrong sequence ID
   number will fail.

## <a name="convex.client/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L175-L185) `state`</a>
``` clojure

(state client)
```


Requests the currrent network state from the peer.

   Returns a future resolving to a result.

## <a name="convex.client/transact">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L189-L214) `transact`</a>
``` clojure

(transact client signed-transaction)
(transact client key-pair transaction)
```


Performs a transaction which is one of the following (from `:module/cvm`):

   - `convex.cell/call` for an actor call
   - `convex.cell/invoke` for executing code
   - `convex.cell/transfer` for executing a transfer of Convex Coins

   Transaction must be either pre-signed beforehand or a key pair must be provided to sign it.
   See the [`convex.key-pair`](#convex.key-pair) namespace to learn more about key pairs.

   It is important that transactions are created for the account matching the key pair and that the right
   sequence ID is used. See [`sequence-id`](#convex.client/sequence-id).

-----
# <a name="convex.key-pair">convex.key-pair</a>


Signing cells using public key cryptography, most notably transactions as required prior to submission.

   More precisely, is signed the hash of the encoding of the cell, producing a signed data cell.
  
   Uses Ed25519.




## <a name="convex.key-pair/account-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L60-L71) `account-key`</a>
``` clojure

(account-key key-pair)
```


Returns the account key of the given `key-pair`.

   An account key is a specialized cell behaving like a blob and representing the public key
   of an account.

## <a name="convex.key-pair/ed25519">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L31-L54) `ed25519`</a>
``` clojure

(ed25519)
(ed25519 seed)
(ed25519 key-public key-private)
```


Creates an Ed25519 key pair.

   It is generated from a [`seed`](#convex.key-pair/seed), a 32-byte blob. If not given, one is generated randomly.

   Alternatively, a [`key-public`](#convex.key-pair/key-public) and a [`key-private`](#convex.key-pair/key-private) retrieved from an existing key pair can
   be provided.

## <a name="convex.key-pair/hex-string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L75-L84) `hex-string`</a>
``` clojure

(hex-string key-pair)
```


Returns the public key of the given `key-pair` as a hex-string (64-char string where each pair of 
   chars represents a byte in hexadecimal).

## <a name="convex.key-pair/key-private">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L88-L96) `key-private`</a>
``` clojure

(key-private key-pair)
```


Returns the `java.security.PrivateKey` of the given `key-pair`.

## <a name="convex.key-pair/key-public">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L100-L108) `key-public`</a>
``` clojure

(key-public key-pair)
```


Returns the `java.security.PublicKey` of the given `key-pair`.

## <a name="convex.key-pair/seed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L112-L122) `seed`</a>
``` clojure

(seed key-pair)
```


Returns the seed of the given `key-pair`.

   Attention, this is very sensitive information since it allows rebuilding the key-pair using [`ed25519`](#convex.key-pair/ed25519).

## <a name="convex.key-pair/sign">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L128-L143) `sign`</a>
``` clojure

(sign key-pair cell)
```


Returns the given `cell` as data signed by `key-pair`. That value is a cell itself
   and can be stored in Etch if required (see the `convex.db` namespace from `:module/cvm`).

   `signed->...` functions allows for extracting information from signed data.

   Most useful for signing transactions.
   See [`convex.client/transact`](#convex.client/transact).

## <a name="convex.key-pair/sign-hash">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L208-L219) `sign-hash`</a>
``` clojure

(sign-hash key-pair hash)
```


Signs the given `hash` with the given `key-pair`.
   Returns the signature as a blob.

   See `convex.cell/hash` from `:module/cvm`.

## <a name="convex.key-pair/signed->account-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L147-L157) `signed->account-key`</a>
``` clojure

(signed->account-key signed)
```


Given signed data, returns the [`account-key`](#convex.key-pair/account-key) of the signer.
  
   See [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/signed->cell">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L161-L173) `signed->cell`</a>
``` clojure

(signed->cell signed)
```


Given signed data, returns the cell that was signed.

   See [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/signed->signature">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L176-L188) `signed->signature`</a>
``` clojure

(signed->signature signed)
```


Given signed data, returns the signature as a blob cell.

   See [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/verify">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L192-L202) `verify`</a>
``` clojure

(verify account-key signature cell)
```


Returns true if the given `cell` has indeed been signed by the given [`account-key`](#convex.key-pair/account-key).

   `signature` is the signature to verify as a blob cell.

## <a name="convex.key-pair/verify-hash">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L223-L234) `verify-hash`</a>
``` clojure

(verify-hash account-key signature hash)
```


Verifies that the given `signature` is indeed the given `hash` signed by the given
   [`account-key`](#convex.key-pair/account-key).
  
   See [`sign-hash`](#convex.key-pair/sign-hash).

-----
# <a name="convex.pfx">convex.pfx</a>


Creating and managing a key store for storing key pairs in a file.
  
   See [`convex.key-pair`](#convex.key-pair) about key pairs.




## <a name="convex.pfx/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L24-L44) `create`</a>
``` clojure

(create path)
(create path passphrase)
```


Creates a new key store in the file under `path`.
  
   An optional passphrase protecting the store may be provided.

## <a name="convex.pfx/key-pair-get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L92-L106) `key-pair-get`</a>
``` clojure

(key-pair-get key-store alias-or-account-key passphrase)
```


Retrieves a key pair from the given `key-store`.

   See [`key-pair-set`](#convex.pfx/key-pair-set).

## <a name="convex.pfx/key-pair-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L110-L132) `key-pair-set`</a>
``` clojure

(key-pair-set key-store key-pair passphrase)
(key-pair-set key-store alias key-pair passphrase)
```


Adds the given `key-pair` to the `key-store`, protected by a mandatory `passphrase`.

   Public key is used as `alias` if none is provided.
  
   See [`key-pair-set`](#convex.pfx/key-pair-set).

## <a name="convex.pfx/load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L48-L64) `load`</a>
``` clojure

(load path)
(load path passphrase)
```


Loads a key store from the file under `path`.
  
   Passphrase must be provided if the store is protected by one.

## <a name="convex.pfx/save">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L68-L86) `save`</a>
``` clojure

(save key-store path)
(save key-store path passphrase)
```


Saves the given `key-store` to the file under `path`.
  
   An optional passphrase protecting the store may be provided.

-----
# <a name="convex.server">convex.server</a>


Creating a peer which can either:

   - Run alone for dev and test
   - Run locally, synced with other local peers
   - Run locally but synced with the test network on `convex.world`
  
   See README.




## <a name="convex.server/controller">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L209-L219) `controller`</a>
``` clojure

(controller server)
```


Returns the controller associated with `server`.
  
   It was either explicitly specified in [`create`](#convex.server/create) or retrieved from the state.

## <a name="convex.server/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L30-L149) `create`</a>
``` clojure

(create keypair)
(create keypair option+)
```


Returns a new server that can be started using [`start`](#convex.server/start) when required.

   A key pair is mandatory. See the [`convex.key-pair`](#convex.key-pair).

   An map of options may be provided:

   | Key                              | Value                                                      | Default                                     |
   |----------------------------------|------------------------------------------------------------|---------------------------------------------|
   | `:convex.server/bind`            | Bind address (string)                                      | `"localhost"`                             |
   | `:convex.server/state`           | See below                                                  | `[:genesis]`                                |
   | `:convex.server/controller`      | Controller account address                                 | Retrieved from state                        |
   | `:convex.server/db`              | Database (see `:module/cvm`)                               | Default temp instance created automatically |
   | `:convex.server/n-peer`          | Maximum number of other peers this one should broadcast to | `20`                                        |
   | `:convex.server/persist-at-stop? | True if peer data should be persisted in DB when stopped   | `true`                                      |
   | `:convex.server/port`            | Port                                                       | `18888`                                     |
   | `:convex.server/url              | URL of this peer (string) that will be registered on chain | /                                           |

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
   other peer at [`start`](#convex.server/start), forming a network. The option map may specify:

   | Key                   | Value                      | Default         |
   |-----------------------|----------------------------|-----------------|
   | `:convex.server/host` | Address of the remote peer | `"localhost"` |
   | `:convex.server/port` | Port of the remote peer    | `18888`         |

## <a name="convex.server/db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L223-L231) `db`</a>
``` clojure

(db server)
```


Returns the Etch instance used by the `server`.

## <a name="convex.server/host">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L235-L241) `host`</a>
``` clojure

(host server)
```


Returns bind address used by the `server` as a string.

## <a name="convex.server/peer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L245-L254) `peer`</a>
``` clojure

(peer server)
```


Advanced feature. Returns the peer object wrapped by the server. More precisely, the server
   provided network connectivity over this object.

## <a name="convex.server/persist">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L155-L173) `persist`</a>
``` clojure

(persist server)
```


Persists peer data at the root of the server's Etch instance

   Persisted data can be recovered when creating a server with the same Etch instance (see `:convex.server/state`
   option in [`create`](#convex.server/create)).

   Done automatically at [`stop`](#convex.server/stop) is `:convex.server/persist-at-stop?` as set to `true` at [`create`](#convex.server/create).

   However, the database is not flushed. See `convex.db/flush` from `:module/cvm`.
  
   Returns the `server`.

## <a name="convex.server/port">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L258-L264) `port`</a>
``` clojure

(port server)
```


Returns the port used by the `server`.

## <a name="convex.server/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L177-L190) `start`</a>
``` clojure

(start server)
```


Starts `server` created in [`create`](#convex.server/create).

   If peer syncing was configured in [`create`](#convex.server/create), also connects to remote peer.
  
   Returns the `server`.

## <a name="convex.server/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L194-L203) `stop`</a>
``` clojure

(stop server)
```


Stops `server` previously started with `start`.
  
   Does not close the Etch instance optionally provided when starting.
