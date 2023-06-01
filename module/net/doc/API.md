# Table of contents
-  [`convex.client`](#convex.client)  - Interacting with a peer server via the binary protocol.
    -  [`close`](#convex.client/close) - Closes the given <code>client</code>.
    -  [`connect`](#convex.client/connect) - Connects to a peer server as a client using the binary protocol.
    -  [`connect-local`](#convex.client/connect-local) - Connects to an in-process peer server.
    -  [`connected?`](#convex.client/connected?) - Returns true if the given <code>client</code> is still connected.
    -  [`endpoint`](#convex.client/endpoint) - Given a remote <code>client</code> (i.e.
    -  [`peer-status`](#convex.client/peer-status) - Returns a future resolving to the status of the connected peer.
    -  [`query`](#convex.client/query) - Performs a query, <code>cell</code> representing code to execute.
    -  [`resolve`](#convex.client/resolve) - Sends the given <code>hash</code> to the peer to resolve it as a cell using its Etch instance.
    -  [`result->error-code`](#convex.client/result->error-code) - Given a result de-referenced from a future, returns the error code.
    -  [`result->trace`](#convex.client/result->trace) - Given a result de-referenced from a future, returns the stacktrace.
    -  [`result->value`](#convex.client/result->value) - Given a result de-referenced from a future, returns its value.
    -  [`sequence-id`](#convex.client/sequence-id) - Retrieves the next sequence ID required for a transaction.
    -  [`state`](#convex.client/state) - Requests the currrent network state from the peer.
    -  [`transact`](#convex.client/transact) - Performs a transaction.
-  [`convex.key-pair`](#convex.key-pair)  - Signing cells using public key cryptography, most notably transactions.
    -  [`account-key`](#convex.key-pair/account-key) - Returns the account key of the given <code>key-pair</code>.
    -  [`ed25519`](#convex.key-pair/ed25519) - Creates an Ed25519 key pair.
    -  [`hex-string`](#convex.key-pair/hex-string) - Returns the public key of the given <code>key-pair</code> as a hex-string.
    -  [`key-pair?`](#convex.key-pair/key-pair?) - Returns <code>true</code> is <code>x</code> is a key pair.
    -  [`key-private`](#convex.key-pair/key-private) - Returns the <code>java.security.PrivateKey</code> of the given <code>key-pair</code>.
    -  [`key-public`](#convex.key-pair/key-public) - Returns the <code>java.security.PublicKey</code> of the given <code>key-pair</code>.
    -  [`seed`](#convex.key-pair/seed) - Returns the seed of the given <code>key-pair</code>.
    -  [`sign`](#convex.key-pair/sign) - Returns the given <code>cell</code> as data signed by <code>key-pair</code>.
    -  [`signed->account-key`](#convex.key-pair/signed->account-key) - Given signed data, returns the account key of the signer.
    -  [`signed->cell`](#convex.key-pair/signed->cell) - Given signed data, returns the cell that was signed.
    -  [`signed->signature`](#convex.key-pair/signed->signature) - Given signed data, returns the signature as a blob cell.
    -  [`verify`](#convex.key-pair/verify) - Returns true if the given <code>cell</code> has indeed been signed by the given [[account-key]].
-  [`convex.pfx`](#convex.pfx)  - Creating and managing a key store for storing key pairs in a file.
    -  [`alias+`](#convex.pfx/alias+) - Returns a sequence of aliases available in the given <code>key-store</code> (or <code>nil</code> if the store is empty).
    -  [`create`](#convex.pfx/create) - Creates a new key store in the file under <code>path</code>.
    -  [`key-pair-get`](#convex.pfx/key-pair-get) - Retrieves a key pair from the given <code>key-store</code>.
    -  [`key-pair-rm`](#convex.pfx/key-pair-rm) - Removes a key pair from the given <code>key-store</code> by alias.
    -  [`key-pair-set`](#convex.pfx/key-pair-set) - Adds the given <code>key-pair</code> to the <code>key-store</code>, protected by a mandatory <code>passphrase</code>.
    -  [`load`](#convex.pfx/load) - Loads a key store from the file under <code>path</code>.
    -  [`save`](#convex.pfx/save) - Saves the given <code>key-store</code> to the file under <code>path</code>.
-  [`convex.server`](#convex.server)  - Running a peer server.
    -  [`belief`](#convex.server/belief) - Returns the current belief of <code>server</code>.
    -  [`controller`](#convex.server/controller) - Returns the controller associated with <code>server</code>.
    -  [`create`](#convex.server/create) - Returns a new peer server.
    -  [`data`](#convex.server/data) - Returns a map cell with: | Key | Value | |--------------|-------------------------------------------------------------| | <code>:belief</code> | Current belief held by <code>server</code> | | <code>:genesis</code> | Genesis state | | <code>:history</code> | Ordering position for which results and states are avaiable | | <code>:position</code> | State index (ie.
    -  [`db`](#convex.server/db) - Returns the Etch instance used by the <code>server</code>.
    -  [`endpoint`](#convex.server/endpoint) - Given a <code>server</code>, returns a map: | Key | Value | |-----------------------|----------------------------------| | <code>:convex.server/host</code> | Hostname this server is bound to | | <code>:convex.server/port</code> | Port this server is listening to |.
    -  [`n-belief-received`](#convex.server/n-belief-received) - Returns the number of beliefs received by <code>server</code>.
    -  [`n-belief-sent`](#convex.server/n-belief-sent) - Returns the number of beliefs broadcasted by <code>server</code>.
    -  [`peer`](#convex.server/peer) - Returns the peer object wrapped by the <code>server</code>.
    -  [`persist`](#convex.server/persist) - Persists peer data at the root of the server's Etch instance.
    -  [`pubkey`](#convex.server/pubkey) - Returns the public key of this <code>server</code>'s peer.
    -  [`start`](#convex.server/start) - Starts <code>server</code>.
    -  [`state`](#convex.server/state) - Returns the consensus state held by the <code>server</code>.
    -  [`status`](#convex.server/status) - Returns the current status of <code>server</code>, a Map cell such as: | Key | Value | |-------------------------|----------------------------------| | <code>:hash.belief</code> | Hash of the current Belief | | <code>:hash.state+</code> | Hash of all the States | | <code>:hash.state.consensus</code> | Hash of the Consensus State | | <code>:hash.state.genesis</code> | Hash of the Genesis State | | <code>:n.block</code> | Number of blocks in the ordering | | <code>:point.consensus</code> | Current consensus point | | <code>:point.proposal</code> | Current proposal point | | <code>:pubkey</code> | Public key of that peer |.
    -  [`stop`](#convex.server/stop) - Stops <code>server</code>.

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




## <a name="convex.client/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L49-L58) `close`</a>
``` clojure

(close connection)
```


Closes the given `client`.
  
   See [`connect`](#convex.client/connect).

## <a name="convex.client/connect">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L62-L90) `connect`</a>
``` clojure

(connect)
(connect option+)
```


Connects to a peer server as a client using the binary protocol.

   Will use the Etch instance found with `convex.db/current`. It important keeping the client
   on a thread that has always access to the very same instance.
  
   A map of options may be provided:

   | Key                   | Value           | Default       |
   |-----------------------|-----------------|---------------|
   | `:convex.server/host` | Peer IP address | "localhost" |
   | `:convex.server/port` | Peer port       | 18888         |

## <a name="convex.client/connect-local">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L94-L112) `connect-local`</a>
``` clojure

(connect-local server)
```


Connects to an in-process peer server.

   If an application embeds a peer server, using a "local" client for interacting with it
   will be a lot more efficient.

   It is important the client is always on a thread that has the same store being returned on
   `convex.db/current` (from `:module/cvm`) as the store used by the `server`.
  
   See [`convex.server`](#convex.server).

## <a name="convex.client/connected?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L116-L126) `connected?`</a>
``` clojure

(connected? client)
```


Returns true if the given `client` is still connected.
   
   Attention. Currently, does not detect severed connections (eg. server shutting down).
  
   See [`close`](#convex.client/close).

## <a name="convex.client/endpoint">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L130-L141) `endpoint`</a>
``` clojure

(endpoint client)
```


Given a remote `client` (i.e. not a client from [`connect-local`](#convex.client/connect-local)), returns a map:

   | Key                   | Value                                            |
   |-----------------------|--------------------------------------------------|
   | `:convex.server/host` | Hostname of the peer this client is connected to |
   | `:convex.server/port` | Port of the peer this client is connected to     |

## <a name="convex.client/peer-status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L147-L161) `peer-status`</a>
``` clojure

(peer-status client)
```


Returns a future resolving to the status of the connected peer.

   See [`convex.server/status`](#convex.server/status) for the definition of a status.

## <a name="convex.client/query">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L165-L180) `query`</a>
``` clojure

(query client address cell)
```


Performs a query, `cell` representing code to execute.
  
   Queries are a dry run: executed only by the peer, without consensus, and any state change is discarded.
   They do not incur fees.
  
   Returns a future resolving to a result.

## <a name="convex.client/resolve">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L184-L198) `resolve`</a>
``` clojure

(resolve client hash)
```


Sends the given `hash` to the peer to resolve it as a cell using its Etch instance.

   See `convex.db` from `:module/cvm` for more about hashes and values in the context of Etch.
  
   Returns a future resolving to a result.

## <a name="convex.client/result->error-code">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L249-L261) `result->error-code`</a>
``` clojure

(result->error-code result)
```


Given a result de-referenced from a future, returns the error code.
  
   Could be any cell but typically a CVX keyword.
  
   Returns nil if the result is not an error.

## <a name="convex.client/result->trace">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L265-L277) `result->trace`</a>
``` clojure

(result->trace result)
```


Given a result de-referenced from a future, returns the stacktrace.
   
   A CVX vector of strings.
  
   Returns nil if the result is not an error.

## <a name="convex.client/result->value">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L281-L293) `result->value`</a>
``` clojure

(result->value result)
```


Given a result de-referenced from a future, returns its value.

   Could be any cell.

   In case of error, this will be the error message (often a CVX string but can be any value).

## <a name="convex.client/sequence-id">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L299-L321) `sequence-id`</a>
``` clojure

(sequence-id client address)
```


Retrieves the next sequence ID required for a transaction.

   Uses [`query`](#convex.client/query).
  
   Each account has a sequence ID, a number being incremented on each successful transaction to prevent replay
   attacks. Providing a transaction (eg. `convex.cell/invoke` from `:module/cvm`) with a wrong sequence ID
   number will fail.

## <a name="convex.client/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L202-L212) `state`</a>
``` clojure

(state client)
```


Requests the currrent network state from the peer.

   Returns a future resolving to a result.

## <a name="convex.client/transact">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/client.clj#L216-L243) `transact`</a>
``` clojure

(transact client signed-transaction)
(transact client key-pair transaction)
```


Performs a transaction.

   3 types of transactions exists in [`module/cvm`](../../cvm/doc/API.md):
 
   - `convex.cell/call` for an actor call
   - `convex.cell/invoke` for executing code
   - `convex.cell/transfer` for executing a transfer of Convex Coins

   Transaction must be either pre-signed beforehand or a key pair must be provided to sign it.
   See the [`convex.key-pair`](#convex.key-pair) namespace to learn more about key pairs.

   It is important that transactions are created for the account matching the key pair and that the right
   sequence ID is used. See [`sequence-id`](#convex.client/sequence-id).

-----
# <a name="convex.key-pair">convex.key-pair</a>


Signing cells using public key cryptography, most notably transactions.

   More precisely, is signed the hash of the encoding of the cell, producing a signed data cell.
  
   Uses [Ed25519](https://ed25519.cr.yp.to).
  
   ---
  
   By default, Convex uses a pure Java implementation of Ed25519.

   When running a peer, which requires intensive signature validation, it is advised switching to
   the native LibSodium implementation.

   Follow instruction to switch, best done when starting your application:
  
     https://github.com/Convex-Dev/convex/tree/main/convex-sodium




## <a name="convex.key-pair/account-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L69-L80) `account-key`</a>
``` clojure

(account-key key-pair)
```


Returns the account key of the given `key-pair`.

   An account key is a specialized cell behaving like a blob and representing the public key
   of an account.

## <a name="convex.key-pair/ed25519">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L40-L63) `ed25519`</a>
``` clojure

(ed25519)
(ed25519 seed)
(ed25519 key-public key-private)
```


Creates an Ed25519 key pair.

   It is generated from a [`seed`](#convex.key-pair/seed), a 32-byte blob. If not given, one is generated randomly.

   Alternatively, a [`key-public`](#convex.key-pair/key-public) and a [`key-private`](#convex.key-pair/key-private) retrieved from an existing key pair can
   be provided.

## <a name="convex.key-pair/hex-string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L84-L94) `hex-string`</a>
``` clojure

(hex-string key-pair)
```


Returns the public key of the given `key-pair` as a hex-string.
   
   64-char string where each pair of chars represents a byte in hexadecimal.

## <a name="convex.key-pair/key-pair?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L218-L225) `key-pair?`</a>
``` clojure

(key-pair? x)
```


Returns `true` is `x` is a key pair.

## <a name="convex.key-pair/key-private">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L98-L106) `key-private`</a>
``` clojure

(key-private key-pair)
```


Returns the `java.security.PrivateKey` of the given `key-pair`.

## <a name="convex.key-pair/key-public">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L110-L118) `key-public`</a>
``` clojure

(key-public key-pair)
```


Returns the `java.security.PublicKey` of the given `key-pair`.

## <a name="convex.key-pair/seed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L122-L132) `seed`</a>
``` clojure

(seed key-pair)
```


Returns the seed of the given `key-pair`.

   Attention, this is very sensitive information since it allows rebuilding the key-pair using [`ed25519`](#convex.key-pair/ed25519).

## <a name="convex.key-pair/sign">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L138-L153) `sign`</a>
``` clojure

(sign key-pair cell)
```


Returns the given `cell` as data signed by `key-pair`. That value is a cell itself
   and can be stored in Etch if required (see the `convex.db` namespace from `:module/cvm`).

   `signed->...` functions allows for extracting information from signed data.

   Most useful for signing transactions.
   See [`convex.client/transact`](#convex.client/transact).

## <a name="convex.key-pair/signed->account-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L157-L167) `signed->account-key`</a>
``` clojure

(signed->account-key signed)
```


Given signed data, returns the account key of the signer.

   See [`account-key`](#convex.key-pair/account-key), [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/signed->cell">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L171-L181) `signed->cell`</a>
``` clojure

(signed->cell signed)
```


Given signed data, returns the cell that was signed.

   See [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/signed->signature">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L185-L198) `signed->signature`</a>
``` clojure

(signed->signature signed)
```


Given signed data, returns the signature as a blob cell.

   See [`sign`](#convex.key-pair/sign).

## <a name="convex.key-pair/verify">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/key_pair.clj#L202-L212) `verify`</a>
``` clojure

(verify account-key signature cell)
```


Returns true if the given `cell` has indeed been signed by the given [`account-key`](#convex.key-pair/account-key).

   `signature` is the signature to verify as a blob cell.

-----

-----
# <a name="convex.pfx">convex.pfx</a>


Creating and managing a key store for storing key pairs in a file.

   Key pairs are indexed by alias (string). A store may be protected by a passphrase (optional).
   Additionally, each key pair is protected by its own passphrase (mandatory).
  
   See [`convex.key-pair`](#convex.key-pair) about key pairs.




## <a name="convex.pfx/alias+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L95-L102) `alias+`</a>
``` clojure

(alias+ key-store)
```


Returns a sequence of aliases available in the given `key-store` (or `nil`
   if the store is empty).

## <a name="convex.pfx/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L27-L47) `create`</a>
``` clojure

(create path)
(create path passphrase)
```


Creates a new key store in the file under `path`.
  
   An optional passphrase protecting the store may be provided.

## <a name="convex.pfx/key-pair-get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L120-L137) `key-pair-get`</a>
``` clojure

(key-pair-get key-store alias-or-account-key passphrase)
```


Retrieves a key pair from the given `key-store`.

   See [`key-pair-set`](#convex.pfx/key-pair-set).

## <a name="convex.pfx/key-pair-rm">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L106-L116) `key-pair-rm`</a>
``` clojure

(key-pair-rm key-store alias)
```


Removes a key pair from the given `key-store` by alias.

## <a name="convex.pfx/key-pair-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L141-L163) `key-pair-set`</a>
``` clojure

(key-pair-set key-store key-pair passphrase)
(key-pair-set key-store alias key-pair passphrase)
```


Adds the given `key-pair` to the `key-store`, protected by a mandatory `passphrase`.

   Public key is used as `alias` if none is provided.
  
   See [`key-pair-get`](#convex.pfx/key-pair-get).

## <a name="convex.pfx/load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L51-L67) `load`</a>
``` clojure

(load path)
(load path passphrase)
```


Loads a key store from the file under `path`.
  
   Passphrase must be provided if the store is protected by one.

## <a name="convex.pfx/save">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/pfx.clj#L71-L89) `save`</a>
``` clojure

(save key-store path)
(save key-store path passphrase)
```


Saves the given `key-store` to the file under `path`.
  
   An optional passphrase protecting the store may be provided.

-----
# <a name="convex.server">convex.server</a>


Running a peer server.

   Can either:

   - Run alone for dev and test
   - Run locally, synced with other local peers
   - Run locally but synced with the test network on `convex.world`
  
   See [README](../).




## <a name="convex.server/belief">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L261-L269) `belief`</a>
``` clojure

(belief server)
```


Returns the current belief of `server`.

## <a name="convex.server/controller">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L273-L283) `controller`</a>
``` clojure

(controller server)
```


Returns the controller associated with `server`.
  
   It was either explicitly specified in [`create`](#convex.server/create) or retrieved from the state.

## <a name="convex.server/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L74-L197) `create`</a>
``` clojure

(create keypair)
(create keypair option+)
```


Returns a new peer server.
  
   Can be started using [`start`](#convex.server/start) when required.

   A key pair is mandatory. See the [`convex.key-pair`](#convex.key-pair).

   An map of options may be provided:

   | Key                               | Value                                                      | Default                                     |
   |-----------------------------------|------------------------------------------------------------|---------------------------------------------|
   | `:convex.server/bind`             | Bind address (string)                                      | `"localhost"`                             |
   | `:convex.server/state`            | See below                                                  | `[:genesis]`                                |
   | `:convex.server/controller`       | Controller account address                                 | Retrieved from state                        |
   | `:convex.server/db`               | Database (see `:module/cvm`)                               | Default temp instance created automatically |
   | `:convex.server/n-peer`           | Maximum number of other peers this one should broadcast to | `20`                                        |
   | `:convex.server/persist-at-stop?` | True if peer data should be persisted in DB when stopped   | `true`                                      |
   | `:convex.server/port`             | Port                                                       | `18888`                                     |
   | `:convex.server/root-key`         | Optional cell (see [`persist`](#convex.server/persist))                            | `nil`                                       |
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
   other peer at [`start`](#convex.server/start), forming a network. The option map may specify:

   | Key                   | Value                      | Default         |
   |-----------------------|----------------------------|-----------------|
   | `:convex.server/host` | Address of the remote peer | `"localhost"` |
   | `:convex.server/port` | Port of the remote peer    | `18888`         |

## <a name="convex.server/data">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L287-L305) `data`</a>
``` clojure

(data server)
```


Returns a map cell with:

   | Key          | Value                                                       |
   |--------------|-------------------------------------------------------------|
   | `:belief`    | Current belief held by `server`                             |
   | `:genesis`   | Genesis state                                               |
   | `:history`   | Ordering position for which results and states are avaiable |
   | `:position`  | State index (ie. number of state transitions)               |
   | `:results`   | All available block results                                 |
   | `:state`     | Consensus state                                             |
   | `:timestamp` | Current timestamp                                           |

## <a name="convex.server/db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L309-L317) `db`</a>
``` clojure

(db server)
```


Returns the Etch instance used by the `server`.

## <a name="convex.server/endpoint">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L321-L332) `endpoint`</a>
``` clojure

(endpoint server)
```


Given a `server`, returns a map:
  
   | Key                   | Value                            |
   |-----------------------|----------------------------------|
   | `:convex.server/host` | Hostname this server is bound to |
   | `:convex.server/port` | Port this server is listening to |

## <a name="convex.server/n-belief-received">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L336-L342) `n-belief-received`</a>
``` clojure

(n-belief-received server)
```


Returns the number of beliefs received by `server`

## <a name="convex.server/n-belief-sent">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L346-L352) `n-belief-sent`</a>
``` clojure

(n-belief-sent server)
```


Returns the number of beliefs broadcasted by `server`

## <a name="convex.server/peer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L356-L366) `peer`</a>
``` clojure

(peer server)
```


Returns the peer object wrapped by the `server`.
   
   For advanced users only.

## <a name="convex.server/persist">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L203-L221) `persist`</a>
``` clojure

(persist server)
```


Persists peer data at the root of the server's Etch instance.

   If `:convex.server/root-key` was provided during [`create`](#convex.server/create), assumes the root value is a map and
   stores peer data under that key. Otherwise, stores peer dataa directly at the root.

   Persisted data can be recovered when creating a server with the same Etch instance (see `:convex.server/state`
   option in [`create`](#convex.server/create)).

   Done automatically at [`stop`](#convex.server/stop) is `:convex.server/persist-at-stop?` as set to `true` at [`create`](#convex.server/create).

   However, the database is not flushed. See `convex.db/flush` from `:module/cvm`.
  
   Returns `true` in case of success, `false` otherwise.

## <a name="convex.server/pubkey">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L370-L378) `pubkey`</a>
``` clojure

(pubkey server)
```


Returns the public key of this `server`'s peer.

## <a name="convex.server/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L225-L240) `start`</a>
``` clojure

(start server)
```


Starts `server`.

   See [`create`](#convex.server/create) first.

   If peer syncing was configured in [`create`](#convex.server/create), also connects to remote peer.
  
   Returns the `server`.

## <a name="convex.server/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L382-L390) `state`</a>
``` clojure

(state server)
```


Returns the consensus state held by the `server`.

## <a name="convex.server/status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L394-L413) `status`</a>
``` clojure

(status server)
```


Returns the current status of `server`, a Map cell such as:

   | Key                     | Value                            |
   |-------------------------|----------------------------------|
   | `:hash.belief`          | Hash of the current Belief       |
   | `:hash.state+`          | Hash of all the States           |
   | `:hash.state.consensus` | Hash of the Consensus State      |
   | `:hash.state.genesis`   | Hash of the Genesis State        | 
   | `:n.block`              | Number of blocks in the ordering |
   | `:point.consensus`      | Current consensus point          |
   | `:point.proposal`       | Current proposal point           |
   | `:pubkey`               | Public key of that peer          |

## <a name="convex.server/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/net/src/main/clj/convex/server.clj#L244-L255) `stop`</a>
``` clojure

(stop server)
```


Stops `server`.
  
   Previously started with [`start`](#convex.server/start).
  
   Does not close the Etch instance optionally provided when starting.
