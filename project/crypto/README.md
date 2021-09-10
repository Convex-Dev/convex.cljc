# `:project/crypto`

[![Clojars](https://img.shields.io/clojars/v/world.convex/crypto.clj.svg)](https://clojars.org/world.convex/crypto.clj)
[![cljdoc](https://cljdoc.org/badge/world.convex/crypto.clj)](https://cljdoc.org/d/world.convex/crypto.clj/CURRENT)

Crypto utilities. Especially needed when interacting with a network, either as a client or as a peer.


## Digital signatures

Namespace `convex.sign` provides Ed25519 key pair generation and signing. For instance, transactions must be signed
by an account using its key pair.

```clojure
(def key-pair
     ($.sign/ed25519-gen))


;; Sometimes, only the account key is needed (public key as a CVM cell).
;;
($.sign/account-key key-pair)
```


Some features such as clients and servers from [`:project/net`](../net) require key pairs.


## Managing key pairs

Namespace `convex.pfx` provides utilities for creating and managing PFX key stores (secure files where key pairs can
be stored).

```clojure
;; Creating store in a given file, protected by a passphrase.
;;
(def key-store
     ($.pfx/create "path/to/file.pfx"
                   "store-passphrase"))

;; If store already exists, `$.pfx/load` must be used.


;; Adding key pair from previous example under alias "my-key-pair".
;;
($.pfx/key-pair-set key-store
                    "my-key-pair"
                    key-pair
                    "key-pair-passphrase")


;; Saving key store to same file using same passphrase to protect it (could be a different one).
;;
($.pfx/save key-store
            "path/to/file.pfx"
            "store-passphrase")


;; Key pair can easily be retrieved using its alias and passphrase.
;;
($.pfx/key-pair-get key-store
                    "my-key-pair"
                    "key-pair-passphrase")

```
