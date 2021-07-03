# Advanced Clojure toolset for the CVM and Convex Lisp

This repository offers a wide variety of utilities for working with the Convex Virual Machine and Convex Lisp: CLI applications,
libraries, and official Convex Lisp assets (actors and libraries meant to be deployed on chain).

Overview of the [./project](./project) directory:

| Project | Purpose |
|---|---|
| [app/fuzz](./project/app/fuzz) | CLI multicore fuzzy tester, generates random Convex Lisp form | 
| [app/run](./project/app/run) | CLI Convex Lisp runner |
| [break](./project/break) | Advanced generative test suite for the CVM ; prime example of testing Convex Lisp thoroughly |
| [clojuriy](./project/clojurify) | Quickly writing and evaluating Convex Lisp as Clojure data ; useful `test.check` generators |
| [cvm](./project/cvm) | Handling a CVM context: low-level utilities, evaluating Convex Lisp, gaining insights |
| [deploy](./project/deploy) | Experimental and stable Convex Lisp actors and libraries officially supported by the Convex Foundation |
| [dev](./project/dev) | Environment for running this reposity in dev mode |
| [run](./project/run) | Core implementation of [app/run](./app/run) ; reusable for similar purposes |
| [sync](./project/sync) | Framework for loading and executing Convex Lisp snippets into a a single context ; executing files from disk |
| [watch](./project/watch) | Live-reloading of Convex Lisp files |

Recommended order for building a good understanding:

- [cvm](./project/cvm), for understanding how to handle a CVM context
- [clojurify](./project/clojurify), for quickly evaluating Convex Lisp during dev and when writing tests
- [break](./project/break), for seeing thorough examples of testing Convex Lisp with [test.check](https://github.com/clojure/test.check)
- [deploy](./project/deploy), for seeing more complex examples of Convex Lisp actors and libraries, associated development and testing

If interested in the Convex Lisp Runner:

- [sync](./project/sync), about syncing code snippets into one CVM context, loading files from disk
- [watch](./project/watch), about providing a live-reloading experience on top of [sync](./sync)
- [run](./project/run), about merging [sync](./sync) and [watch](./watch)
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

The following conventions are enforced in examples and source files:

- Namespaces shorten `convex` into `$`: `convex.app.run` -> `$.app.run`
- Symbols referring to collections are pluralized with `+` at the end: `items` -> `item+`


## Setup

Currently, none of it has been released publicly. User is expected to fork this repository and use what is needed.


### Clojure Deps

This repository rely on the [Clojure command line tools](https://clojure.org/guides/getting_started). Familiarity with
[Clojure Deps](https://clojure.org/guides/deps_and_cli) is required.


### Babashka and tasks

All scripting is done using [Babashka](https://book.babashka.org/), a fast Clojure interpreter that comes with a powerful task runner.
Follow this [simple installation process](https://book.babashka.org/#_installation).

All tasks are written in [./bb.edn](./bb.edn) and can by listed by running in your shell:

```bash
bb tasks
```

Printed list shows all current tasks available for managing this repository: starting dev mode, running some tests, compiling, etc.


### CVM

The CVM and other core utilities are kept private at the moment. Manual installation is needed:

- Clone or fork the [Java repository](https://github.com/Convex-Dev/convex)
- Enter directory
- Ensure [Maven](https://maven.apache.org/) is installed
- Build: `$ mvn install`
- Jar is now located at `./target/convex.jar`
- Good idea to name `$COMMIT_HASH.jar` instead of `convex.jar`, keep track of what is being used
- Copy where relevant, make sure `deps.edn` file points to it


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
