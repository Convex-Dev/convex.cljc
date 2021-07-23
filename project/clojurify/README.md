# `:project/clojurify`

Convex Lisp is particularly close to Clojure on many aspects.

[`:project/cvm`](../cvm) exposes low-level features for executing Convex Lisp code via a CVM context and handling Convex data.
This project offers high-productivity utilities for producing Convex Lisp code from Clojure data structures. Those utilities
are especially useful during development and testing (eg. see [`:project/break`](../break)).


## Writing Convex Lisp as Clojure data structures

**Namespaces of interest:** `$.clj`

[CAD 002](https://github.com/Convex-Dev/design/blob/main/cad/002_values/README.md) describes all CVM values that belong
to Convex Lisp.

There is a direct mapping between Clojure and Convex Lisp for a subset of those types. Those types are converted to strings
which are both valid Clojure and valid Convex Lisp:

- Boolean
- Char
- Double
- List
- Long
- Map
- Set
- Vector
- Unqualitied symbol
- Unqualitied keyword

Warning: in Clojure, sequential collections with equal items are equal while Convex Lisp makes a distinction baed on type.
For instance, the following map has 2 key-values in Convex Lisp but only 1 in Clojure (the latter replaces the former):

```clojure
{[1]  :vector
 '(1) :list}
```

Other types do not have an obvious mapping. The API offers constructor functions those types which actually produce symbols.
Indeed, when converted to a string, a symbol looks exactly the same. However, those symbols also hold differentiating information
in their metadata:

```clojure
(def addr
     ($.clj/address 42))


(= addr
   (symbol "#42"))


(= 42
   ($.clj/meta-raw addr))


(= :address
   ($.clj/meta-type))
```


## Producing source

**Namespaces of interest:** `$.clj`, `$.read`

Revelant Clojure values can be converted to Convex Lisp and read back using the Convex Lisp reader:

```clojure
(-> '(+ 2 2)       ; Form written in Clojure
    $.clj/src      ; As string
    $.read/string  ; Read back as a Convex list of Convex data
    )
```


## Templating Convex Lisp

**Namespaces of interest:** `$.clj`

In Clojure, templating code with `~` and `~@` is a powerful feature. However, it is unsuitable for producing Convex Lisp since symbols
are automatically namespaced.

The following macro provides an almost exact experience for templating forms without qualifying symbols:

```clojure
(let [kw :foo
      xs [2 3]
      y  42]
  ($.clj/templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing xs)]))

;; [:foo 1 2 3 4 42 y (unquote y) (unquote-splicing xs)]
```

Notice how `unquote` and `unquote-splicing`, written in plain text, remain intact. This is because Convex Lisp has templating as well.
For avoiding confusion, when using this macro, only `~` and `~@` are actually processed. The rest is in the realm of Convex Lisp.


## Shortcuts for evaluation

**Namespaces of interest:** `$.clj.eval`, `$.cvm`

As empirically proven, the above utilities are most useful during development and evaluation. Functions for common evaluation patterns
have been prepared in order to make it particularly easy, especially when writing sophisticated tests. Similar macros have been added
which automatically provides the templating experience described in the previous section.

Results and CVM exceptions are also automatically converted to Clojure data for easy consumption.

```clojure
;; Directly evaluating to a result.
;;
(let [x 42]
  (= 46
     ($.cvm.eval/result ($.cvm/ctx)
                        (list '+ x 4))))


;; Same but using templating
;;
(let [x 42]
  (= 46
     ($.cvm.eval/result* ($.cvm/ctx)
                         (+ ~x 4))))
```

Any of those functions always fork the given context. See `$.cvm/fork` from [`:project/cvm`](../cvm).

Here is a common scenario of preparing a base context and then easily reusing it, as often seen in tests, knowing
forks will not affect the original:

```clojure
;; Templating a definition for `x` and getting back a new context
;;
(def ctx
     (let [x 42]
       ($.cvm.eval/ctx* ($.cvm/ctx)
                        (do
                          (def x
                               ~x)))))


;; Not fearing that `ctx` will be modified in any way
;;
(= 46
   ($.cvm.eval/result ctx
                      '(+ x 4)))
```


## Setting a default context for development

**Namespaces of interest:** `$.clj.eval`

When working at the REPL or during some test situations, there is often a need for having a prepared context at hand. A default context
can be set under `$.clj.eval/*ctx-default*` with:

```clojure
($.clj.eval/alter-ctx-default some-ctx)

```

All functions and macros from the `$.clj.eval` namespace use that default context when none is provided. This var is also dynamic, hence
the following is possible:

```clojure
(binding [$.clj.eval/*ctx-default* some-ctx]
  ($.clj.eval/result '(+ 2 2)))
```


## Generating random forms

**Namespaces of interest:** `$.clj.gen`

Generative tests using [test.check](https://github.com/clojure/test.check) offer a highly productive and robust way of findings bugs in
Convex Lisp and smart contracts, as described in [`:project/break`](../break) which should be studied for learning about such methods.. 

Common useful generators are defined in the `$.clj.gen` namespaces and are a great match for the `$.clj.eval` namespace described above and
the templating utilities.
