# Project 'CVM'

The Convex Virtual Machine executes operations over state, as described in [CAD 005](https://github.com/Convex-Dev/design/blob/main/cad/005_cvmex/README.md).

This project offers a toolset running a CVM locally, purely in memory, without requiring any database or network setup. Ultimately, the goal is to execute
Convex Lisp and gain various insights.


## CVM types

**Namespaces of interest:** `$.code`

CVM types are described in [CAD 002] and consist of immutable Java objects.

The `$.code` namespace provides a set of functions for creating those objects from scratch, type predicate functions, and a few higher-level utilities:

```clojure
;; Creating a CVM vector.
;;
(def v
     ($.code/vector [($.code/address 42)
                     ($.code/long 24)
                     ($.code/keyword "foo")]))


;; Yes, it is a vector indeed
;;
($.code/vector? v)


;; Creating a `def` form for that vector
;;
(def cvm-dev
     ($.code/def ($.code/symbol "my-vector")
                 v))


;; It is only a form: `(def my vector [#42 24 :foo])` expressed in CVM objects
;;
($.code/list? cvm-dev)
```

Some types works with some aspects of Clojure. For instance, `first` can be applied to collections. Overall, it is safer and more effective to use the Java API
(#TODO Link when public).


## Reading Convex Lisp

**Namespaces of interest:** `$.cvm`

Reading is the process of parsing source code (a Java string) into a CVM list of CVM objects.

```clojure
(def form+
     ($.cvm/read "(inc 42) :foo"))


;; Two forms have been read.
;;
(= 2
   (count form+))


;; Nothing has been evaluated yet.
;;
($.code/list? (first form+))
```

Convex Lisp is so close to Clojure that it is sometimes convenient writing source as Clojure data. Especially during development and testing.

```clojure
($.cvm/read-clojure '(inc 42))
```

For more information about leveraging Clojure data for writing Convex Lisp, see [project 'clojurify'](../clojurify).


## Creating and handling a CVM context

**Namespaces of interest:** `$.cvm`

A CVM context holds the CVM state as well as extra information. It is needed for evaluating Convex Lisp code and doing anything useful.

```clojure
(def ctx
     ($.cvm/ctx))
```

Functions `ctx` -> `ctx` are common. While a context is mostly immutable, when using such functions, the input context **MUST** be discarded in favor
of the output context.

The only exception is forking which creates a cheap copy. Whatever happens with a copy as absolutely no effect on the original. It is commonly used for
preparing a "base" context that is then copied and reused in many situations.

```clojure
(def ctx-copy
     ($.cvm/fork ctx))
```


## Expand, compile, run (aka eval)

**Namespaces of interest:** `$.cvm`

Any computation ultimately relies on 3 steps. 

Expansion and compilation are explained in [CAD 008](https://github.com/Convex-Dev/design/blob/main/cad/008_compiler/README.md).
Only after compilation code can effectively be run.

Each step is a function `(ctx, form)` -> `ctx`. The output context holds either a result for the next step or a CVM exception in case
of error.

Evaluation condenses those 3 steps:

```clojure
(let [ctx-2 ($.cvm/eval ctx
                        ($.cvm/read "(+ 2 2)"))
      ex    ($.cvm/exception ctx-2)]
  (if ex
     ...error-handling
     ($.cvm/result ctx-2)))
```

Step-by-step with error handling would be structured similarly to:

```clojure
(let [ctx-expand ($.cvm/expand ctx
                               ($.cvm/read "(+ 2 2)"))
      ex-expand  ($.cvm/exception ctx-expand)]
  (if ex-expand
    ...error-handling
    (let [ctx-compile ($.cvm/run ctx-expand
                                 ($.cvm/result ctx-expand))
          ex-compile  ($.cvm/exception ctx-2)]
      (if ex-expand
        ...
        (let [ctx-run ($.cvm/run ctx-compile
                                 ($.cvm/result ctx-compile))
              ex-run  ($.cvm/exception ctx-run)]
          (if ex-run
             ...
             ($.cvm/result ctx-run)))))))
```

All those steps consumes juice as described in [CAD 007](https://github.com/Convex-Dev/design/blob/main/cad/007_juice/README.md).


## Getting insights and altering context properties

**Namespaces of interest:** `$.cvm`

The API offers utilities for retrieving and altering a variety of common properties from a context and its state: getting/setting juice,
timestamp, defining symbols in accounts, etc.

If not sufficient, the Java API offers wider possibilities.


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
