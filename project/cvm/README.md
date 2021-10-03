# `:project/cvm`

[![Clojars](https://img.shields.io/clojars/v/world.convex/cvm.clj.svg)](https://clojars.org/world.convex/cvm.clj)  
[![cljdoc](https://cljdoc.org/badge/world.convex/cvm.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT)

This library hosts core Convex utilities.

The CVM (Convex Virtual Machine) executes operations over state, as described in [CAD
005](https://github.com/Convex-Dev/design/blob/main/cad/005_cvmex/README.md).

It inputs cells and outputs cells, the word *"cell"* designating immutable Convex objects that have been modeled closely
on the Clojure philosophy. Most of those types will be familiar to any Clojurist: keywords, symbols, maps, vectors, etc.
"Cell" is used instead of "data" because this word also encompasses additional types such as functions, which are
commonly not considered as data, even in Clojure.

Code to execute is represented as cells. Convex Lisp is a language evidently based on Clojure, almost a subset of it. It
is used to query information from the Convex network and submit transactions, such as creating or calling smart
contracts. More information about Convex Lisp can be found in [this guide](https://convex.world/cvm).

Etch is an immutable database specially designed for cells. It is a Merkle DAG, a key-value store where values can be
any cells and keys are hashes of the cells they point to. It is particularly efficient by implementing structural
sharing and semi-lazy loading, meaning that data bigger than memory can be queried because not all of it will be loaded
at once.

Overview of namespaces:

| Namespace | Purpose |
|-----------|---------|
| `convex.cell`   | Constructors for cells                                  |
| `convex.clj`    | Translating cells into Clojure data                     |
| `convex.cvm`    | CVM execution, manipulation, gathering various insights |
| `convex.cvm.db` | Set which Etch instances are used by default            |
| `convex.db`     | Open and handle Etch instances                          |
| `convex.read`   | Parse text source into cells                            |
| `convex.std`    | Standard library for cells, similar to `clojure.core`   |
| `convex.write`  | Convert cells into text source                          |

Those namespaces are built on top of [`convex-core` in the core Java repository](https://github.com/Convex-Dev/convex)
and provide commonly needed features for building tools and decentralized applications.

Examples and walk-through can be found in [`:project/recipe`](../recipe).
