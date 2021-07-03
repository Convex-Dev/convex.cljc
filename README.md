# Advanced Clojure toolset for the CVM and Convex Lisp

This repository offers a wide variety of utilities for working with the Convex Virual Machine and Convex Lisp: CLI applications,
libraries, and official Convex Lisp assets (actors and libraries meant to be deployed on chain).

See this [overview](./project).


## Structure

These utilities are divided, as seen in the [./project](./project) directory which lists everything that is currently
available. Albeit numerous, each project and subproject follows a predictable directory structure that should be straightforward
to follow:

- Project details are exposed in a dedicated README
- All source is located under the `./src` directory of each project/subproject
- Source is subdivided by language (eg. `clj`, `cvx`) and then by purpose (eg. `main`, `test`)
- All scripts and tasks are located and executed at the root of this repository
- All namespaces and source files are documented, which, considering READMEs, should help building a clear understanding


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

Printed lists shows all current tasks available for managing this repository: starting dev mode, running some tests, compiling, etc.


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
