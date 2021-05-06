# convex.lisp.cljc

[![Clojars](https://img.shields.io/clojars/v/helins/convex.lisp.cljc.svg)](https://clojars.org/helins/convex.lisp.cljc)

[![Cljdoc](https://cljdoc.org/badge/helins/convex.lisp.cljc)](https://cljdoc.org/d/helins/convex.lisp.cljc)

Working with Convex Lisp and testing the CVM (Convex Virtual Machine).

Toolset consist of:

- Reading, expanding, compiling, and executing Convex Lisp code
- Translating between Convex object and Clojure data structures
- Describing Convex Lisp code as Clojure forms
- Specyfing Convex Lisp using Malli, providing:
    - Some basic level of validation
    - Generation of random forms
- Writing generative tests against the CVM


## Usage

Coming soon.


## Testing the CVM

Current strategy consists of ensuring first that the very basic building blocks
of transactions and smart contracts work perfectly. It entails using generative
testing for proving that the core namespace and core language utilities work as
intended and fail as intended.

Second step will be about reusing what has been previously built for generating
common types of multi-form transactions. The purpose is to prove that the core
utilities interact with each other as intended. A simple example would be a
transaction where an account is created, some random amount of coin is sent to
that account, and balance as well as *balance* are used to reflected that all
involved accounts were indeed balanced out. Another simple example would be the
fact that get-holding should reflect set-holding. Once again, proving graceful
failure is just as important.

Third step will happen when the CVM has been proven resilient and robust. Only
then network tests can be envisioned. Loosely, the purpose will be to setup a
test network of peers and some number of automated client generating random
transactions. Never should peers diverge. If they do, it means that something
is wrong at the consensus/protocol level probably since the execution itself
has been proven flawless. Generating those transaction will leverage previous
work while the network part could possibly leverage [Jepsen](https://github.com/jepsen-io/jepsen),
a notorious Clojure tool used for analyzing and breaking distributed systems.


## Running tests

Depending on hardware, tests usually takes a few minutes to run.

On the JVM, using [Kaocha](https://github.com/lambdaisland/kaocha):

```bash
$ ./bin/test/jvm/run
```
On NodeJS, using [Shadow-CLJS](https://github.com/thheller/shadow-cljs):

```bash
$ ./bin/test/node/run

# Or testing an advanced build:
$ ./bin/test/node/advanced
```


## Development

Starting in Clojure JVM mode, mentioning an additional Deps alias (here, a local
setup of NREPL):
```bash
$ ./bin/dev/clojure :nrepl
```

Starting in CLJS mode using [Shadow-CLJS](https://github.com/thheller/shadow-cljs):
```bash
$ ./bin/dev/cljs
```


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
