# `:project/net`

[![Clojars](https://img.shields.io/clojars/v/world.convex/net.clj.svg)](https://clojars.org/world.convex/net.clj)
[![cljdoc](https://cljdoc.org/badge/world.convex/net.clj)](https://cljdoc.org/d/world.convex/net.clj/CURRENT)

This project hosts network utilities for:

- Communicating with peers via the binary protocol they speak (eg. submit transactions)
- Running a peer (locally or synced with the Convex network)

In the following example, namespaces `convex.cell` and `convex.read` from [`:project/cvm`](../cvm) are used to respectively
create cells and convert Convex Lisp source code to cells.

This document exposes the most commonly used features. Refer to the full API for the broader picture.


## Prerequisite

Following aspects are common to both clients and peers.


### Generating a key pair

Key pairs are used for digital signatures. More information can be found in [`:project/crypto`](../crypto) which provides
the `convex.sign` namespace for generating key pairs and the `convex.pfx` namespace for managing them securely in a file
(preferred method over custom ideas ; security is hard).

Creating a key pair for below examples:

```clojure
(def key-pair
     ($.sign/ed25519))
```

Account key (cell representing the public key of the key pair) can be retrieved using:

```clojure
($.sign/account-key key-pair)
```


## Using a binary client

Namespace `convex.client` hosts utilities for connecting to a peer via the Convex binary protocol.

Address and port of the peer are needed. For instance, for the current test net on [convex.world](https://convex.world/):

```clojure
(def client
     ($.client/connect {:convex.server/host "convex.world"
                        :convex.server/port 18888}))
```


### Handling results

IO operations return a future which resolves when a result is received from the peer. This result embeddeds a value. If
operation ran to success, this value is the result ot that operation. In case of failure, the value designates a message
(typically a CVM string) and the result also contains an error code (typically a CVM keyword) and possibly a stack trace
(a CVM vector of CVM strings).

Hence, error handling could look like this:

```clojure
(let [result     (deref request)
      error-code ($.client/error-code result)
      value      ($.client/value result)]
  (if error-code
    {:success?   false
     :error-code error-code
     :message    value
     :trace      ($.client/trace result)}
    {:success? true
     :return   value}))

```


### Queries

A query does not require any signature as it does not involve consensus and do not incur fees. Code is simply run at the level
of the connected peer only and any state change is discarded.

Queries are used to read data from a trusted peer, compute a result, or simulate an actual transaction. An address is specified so
that the query run *"as if"* it was a transaction submitted by that address, but this address does not have to be owned.


```clojure
;; Without error handling.
;;
(-> ($.client/query client
                    ($.cell/address 42)
                    ($.read/string "(do
                                      (def x
                                           100)
                                      [*address*
                                       (inc x)])"))
    deref
    $.client/value)

;; [#42 101] (CVM vector)


;; We can prove that new state was discarded.
;;
(-> ($.client/query client
                    ($.cell/address 42)
                    ($.cell/symbol "x"))
    deref
    $.client/error-code)

;; :UNDECLARED (CVM keyword)
;;
;; Error because query defining `x` was never run on-chain.
```


### Transactions

A transaction contains code to run on behalf on an account. It is digitally signed by the private key of that account.
A sequence number is also embedded which is a number related to an account and incremented on each successful transaction.
This is crucial to prevent [replay attacks](https://en.wikipedia.org/wiki/Replay_attack).

Three types of transactions exist and can be create via [`:project/cvm`](../cvm):

- `$.cell/call` for performing an actor call
- `$.cell/invoke` for executing given cell as code
- `$.cell/transfer` for efficiently transferring Convex Coins to another account

Transactions involve consensus and incur fees, albeit fees are actually fictitious on the current test network.

Let us suppose we are using the account with address `#42` and the key pair generated above.

If not known, the sequence number can be retrieved via a query. This helper makes it easier:

```clojure
(def seqnum
     (deref ($.client/sequence client
                               ($.cell/address 42))))
```

Example of an `invoke` transaction:

```clojure
;; Without error handling.
;;
(-> ($.client/transact client
                       key-pair
                       ($.cell/invoke ($.cell/address 42)
                                      seqnum
                                      ($.read/string "(def foo 100")))
    deref
    $.client/value)


;; Using even a query, We can prove that new state was computed.
;;
(-> ($.client/query client
                    ($.cell/address 42)
                    ($.cell/symbol "x"))
    deref
    $.client/value)

;; 100 (as a CVM long)
```


### Creating an account

For transactions, an account is required. It is easy to create an account with the Convex Lisp function `create-account`.
However, to run this function, one must aleady own at least one account.

When using a client against an isolated peer, see next section.

When using a client against the current test network on [convex.world](https://convex.world), 2 solutions exist. All require
the public key of the key pair generated for the new account as a 32-byte blob.

- Create a new account through the [sandbox](https://convex.world/sandbox) and transact `(set-key KEY)`
- Same but through the [REST API](https://convex.world/rest-api/create-an-account)


## Running a peer

Namespace `convex.server` hosts utilities for running peers. The API describes many options depending on the exact requirements.
Following examples are minimalistic, more aspects can be configured.

When a peer issues a block of transactions for consensus, this block must be digitally signed. This is why providing a key pair
at server creation is mandatory.

By default, the database used by peers is a temporary one. If state must be persisted across restarts, a stable database can
be provided in options when creating the server. See [`:project/db`](../db).

```clojure
(def db
     ($.db/open "my_db.etch"))
```


### Locally in isolation

In this example, a genesis state representing the network state is created automatically as well as a peer and
account `#12` controlling that peer. A genesis state does not contain much besides official libraries and actors.

```clojure
(def server
     (-> ($.server/create key-pair
                          {:convex.server/db db})
         $.server/start))
```

Transactions can be run under account `#12` with this `key-pair` via the binary client. From there, other accounts can be
created, and so on.


### Syncing with a remote peer

The easiest way to get started with remote syncing is to connect to the current Convex test network on [convex.world](https://convex.world).
More experienced users can try running several local peers and syncing them together, the process is ultimately the same.

First, peer must be declared on that remote network, otherwise it will be unresponsive. Steps are described [in this guide](https://convex.world/cvm/peer-operations).

In the previous example, a genesis state was created by default. Several options exists for providing an initial state and
this example uses peer syncing. When creating the server, state is retrieved from the specified remote peer. Then, when starting the server,
it connects to that remote so that it can participate in the network.

```clojure
(def server
     (-> ($.server/create key-pair
                          {:convex.server/db    db
                           :convex.server/bind  "0.0.0.0"
                           :convex.server/state [:sync
                                                 {:convex.server/host "convex.world"}]
                           :convex.server/url   "$PUBLIC-IP:18888"
                           })
         $.server/start))
```

A bind address `"0.0.0.0"` is explicitly provided because it is usually the easiest way for making the server available to the outside.
Default port is always `18888`. A URL is also specified where `$PUBLIC-ÏP` should be the IP address other peers can use to join your server.
This URL is stored on-chain using `(set-peer-data ...)` as described [in this guide](https://convex.world/cvm/peer-operations). Ultimately,
this usually involves some port mapping in your router to ensure that any client/peer connecting to this URL actually connects to your peer.

If the URL is not provided or is not accessible, your peer will not receive broadcasts from other peers and will not be able to take active part
in the consensus. However, it will keep its state pretty much up-to-date by polling peers whose URL is accessible.

In order to see debug log messages (very useful when learning about peers), set the [Timbre](https://github.com/ptaoussanis/timbre) log level
via this env variable:

```clojure
TIMBRE_LEVEL=':debug'
```
