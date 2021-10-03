# `:project/net`

[![Clojars](https://img.shields.io/clojars/v/world.convex/net.clj.svg)](https://clojars.org/world.convex/net.clj)  
[![cljdoc](https://cljdoc.org/badge/world.convex/net.clj)](https://cljdoc.org/d/world.convex/net.clj/CURRENT)

This library hosts network utilities from the Convex stack. Peers are nodes of the Convex network that perform
transactions and keep state. The fast binary client is used to connect to a peer for querying information from
the network or submitting transactions, such as creating or calling smart contracts.

Most network interactions require a key pair. For instance, each user account in the network has a public key attached.
When a transaction is submitted for an account, executed on behalf of that account, it must be signed using the
matching private key. This is [public-key cryptography](https://en.wikipedia.org/wiki/Public-key_cryptography) and
it ensures that only the owner of an account, owning its secret private key, can submit code to be executed in the
context of that account.

Overview of namespaces:

| Namespace | Purpose |
|-----------|---------|
| `convex.client` | Fast binary client for talking to the Convex network |
| `convex.pfx`    | Store key pairs securely in password protected files |
| `convex.server` | Run a peer node                                      |
| `convex.sign`   | Generate and handle key pairs                        |

Those namespaces are built on top of [`convex-core` and `convex-peer` in the core Java
repository](https://github.com/Convex-Dev/convex) and provide commonly needed features for building tools and
decentralized applications.

Examples and walk-through can be found in [`:project/recipe`](../recipe).
