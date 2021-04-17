# Convex.clj

[![Clojars](https://img.shields.io/clojars/v/helins/convex.lisp.cljc.svg)](https://clojars.org/helins/convex.lisp.cljc)

[![Cljdoc](https://cljdoc.org/badge/helins/convex.lisp.cljc)](https://cljdoc.org/d/helins/convex.lisp.cljc)

About working with Convex Lisp, from analysis to execution.


## Usage

Coming soon.


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
# Then open ./cljs/index.html
```


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
