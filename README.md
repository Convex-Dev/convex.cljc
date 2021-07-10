# Advanced Clojure toolset for the CVM and Convex Lisp

This monorepo offers a variety of applications and libraries for working with the Convex Virual Machine and Convex Lisp

In addition, it holds Convex Lisp implementations for actors and libraries officially managed by the Convex Foundation.

Overview of the [./project](./project) directory:

| Project | `deps.edn` alias | Purpose |
|---|---|---|
| [all](./project/all) | `:project/all` | Environment for running this repository in dev mode with access to all projects |
| [app/fuzz](./project/app/fuzz) | `:project/app.fuzz` | CLI multicore fuzzy tester, generates and tests random Convex Lisp forms | 
| [app/run](./project/app/run) | `:project/app.run` |CLI Convex Lisp runner, from the comfort of your terminal |
| [break](./project/break) | `:project/break` | Advanced generative test suite for the CVM ; prime example of testing Convex Lisp thoroughly |
| [clojuriy](./project/clojurify) | `:project/clojurify` |Convex <-> Clojure data conversions, quick evaluation, useful `test.check` generators for testing |
| [cvm](./project/cvm) | `:project/cvm` | Handling Convex data and the CVM, low-level utilities |
| [cvx](./project/cvx) | `:project/cvx.XXX` | Experimental and stable Convex Lisp actors and libraries officially managed by the Convex Foundation |
| [run](./project/run) | `:project/run` | Core implementation of [`:project/app/.run`](./app/run) ; reusable for similar purposes |
| [sync](./project/sync) | `:project/sync` | Framework for loading and executing Convex Lisp snippets into a a single context ; executing files from disk |
| [watch](./project/watch) | `:project/watch` | Live-reloading of Convex Lisp files |

Ordered recommendation for building a solid understanding:

- [cvm](./project/cvm), for understanding core CVM utilities and Convex data
- [clojurify](./project/clojurify), for quickly evaluating Convex Lisp during dev and when writing tests
- [break](./project/break), for seeing thorough examples of testing Convex Lisp with [test.check](https://github.com/clojure/test.check)
- [cvx](./project/cvx), for seeing more complex examples of Convex Lisp actors and libraries, associated development and testing

If interested in the Convex Lisp Runner:

- [sync](./project/sync), about syncing code snippets into one CVM context, loading files from disk
- [watch](./project/watch), about providing a live-reloading experience on top of [sync](./project/sync)
- [run](./project/run), about merging [sync](./project/sync) and [watch](./project/watch)
- [app/run](./project/app/run), finally, a lightweight CLI interface on top of all that


## Structure

These utilities are divided, as seen in the [./project](./project) directory which lists everything that is currently
available. Albeit numerous, each project and subproject follows a predictable directory structure that should be straightforward
to understand:

- Project details are exposed in a dedicated README
- All source is located under the `./src` directory of each project or subproject
- Source is subdivided by language (eg. `clj`, `cvx`) and then by purpose (eg. `main`, `test`)
- All scripts and tasks are located and executed at the root of this repository
- All namespaces and source files are documented, which, considering READMEs, should help building a clear understanding


## Conventions

The following conventions are enforced in READMEs and source files:

- Namespaces shorten `convex` into `$`: `convex.app.run` -> `$.app.run`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`


## Setup

Currently, none of it has been publicly released. User is expected to fork this repository and use what is needed.

This monorepo is managed with [Maestro](https://github.com/helins/maestro.clj).


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

A task typically one or several aliases from `deps.edn` and sometimes CLI arguments.

For instance:

```bash
# Starts project 'CVM' in dev mode which is an alias in `deps.edn` + personal `:nrepl` alias 
$ bb dev :project/cvm:nrepl

# Testings all namespaces for project 'break'
$ bb test :project/break
```


### CVM

The CVM and other needed Java utilities are kept private at the moment. Manual installation is needed:

- Clone or fork the [Java repository](https://github.com/Convex-Dev/convex)
- Enter directory
- Ensure [Maven](https://maven.apache.org/) is installed
- Build: `$ mvn install`
- Jar is now located at `./target/convex.jar`
- Good idea to name `$COMMIT_HASH.jar` instead of `convex.jar`, keep track of what is being used
- Copy where relevant, make sure `deps.edn` file points to it


## Dev

Following directory structure, each project typically has a `dev_templ.clj` file in its Clojure dev files which requires useful namespaces.
This file can be copied in the same directory to `dev.clj` for hacking and trying thing out. Those `dev.clj` files are effectively private and will
not appear in this repository.

For example, see [`:project/all` dev directory](./project/all/src/clj/dev/convex/all).


## License

Copyright © 2021 Adam Helinski, the Convex Foundation, and contributors

Licensed under the Apache License, Version 2.0
