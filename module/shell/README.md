# `:module/shell`

**Convex Lisp Shell**

One-size fits all tool for sophisticated development, testing, and analysis of Convex Lisp in a fun
and highly productive environment. Meant for more advanced users already familiar with [Convex Lisp and
the Convex Virtual Machine](https://convex.world/cvm), wanting to prototype and build actual smart contracts.

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

Install the [latest
release](https://github.com/Convex-Dev/convex.cljc/releases/tag/run%2F0.0.0-alpha3)
on your system.

The native version is highly recommended, but a jar file is provided in case
your operating system is not supported.

Ensure the binary is executable on your system. Let us suppose it is aliased in
your terminal as `cvx`.

No arguments starts a basic REPL and Convex Lisp transactions can be entered
interactively:

    cvx

Transactions can be provided directly as command-line arguments:

    cvx '(def foo :hello)'  '($.stream/!.outln foo)'

In the grand tradition of Lisp languages, the runner is self-documented. REPL invites the user to query help
by running `($/help)` which takes the lead from there, informs about available features and how to query
more help for those features and everything else.


## Improved REPL experience

It is highly advised using [rlwrap](https://github.com/hanslub42/rlwrap) when working at the REPL since it
provides command history and navigation using arrow keys, among other features. Linux package managers typically
host this common program.

For instance, on Ubuntu:

    sudo apt install rlwrap

Start a comfortable REPL:

    rlwrap cvx


## Build

For building the runner, commands must be issued from the root of this repository and it is assumed tools
mentioned in the [general README](../../README.md) are available. 

First, an uberjar with direct-linking must be created:

    bb build :module/shell

Uberjar should now be located under `./private/target/shell.uber.jar`. It is usable with any JVM:

    java -jar ./private/target/shell.uber.jar

For native compilation, [GraalVM](https://www.graalvm.org/docs/getting-started/)
must be installed as well as its companion tool [Native
Image](https://www.graalvm.org/reference-manual/native-image/#install-native-image).

We recommend [SDKMan](https://sdkman.io) for easy installation of GraalVM tools.

Assuming everything is ready and the uberjar has been built:

    bb native:image :module/shell

After a few minutes of work and quite a bit of memory usage, the native binary
for your system will be available under `./private/target/shell`.
