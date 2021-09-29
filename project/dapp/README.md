# `:project/dapp`

[![Clojars](https://img.shields.io/clojars/v/world.convex/dapp.clj.svg)](https://clojars.org/world.convex/dapp.clj)
[![cljdoc](https://cljdoc.org/badge/world.convex/dapp.clj)](https://cljdoc.org/d/world.convex/dapp.clj/CURRENT)

Simply bundles useful libraries from this monorepo for the intent of building decentralized applications.

Contains:

- [`:project/crypto`](../crypto)
- [`:project/cvm`](../cvm)
- [`:project/db`](../db)
- [`:project/net`](../net)

Step-by-step examples on how to use those libraries can be found in [`:project/recipe`](../recipe).

Decentralized applications leverage a decentralized network for storing and managing state. In the case of the
[Convex network](https://convex.world), *state* means anything that can be represented as a Clojure-like data type.
In essence, the network acts as a public database that is replicated worldwide and is completely tamper-proof, which
opens new opportunities. Data can be read via queries and can be modified via transactions.

Overall, a decentralized Convex application involves 2 aspects:

**Smart contracts.** Running on the network, they manage state. Written in Convex Lisp, a language evidently similar
to Clojure, functions describe how state can be altered with proper access control. Calling a contract means effectively
calling such a function with the intent of modifying state. More information about Convex Lisp can be found in
[this guide](https://convex.world/cvm).

**User interface.** Or anything between the network and a user or another machine. A Clojurescript application
would use the [REST API](https://convex.world/tools/rest-api) to interact with the network and could be written
in any common framework such as [Re-frame](https://github.com/day8/re-frame). A Clojure application would use the
fast binary client over TCP as seen in [this recipe](./../recipe/src/clj/main/convex/recipe/client.clj). This is akin
to regular web/app development with the difference that a decentralized network is used instead of a centralized
database or server.
