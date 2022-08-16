# Maintaining this monorepo

This document is only useful for learning about the tooling used to maintain this monorepo.


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
