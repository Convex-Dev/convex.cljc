# Any aspect of the Convex stack

This monorepo hosts a variety of applications and libraries written in Clojure providing access to all aspects of the [Convex network](https://github.com/Convex-Dev/convex)
with additional capabilities à la carte.

Since some key aspects of [Convex](https://convex.world/) have been modeled on Clojure constructs. Hence, there is no surprise in realizing that both form a unique and perfect
match. Even without having any interest in blockchain, it is still worth exploring features offered by this repository such as the immutable [Etch database](./project/db).

Released applications and libraries:

| Project | Library | Cljdoc | Download |
|---|---|---|---|
| [`:project/crypto`](./project/crypto) | [![Clojars](https://img.shields.io/clojars/v/world.convex/crypto.clj.svg)](https://clojars.org/world.convex/crypto.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/crypto.clj)](https://cljdoc.org/d/world.convex/crypto.clj/CURRENT) | / |
| [`:project/cvm`](./project/cvm) | [![Clojars](https://img.shields.io/clojars/v/world.convex/cvm.clj.svg)](https://clojars.org/world.convex/cvm.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/cvm.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT) | / |
| [`:project/dapp`](./project/dapp) | [![Clojars](https://img.shields.io/clojars/v/world.convex/dapp.clj.svg)](https://clojars.org/world.convex/dapp.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/dapp.clj)](https://cljdoc.org/d/world.convex/dapp.clj/CURRENT) | / |
| [`:project/db`](./project/db) | [![Clojars](https://img.shields.io/clojars/v/world.convex/db.clj.svg)](https://clojars.org/world.convex/db.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/db.clj)](https://cljdoc.org/d/world.convex/db.clj/CURRENT) | / |
| [`:project/net`](./project/net) | [![Clojars](https://img.shields.io/clojars/v/world.convex/net.clj.svg)](https://clojars.org/world.convex/net.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/net.clj)](https://cljdoc.org/d/world.convex/net.clj/CURRENT) | / |
| [`:project/run`](./project/run) | [![Clojars](https://img.shields.io/clojars/v/world.convex/run.clj.svg)](https://clojars.org/world.convex/run.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/run.clj)](https://cljdoc.org/d/world.convex/run.clj/CURRENT) | [CVX runner](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha2) |

Overview of main folders in the [./project](./project) directory:

| Project | Purpose |
|---|---|
| [`:project/app.fuzz`](./project/app/fuzz) | CLI multicore fuzzy tester, generates and tests random Convex Lisp forms | 
| [`:project/break`](./project/break) | Advanced generative test suite for the CVM ; novel smart contract testing |
| [`:project/crypto`](./project/crypto) | Key pair creation and management for digital signing |
| [`:project/cvm`](./project/cvm) | Convex types, reading Convex Lisp code, execution |
| [`:project/dapp`](./project/db) | Bundle of useful libraries for building decentralized applications |
| [`:project/db`](./project/db) | Create and handle immutable Etch databases crafted for Convex types |
| [`:project/net`](./project/net) | Convex network stack (running peers and using the binary client) |
| [`:project/recipe`](./project/recipe) | Recipes for understanding Convex and writing dApps |
| [`:project/run`](./project/run) | Convex Lisp Runner and REPL, advanced terminal environment |

Most of the time, using [`:project/dapp`](./project/dapp) is what you look for. It bundles useful libraries from this repository for the purpose of writing high-performance dApps (decentralized applications).

For learning, it is best starting with [`:project/recipe`](./project/recipe). This collection of examples showcases how to write efficient dApps, step-by-step, while learning more about
the Convex network.


## Community

We use Discord as the primary means for discussing Convex - you can join the public server at https://discord.gg/5j2mPsk

Alternatively, email the core maintainer of this repository: adam(at)convex.world

More information about the Convex network: https://convex.world/


## This repository

Each project follows a predictable structure:

- Project details are exposed in a dedicated README
- All source is located under the `./src` directory of each project or subproject
- Source is subdivided by language (eg. `clj`, `cvx`) and then by purpose (eg. `main`, `test`)
- All scripts and tasks are located and executed at the root of this repository

The following conventions are enforced in READMEs and source files:

- Namespaces shorten `convex` into `$`: `convex.cvm` -> `$.cvm`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`

More information about maintenance and organization can be found [in this file](./maintenance.md).


## License

Copyright © 2021 Adam Helinski, the Convex Foundation, and contributors

Licensed under the Apache License, Version 2.0
