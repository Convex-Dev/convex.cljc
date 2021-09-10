# Advanced tooling for Convex Lisp and the CVM

This monorepo offers a variety of applications and libraries written in Clojure for working with the [Convex Virtual Machine
and Convex Lisp](https://github.com/Convex-Dev/convex).

Overview of main folders in the [./project](./project) directory:

| Project | Purpose |
|---|---|
| [`:project/app.fuzz`](./project/app/fuzz) | CLI multicore fuzzy tester, generates and tests random Convex Lisp forms | 
| [`:project/break`](./project/break) | Advanced generative test suite for the CVM ; novel smart contract testing |
| [`:project/clojurify`](./project/clojurify) | Convex <-> Clojure data conversions, quick evaluation, useful `test.check` generators |
| [`:project/crypto`](./project/crypto) | Key pair creation and management for digital signing |
| [`:project/cvm`](./project/cvm) | Handling Convex data and the CVM, low-level utilities |
| [`:project/run`](./project/run) | Convex Lisp Runner and REPL, advanced terminal environment |


## Releases

Released applications and libraries:

| Project | Library | Cljdoc | Download |
|---|---|---|---|
| [`:project/clojurify`](./project/clojurify) | [![Clojars](https://img.shields.io/clojars/v/world.convex/clojurify.clj.svg)](https://clojars.org/world.convex/clojurify.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/clojurify.clj)](https://cljdoc.org/d/world.convex/clojurify.clj/CURRENT) | / |
| [`:project/crypto`](./project/crypto) | [![Clojars](https://img.shields.io/clojars/v/world.convex/crypto.clj.svg)](https://clojars.org/world.convex/crypto.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/crypto.clj)](https://cljdoc.org/d/world.convex/crypto.clj/CURRENT) | / |
| [`:project/cvm`](./project/cvm) | [![Clojars](https://img.shields.io/clojars/v/world.convex/cvm.clj.svg)](https://clojars.org/world.convex/cvm.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/cvm.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT) | / |
| [`:project/run`](./project/run) | [![Clojars](https://img.shields.io/clojars/v/world.convex/run.clj.svg)](https://clojars.org/world.convex/run.clj) | [![cljdoc](https://cljdoc.org/badge/world.convex/run.clj)](https://cljdoc.org/d/world.convex/run.clj/CURRENT) | [CVX runner](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha2) |


## Structure

Each project follows a predictable structure:

- Project details are exposed in a dedicated README
- All source is located under the `./src` directory of each project or subproject
- Source is subdivided by language (eg. `clj`, `cvx`) and then by purpose (eg. `main`, `test`)
- All scripts and tasks are located and executed at the root of this repository


## Conventions

The following conventions are enforced in READMEs and source files:

- Namespaces shorten `convex` into `$`: `convex.cvx` -> `$.cvm`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`


## Setup

Following sections are only useful for managing this repository or experimenting with a clone/fork.


### Clojure Deps

This repository rely on the [Clojure command line tools](https://clojure.org/guides/getting_started). Familiarity with
[Clojure Deps](https://clojure.org/guides/deps_and_cli) is required.

Alias names follow the convention established in [Maestro](https://github.com/helins/maestro.clj). For instance, see project aliases in table above.


### Babashka and tasks

All scripting is done using [Babashka](https://book.babashka.org/), a fast Clojure interpreter that comes with a powerful task runner.
Follow this [simple installation process](https://book.babashka.org/#_installation).

All tasks are written in [./bb.edn](./bb.edn) and can by listed by running in your shell:

```bash
bb tasks
```

Printed list shows all current tasks available for managing this repository: starting dev mode, running some tests, compiling, etc.

A task typically requires one or several aliases from `deps.edn` and sometimes CLI arguments.

For instance:

```bash
# Starts project 'CVM' in dev mode which is an alias in `deps.edn` + personal `:nrepl` alias 
$ bb dev :project/cvm:nrepl

# Testings all namespaces for project 'break' and dependencies
$ bb test :project/break
```


## Dev

Following directory structure, each project typically has a `dev_templ.clj` file in its Clojure dev files which requires useful namespaces.
This file can be copied in the same directory to `dev.clj` for hacking and trying thing out. Those `dev.clj` files are effectively private and will
not appear in this repository.

For example, see [`:project/all` dev directory](./project/all/src/clj/dev/convex/all).


## License

Copyright © 2021 Adam Helinski, the Convex Foundation, and contributors

Licensed under the Apache License, Version 2.0
