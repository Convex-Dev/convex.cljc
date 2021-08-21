# `:project/run`

[![Clojars](https://img.shields.io/clojars/v/world.convex/run.clj.svg)](https://clojars.org/world.convex/run.clj)
[![cljdoc](https://cljdoc.org/badge/world.convex/run.clj)](https://cljdoc.org/d/world.convex/cvm.clj/CURRENT)
[Lastest downloads](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha1)

Convex Lisp Runner.

One-size fits all tool for sophisticated development, testing, and analysis of Convex Lisp in a fun
and highly productive environment.

Executes transactions provided as command-line arguments. If none is given, starts the Convex Lisp REPL where
user can work interactively.

Transactions are executed successively and deterministically by the CVM. However, a given transaction can return a
*request* for performing operations beyond the scope of the CVM. A plethora of CVX libraries is embedded
in the runner, building on those supported requests for providing features like file IO, time-travel, exception
handling, etc.

As a result, Convex Lisp becomes more like a scripting language with advanced metaprogramming and pushes the
boundaries of smart contract development to unforeseen heights with very fast feedback.

In the future, the runner will also integrate client capabilities, allowing for scripted or dynamic interactions
with networks of peers.


## Usage

Install latest release on your system.

Native version is highly recommended, a jar file is provided in case your operating system is not supported.

Assuming binary is executable and available on your path as `cvx`:

```bash
# No arguments starts the REPL and transactions can be entered interactively

$ cvx

# Transactions can be provided directly as command-line arguments

$ cvx '(def foo :hello)'  '($.stream/out! foo)'
```

In the grand tradition of Lisp languages, the runner is self-documented. REPL invites the user to query help
by running `($/help)` which takes the lead from there, informs about available features and how to query
more help for those features and everything else.


## Improved REPL experience

It is highly advised using [rlwrap](https://github.com/hanslub42/rlwrap) when working at the REPL since it
provides command history and navigation using arrow keys, among other features. Linux package managers typically
host this common program.

For instance, on Ubuntu:

```bash
$ sudo apt install rlwrap

# Starts a comfortable REPL

$ rlwrap cvx
```


## Build

For building the runner, commands must be issued from the root of this repository and it is assumed tools
mentioned in the [general README](../../README.md) are available. 

First, an uberjar with direct-linking must be created:

```bash
$ bb uberjar:direct :project/run
```

Uberjar should now be located under `./build/uberjar/project/run.jar`. It is usable with any JVM:

```bash
$ java -jar ./build/uberjar/project/run.jar
```

For native compilation, [GraalVM](https://www.graalvm.org/docs/getting-started/) must be installed as well
as its companion tool [Native Image](https://www.graalvm.org/reference-manual/native-image/#install-native-image).

We recommend [SDKMan](https://sdkman.io/) for easy installation of GraalVM tools.

Assuming everything is ready and the uberjar has been built:

```bash
$ bb native:image :project/jar
```

After a few minutes of work and lots of memory usage, native binary for your system will be available under
`./build/native/project/run`.
