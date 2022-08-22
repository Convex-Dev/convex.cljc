# Any aspect of the Convex stack

This monorepo hosts a variety of applications and libraries written in Clojure providing access to all aspects of the
[Convex network](https://github.com/Convex-Dev/convex).

Convex can be understood as public database replicated worldwide between peer nodes. The network is effectively
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

Newcomers should follow progressive examples in [`:module/recipe`](./module/recipe) in order to better understand how
such a network works and how to build dApps in Clojure. Since Convex is written in Java, one can use the exact same
tools for writing applications as those being used for running the network. 

Most useful modules from this repositories are:

| Project | Library | Cljdoc | Download |
|---|---|---|---|
| [`:module/cvm`](./module/cvm) | [![Clojars](https://img.shields.io/clojars/v/world.convex/cvm.clj.svg)](https://clojars.org/world.convex/cvm.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/cvm.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT) | / |
| [`:module/gen`](./module/gen) | [![Clojars](https://img.shields.io/clojars/v/world.convex/gen.clj.svg)](https://clojars.org/world.convex/gen.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/gen.clj)](https://cljdoc.org/d/world.convex/gen.clj/CURRENT) | / |
| [`:module/net`](./module/net) | [![Clojars](https://img.shields.io/clojars/v/world.convex/net.clj.svg)](https://clojars.org/world.convex/net.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/net.clj)](https://cljdoc.org/d/world.convex/net.clj/CURRENT) | / |
| [`:module/run`](./module/run) | / | / | [CVX runner](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha3) |


## Community

This repository is discussed on the [Clojurians Slack community at `#convex`](https://join.slack.com/t/clojurians/shared_invite/zt-lsr4rn2f-jealnYXLHVZ61V2vdi15QQ).

Our [Discord channel](https://discord.gg/5j2mPsk) is the best for discussing the
overall project, beyond the Clojure tooling.

More more information about the Convex network on the [official website](https://convex.world).


## Understanding this monorepo

Each module is found under [./module](./module) and follows a predictable structure:

- Dedicated README, changelog, source, etc
- All source is located under the `./src` subdirectories
- Source is subdivided by purpose (eg. `main`, `test`) and then by language (eg. `clj`, `cvx`)
- All scripts and tasks are located and executed from the root of this repository

The following conventions are enforced in READMEs and source files:

- Namespaces shorten `convex` into `$`: `convex.cvm` -> `$.cvm`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`

The [./deps.edn](./deps.edn) file is organized around aliases where each alias has one
particular purpose: an external library, a module from this repository, etc

[Babashka](https://book.babashka.org/#_installation) is used for running tasks
found in [./bb.edn](./bb.edn), listed as such:

    bb tasks

The `aliases:...` tasks are especially important. They simply print all
required [./deps.edn](./deps.edn) aliases for given a module and a given
purpose. For instance, when
working on [`:module/cvm`](./module/cvm):

    bb aliases:dev :module/cvm

Which can then easily be combined with [Clojure
CLI](https://clojure.org/guides/getting_started) given a little shell
substitution with `$()`:

    clj -M$( bb aliases:dev :module/cvm )

Of course, any other aliases required for your own setup can be appended.

Testing a module, for instance the suite of generative tests for the CVM:

    clj -M$( bb aliases:test :module/break )

## License

Copyright © 2021 Protosens SRL and the Convex Foundation

Licensed under the Apache License, Version 2.0
