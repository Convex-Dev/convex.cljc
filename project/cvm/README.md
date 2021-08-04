# `:project/cvm`

The Convex Virtual Machine executes operations over state, as described in [CAD 005](https://github.com/Convex-Dev/design/blob/main/cad/005_cvmex/README.md).

It inputs cells and outputs cells, the word "cell" designating immutable Convex objects such as values, data structures, or functions.
[CAD 002](https://github.com/Convex-Dev/design/tree/main/cad/002_values) offers an overview of main data types.

This project provides utilities crafted around cells and the CVM:

| Namespace | Purpose |
|-----------|---------|
| `$.cell`  | Constructors and predicate functions for cells          |
| `$.cvm`   | CVM execution, manipulation, gathering various insights |
| `$.read`  | Parse text source into cells                            |
| `$.write` | Convert cells into text source                          |

Those namespaces are built on top of [`convex-core` in the core Java repository](https://github.com/Convex-Dev/convex) and provide
commonly needed features for building tools. Prime example is the Convex Lisp Runner from [:project/run](../run).


## Usage

Namespaces are well-documented. The following sections are but very brief overviews providing a sense of where this is going.


### Handling source and cells

Reading is the act of parsing source (strings, files, streams) into cells:

```clojure
(def source
     "[#42 24 :foo]")


(def v
     ($.read/string source))


($.cell/vector? v)  ;; True
```

While writing takes cells and produces source:

```clojure
(= source
   ($.write/string v))  ;; True
```

Especially when working on tooling, cells can be built from scratch:

```clojure
(= v
   ($.cell/vector [($.cell/address 42)
                   ($.cell/long 24)
                   ($.cell/keyword "foo")]))
```


### Execution

Cells can be executed using a CVM context, consuming juice in the process, as described in [CAD 007](https://github.com/Convex-Dev/design/tree/main/cad/007_juice).

```clojure
(def code
     ($.read/string "(+ 2 2)"))
```

Usually, evaluation is sufficient and it is the shorted path:

```clojure
(= ($.cell/long 4)

   (-> ($.cvm/eval ($.cvm/ctx)
                   code)
       $.cvm/result))
```

In reality, 3 steps are performed as described in [CAD 008](https://github.com/Convex-Dev/design/tree/main/cad/008_compiler).
Sometimes, it is useful performing them manually.

First, expansion which takes a cell and produces a canonical cell by applying macros, meaning it cannot be expanded any further.
[CAD 009](https://github.com/Convex-Dev/design/tree/main/cad/009_expanders) goes into greater details if needed.

Second, canonical cell is compiled into an operation.

And third, operation is actually executed.

```clojure
(= ($.cell/long 4)

   (-> ($.cvm/ctx)
       ($.cvm/expand code)
       $.cvm/compile
       $.cvm/exec
       $.cvm/result))
```

Any result of any steps can be retrieved by using `$.cvm/result` (done automatically in previous example). However, in practice,
a CVM exception is thrown in case of failure, meaning users should defensively check for errors using `$.cvm/exception` first.

Other utilities from the `$.cvm` namespace serve to gain insights from contextes (eg. track juice consumption) or alter them
(eg. set juice).
