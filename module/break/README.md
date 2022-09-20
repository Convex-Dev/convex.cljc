# `:module/break`

Advanced suite of generative tests targeting the Convex Virtual Machine.

Builds on utilities from [`:module/cvm`](../cvm) and
[test.check](https://github.com/clojure/test.check) generators from
[`:module/gen`](../gen).

This great combination is showcased in all the [test
files](./src/test/clj/convex/test/break) from this module. Those tests helped
uncovering dozens of issues and bugs in the CVM, contributing to making it
particularly strong and robust, and serve as a prime example of advanced testing
of Convex Lisp.

Generative assertions are structured using the
[Mprop](https://github.com/helins/mprop.cljc) library. It is highly recommended
for writing such sophisticated tests fearlessly.


## Running tests

By default, they take about a minute to run on modern hardware.

From your terminal, at the root of this repository:

    clojure -M$( bb aliases:test :module/break )

The maximum size of generators can be set using the `MPROP_MAX_SIZE` environment
variable (default is 200) while the number of tests per case can be set with
`MPROP_NUM_TESTS` (default is 100). For instance:


    env MPROP_MAX_SIZE=400 MPROP_NUM_TESTS=300 clojure -M$( bb aliases:test :module/break )
