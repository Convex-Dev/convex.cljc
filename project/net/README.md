# `:project/net`

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
     ($.sign/ed25519-gen))
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
