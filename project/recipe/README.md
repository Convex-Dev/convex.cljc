# `project/recipe`

This project hosts a series of recipes, concrete examples regarding how Convex works and how to build high-performance
decentralized applications. They are meant to run at the REPL. Early recipes explain core concepts while latter ones
focus on networking and interacting with the current testnet.

More information about Convex: https://convex.world

Overall, they showcase what can be done using [`:project/dapp`](../dapp). Go there for the API and a brief explanation
of what decentralized applications are.

Recommended order is:

- [convex.recipe.cell](./src/clj/main/convex/recipe/cell.clj), to understand cells, types forming the core data model
- [convex.recipe.db](./src/clj/main/convex/recipe/db.clj), to understand how cells are persisted in an immutable database
- [convex.recipe.cvm](./src/clj/main/convex/recipe/cvm.clj), to understand the execution engine
- [convex.recipe.key-pair](./src/clj/main/convex/recipe/key_pair.clj), for generating and managing key pairs
- [convex.recipe.rest](./src/clj/main/convex/recipe/rest.clj), useful methods for the `convex.world` REST API (eg. creating an account)
- [convex.recipe.client](./src/clj/main/convex/recipe/client.clj), using the fast binary client to query the network and issue transactions
- [convex.recipe.peer.local](./src/clj/main/convex/recipe/peer/local.clj), to run a local standalone peer
- [convex.recipe.peer.testnet](./src/clj/main/convex/recipe/peer/testnet.clj), to run a peer connected to the current testnet


Steps:

- Clone/fork this repo
- Ensure [Babashka](https://github.com/babashka/babashka) is installed
- Start REPL from the root of this whole repository
- Open any of the namespaces above and eval stuff


From the root of this repository, in your terminal, supposing `:REPL` is a personal alias adding REPL connectivity:

```
$ bb dev :project/recipe:REPL
```

Often, especially when running a peer, it is best setting the log level to debug in order to see more accurately what is going on:

```
$ env TIMBRE_LEVEL=:debug  bb dev :project/recipe:REPL
```
