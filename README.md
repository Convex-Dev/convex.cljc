# Any aspect of the Convex stack

This monorepo hosts a variety of applications and libraries written in Clojure providing access to all aspects of the
[Convex network](https://github.com/Convex-Dev/convex).

Convex can be understood as public database replicated worldwide among peer nodes. The network is effectively
permissionless and anyone is free to participate. Data is defined in accounts akin to namespaces. Anyone can read data
from those accounts but only owners and programmatically authorized users can transact new data using
cryptographic keys. A consensus algorithm between peers guarantees that the network is tamperproof.

Using such a network, it is possible to write decentralized applications (dApps) that do not need a centralized database or server,
are censorship-resistant, and highly-available. Such characterics provide a robust framework for managing any kind of
state, even digital assets.

Instead of relying on a query language like SQL or datalog, querying or transacting data on the network is done via
Convex Lisp. Almost a subset of Clojure, it is a fully Turing-complete language centered on immutable values. It is
effectively the very first decentralized Lisp in the history of computing. A guide is accessible [at this
link](https://convex.world/cvm).

Newcomers should follow progressive examples in [`:project/recipe`](./project/recipe) in order to better understand how
such a network works and how to build dApps in Clojure. Since Convex is written in Java, one can use the exact same
tools for writing applications that are being used for running the network. 

Most useful modules from this repositories are:

| Project | Library | Cljdoc | Download |
|---|---|---|---|
| [`:project/cvm`](./project/cvm) | [![Clojars](https://img.shields.io/clojars/v/world.convex/cvm.clj.svg)](https://clojars.org/world.convex/cvm.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/cvm.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT) | / |
| [`:project/net`](./project/net) | [![Clojars](https://img.shields.io/clojars/v/world.convex/net.clj.svg)](https://clojars.org/world.convex/net.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/net.clj)](https://cljdoc.org/d/world.convex/net.clj/CURRENT) | / |
| [`:project/run`](./project/run) | / | / | [CVX runner](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha3) |

While these tools are used for talking to the network and handling data, the rest is usual application development and
there is nothing specific about it.


## Community

This repository is discussed on the Clojurians Slack community at `#convex`: https://join.slack.com/t/clojurians/shared_invite/zt-lsr4rn2f-jealnYXLHVZ61V2vdi15QQ

Discord is the primary channel for discussing the overall Convex project: https://discord.gg/5j2mPsk

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
