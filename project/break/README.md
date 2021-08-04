# `:project/break`

Advanced suite of generative tests targetting the CVM.

Builds upon [`:project/clojurify`](../clojurify) which provides utilities for quickly writing Convex Lisp as Clojure data and
evaluating such code for different scenarios. It also defines a series of useful [test.check](https://github.com/clojure/test.check)
generators used extensively in this project.

This great combination is showcased in all the [test files](./src/clj/test/convex/test/break) from this project. Those tests helped
uncovering dozens of issues and bugs in the CVM, contributing to making it particularly strong and robust, and serve as a prime example.

Generative assertions are structured using the [Mprop](https://github.com/helins/mprop.cljc) library. It is highly recommended for writing
such sophisticated tests fearlessly.


## Running tests

By default, they take several minutes depending on hardware.

From your terminal, at the root of this repository:

```bash
$ bb test:narrow :project/break
```
