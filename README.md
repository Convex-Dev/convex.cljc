# `convex.cljc` - [CHANGES](./doc/changelog.md) - [MODULES](./module)

![Workflow](https://github.com/convex-dev/convex.cljc/actions/workflows/workflow.yml/badge.svg)

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


---

## Public work

Modules exposed publicly are listed in [`./module`](./module).

This repository uses [calver](https://calver.org) and follows best effort
towards avoiding known breaking changes in publicly exposed modules. Excepts
regarding experimental module starting with `lab.` which may be subject to
breaking changes or removal.

All consumed modules must be required with the same `stable/YYYY-0M-0D` tag.


Applications:

- [`shell`](./module/shell)

Libraries exposed as [Git
dependencies](https://clojure.org/guides/deps_and_cli#_using_git_libraries) by
[Clojure CLI](https://clojure.org/guides/deps_and_cli):

- [`cvm`](./module/cvm)
- [`gen`](./module/gen)
- [`net`](./module/net)

Learning materials:

- [`recipe`](./module/recipe)


---


## Community

This repository is discussed on the [Clojurians Slack community at `#convex`](https://join.slack.com/t/clojurians/shared_invite/zt-lsr4rn2f-jealnYXLHVZ61V2vdi15QQ).

Our [Discord channel](https://discord.gg/5j2mPsk) is the best for discussing the
overall project, beyond the Clojure tooling.

More more information about the Convex network on the [official website](https://convex.world).


---


## Notes

This monorepo is managed with
[Maestro](https://github.com/protosens/monorepo.cljc/tree/main/module/maestro).

- [Conventions](./doc/conventions.md)
- [Fork this repository](./doc/fork_this_repository.md)


---


## License

Copyright © 2021 Protosens SRL and the Convex Foundation

Licensed under the Apache License, Version 2.0 (see [LICENSE](./LICENSE))
