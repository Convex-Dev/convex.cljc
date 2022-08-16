# `:module/recipe`

This project hosts a series of recipes, concrete examples regarding how Convex works and how to build high-performance
decentralized applications. They are meant to run at the REPL. Early recipes explain core concepts while latter ones
focus on networking and interacting with the current testnet.

Find more information about the Convex network on the [official website](https://convex.world).

Those examples relies on 2 libraries from this monorepository:

- [`:module/cvm`](../cvm), core utilities
- [`:module/net`](../net), network utilities

Recommended order is:

- [convex.recipe.cell](./src/main/clj/convex/recipe/cell.clj), to understand cells, types forming the core data model
- [convex.recipe.db](./src/main/clj/convex/recipe/db.clj), to understand how cells are persisted in an immutable database
- [convex.recipe.cvm](./src/main/clj/convex/recipe/cvm.clj), to understand the execution engine
- [convex.recipe.key-pair](./src/main/clj/convex/recipe/key_pair.clj), for generating and managing key pairs
- [convex.recipe.rest](./src/main/clj/convex/recipe/rest.clj), useful methods for the `convex.world` REST API (eg. creating an account)
- [convex.recipe.client](./src/main/clj/convex/recipe/client.clj), using the fast binary client to query the network and issue transactions
- [convex.recipe.peer.local](./src/main/clj/convex/recipe/peer/local.clj), to run a local standalone peer
- [convex.recipe.peer.testnet](./src/main/clj/convex/recipe/peer/testnet.clj), to run a peer connected to the current testnet


Steps:

- Clone/fork this repo
- Ensure [Babashka](https://github.com/babashka/babashka) is installed
- Start REPL from the root of this whole repository
- Open any of the namespaces above and eval stuff


From the root of this repository, in your terminal:

    clj -M$( bb aliases:dev :module/recipe )

This Babashka task merely prints all required aliases. Given a little shell
substitution using `$()`, this easily combines with the regular `clj` command.

Additional aliases can be appended. For instance, depending on personal setup,
one could have an `:nrepl` alias defined in `~/.clojure/deps.edn` in order to
provide NREPL connectivity:

    clj -M$( bb aliases:dev :module/recipe ):nrepl

Often, especially when running a peer, it is best setting the log level to `:debug` in order to see more accurately what is going on:

    env TIMBRE_LEVEL=:debug clj -M$( bb aliases:dev :module/recipe )
