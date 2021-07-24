# `:project/cvm`

The Convex Virtual Machine executes operations over state, as described in [CAD 005](https://github.com/Convex-Dev/design/blob/main/cad/005_cvmex/README.md).

This project offers a toolset running a CVM locally, purely in memory, without requiring any database or network setup. Ultimately, the goal is to execute
Convex Lisp and gain various insights.


## Convex data

**Namespaces of interest:** `$.data`

CVM types are described in [CAD 002] and consist of immutable Java objects.

The `$.data` namespace provides a set of functions for creating those objects from scratch, type predicate functions, and a few higher-level utilities:

```clojure
;; Creating a CVM vector.
;;
(def v
     ($.data/vector [($.data/address 42)
                     ($.data/long 24)
                     ($.data/keyword "foo")]))


;; Yes, it is a vector indeed
;;
($.data/vector? v)


;; Creating a `def` form for that vector
;;
(def def-form
     ($.data/def ($.data/symbol "my-vector")
                 v))


;; It is only a form: `(def my vector [#42 24 :foo])` expressed in CVM objects
;;
($.data/list? def-form)
```

Some types works with some aspects of Clojure. For instance, `first` can be applied to collections. Overall, it is safer and more effective to use the Java API
(#TODO Link when public).


## Reading Convex Lisp

**Namespaces of interest:** `$.read`

Reading is the process of parsing source code (text) into a Convex list of Convex data.

```clojure
(def form+
     ($.read/string+ "(inc 42) :foo"))


;; Two forms have been read.
;;
(= 2
   (count form+))


;; Nothing has been evaluated yet.
;;
($.data/list? (first form+))
```

The `$.read` namespace provides functions for reading source from strings, files, and others means.


## Creating and handling a CVM context

**Namespaces of interest:** `$.cvm`

A CVM context holds the CVM state as well as extra information. It is needed for evaluating Convex Lisp code and doing anything useful.

```clojure
(def ctx
     ($.cvm/ctx))
```

Functions `ctx` -> `ctx` are common in the `$.cvm` namespace. While a context is mostly immutable, when using such functions, the input context **MUST**
be discarded in favor of the output context.

The only exception is forking which creates a cheap copy. Whatever happens with a copy has absolutely no effect on the original. It is commonly used for
preparing a "base" context that is then copied and reused in many situations.

```clojure
(def ctx-copy
     ($.cvm/fork ctx))
```


## Expand, compile, exec (aka eval)

**Namespaces of interest:** `$.cvm`

Any computation ultimately relies on 3 steps. 

Expansion and compilation are explained in [CAD 008](https://github.com/Convex-Dev/design/blob/main/cad/008_compiler/README.md).
Only after compilation code can effectively be executed

Each step is a function `(ctx, form)` -> `ctx`. The output context holds either a result for the next step or a CVM exception in case
of error.

Evaluation condenses those 3 steps:

```clojure
(let [ctx-2 ($.cvm/eval ctx
                        ($.read/string "(+ 2 2)"))
      ex    ($.cvm/exception ctx-2)]
  (if ex
     :error-handling
     ($.cvm/result ctx-2)))
```

Step-by-step with error handling would be structured similarly to:

```clojure
(let [ctx-expand ($.cvm/expand ctx
                               ($.read/string "(+ 2 2)"))
      ex-expand  ($.cvm/exception ctx-expand)]
  (if ex-expand
    :error-handling
    (let [ctx-compile ($.cvm/exec ctx-expand
                                  ($.cvm/result ctx-expand))
          ex-compile  ($.cvm/exception ctx-2)]
      (if ex-expand
        :error-handling
        (let [ctx-exec ($.cvm/exec ctx-compile
                                   ($.cvm/result ctx-compile))
              ex-exec  ($.cvm/exception ctx-exec)]
          (if ex-exec
             :error-handling
             ($.cvm/result ctx-exec)))))))
```

Hence, user can have full control over those steps, and when something has been compiled, the results can be reused at will.

All those steps consume juice as described in [CAD 007](https://github.com/Convex-Dev/design/blob/main/cad/007_juice/README.md).


## Getting insights and altering context properties

**Namespaces of interest:** `$.cvm`

The API offers utilities for retrieving and altering a variety of common properties from a context and its state: getting/setting juice,
timestamp, defining symbols in accounts, etc.

It has been designed to fullfill common use cases. If not sufficient, the Java API offers wider possibilities.
