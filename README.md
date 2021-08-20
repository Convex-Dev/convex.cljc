# Advanced tooling for Convex Lisp and the CVM

This monorepo offers a variety of applications and libraries written in Clojure for working with the [Convex Virual Machine
and Convex Lisp](https://github.com/Convex-Dev/convex).

Overview of main folders in the [./project](./project) directory:

| Project | `deps.edn` alias | Purpose |
|---|---|---|
| [app/fuzz](./project/app/fuzz) | `:project/app.fuzz` | CLI multicore fuzzy tester, generates and tests random Convex Lisp forms | 
| [break](./project/break) | `:project/break` | Advanced generative test suite for the CVM ; prime example of testing Convex Lisp thoroughly |
| [clojurify](./project/clojurify) | `:project/clojurify` |Convex <-> Clojure data conversions, quick evaluation, useful `test.check` generators for testing |
| [cvm](./project/cvm) | `:project/cvm` | Handling Convex data and the CVM, low-level utilities |
| [run](./project/run) | `:project/run` | Convex Lisp Runner and REPL, advanced terminal environment |


## Structure

Each project follows a predictable structure:

- Project details are exposed in a dedicated README
- All source is located under the `./src` directory of each project or subproject
- Source is subdivided by language (eg. `clj`, `cvx`) and then by purpose (eg. `main`, `test`)
- All scripts and tasks are located and executed at the root of this repository
- All namespaces and source files are properly documented


## Conventions

The following conventions are enforced in READMEs and source files:

- Namespaces shorten `convex` into `$`: `convex.cvx` -> `$.cvm`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`


## Setup


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
