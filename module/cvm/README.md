# `module/cvm` - [API](doc/API.md)  - [CHANGES](doc/changelog.md)

Core utilities related to the Convex Virtual Machine.

```clojure
;; Add to dependencies in `deps.edn`:
;;
world.convex/cvm
{:deps/root "module/cvm"
 :git/sha   "06c7137"
 :git/tag   "stable/2023-01-18"
 :git/url   "https://github.com/convex-dev/convex.cljc"}
```

```clojure
;; Supported platforms:
;;
[:jvm]
```


---

The CVM executes operations over state, as described in [CAD
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

| Namespace       | Purpose                                                 |
|-----------------|---------------------------------------------------------|
| `convex.cell`   | Constructors for cells                                  |
| `convex.clj`    | Translating cells into Clojure data                     |
| `convex.cvm`    | CVM execution, manipulation, gathering various insights |
| `convex.db`     | Open and handle Etch instances                          |
| `convex.read`   | Parse text source into cells                            |
| `convex.std`    | Standard library for cells, similar to `clojure.core`   |
| `convex.write`  | Convert cells into text source                          |

Those namespaces are built on top of [`convex-core` in the core Java repository](https://github.com/Convex-Dev/convex)
and provide commonly needed features for building tools and decentralized applications.

Examples and walk-through can be found in [`:module/recipe`](../recipe).

This library offers support for
[Clj-kondo](https://github.com/clj-kondo/clj-kondo). Follow the [usual steps for
copying the
required configuration](https://github.com/clj-kondo/clj-kondo#project-setup).

