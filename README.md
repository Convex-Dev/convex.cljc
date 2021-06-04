# convex.lisp.cljc

[![Clojars](https://img.shields.io/clojars/v/helins/convex.lisp.cljc.svg)](https://clojars.org/helins/convex.lisp.cljc)

[![Cljdoc](https://cljdoc.org/badge/helins/convex.lisp.cljc)](https://cljdoc.org/d/helins/convex.lisp.cljc)

Working with Convex Lisp and the CVM (Convex Virtual Machine) in Clojure.

This repository holds several interrelated projects and this document is only a brief overview. Namespaces are well documented and the user is
expected to explore them.

Attention, while those projects are getting more and more stable, unannonced breaking changes are still happening.


## Projects

Current examples are located in the [example directory](../main/src/example/convex/example). This document shows excerpts.

### convex.break

Extensive test suite targetting the CVM. The various generative tests have been helping in strenghtening the CVM and validating its design. They form a strong empirical proof
that the CVM is robust and that the Convex Lisp language is consistent.

Based on [test.check](https://github.com/clojure/test.check), they leverage the numerous generators found in the `convex.lisp` project.


### convex.cvm

Interface for using the CVM and evaluating Convex Lisp source.


##### Handling Convex Lisp code

Convex Lisp source code goes through 4 steps: reading, expanding, compiling, and executing. A CVM context is needed for handling them. The result of a step is either a valid result or a CVM exception. If needed, those Java objects can be converted to Clojure data for easy consumption.

A CVM exception is a special state which means that the CVM shortcuts execution, such as when a failure happens. Any JVM exception **MUST** be handled by the CVM otherwise
it should be reported as a bug.

First, an example of going through those 4 steps one by one:

```clojure
(let [;; It is convenient writing Convex Lisp as Clojure data
      form   '(+ 2 2)
      
      ;; Converting Clojure data to source code (a string)
      source (convex.lisp/src form)
      
      ;; Reading source code as Convex object
      code   (convex.cvm/read source)
      
      ;; Creating a test context
      ctx    (convex.cvm/ctx)
      
      ;; Using context for expanding, compiling, and running code
      ctx-2  (-> (convex.cvm/expand ctx
                                    code)
                 convex.cvm/compile
                 convex.cvm/run)]

  ;; Getting result and converting to Clojure data
  (-> ctx-2
      convex.cvm/result
      convex.cvm/as-clojure))
```

There are shortcuts and it is easy writing a helper function as needed. For instance, leveraging `eval`:

```clojure
(-> (convex.cvm/eval (convex.cvm/ctx)
                     (convex.cvm/read-form '(+ 2 2)))
    convex.cvm/result
    convex.cvm/as-clojure)
```

Often, a CVM context needs some preparation such as adding utility functions. Then, prior to using it, a cheap copy can be obtained by forking it, leaving the original
intact and reusable at will.

```clojure
;; Creating a new context, modifying it by adding a couple of functions in the environment
;;
(def base-ctx
     (convex.cvm.eval/ctx (convex.cvm/ctx)
                          '(do
                             (defn my-inc [x] (+ x 1))
                             (defn my-dec [x] (- x 1)))))



;; Later, forking and reusing it ad libidum
;;
(-> (convex.cvm/eval (convex.cvm/fork base-ctx)
                     (convex.cvm/read-form '(= 42
                                               (my-dec (my-inc 42)))))
    convex.cvm/result
    convex.cvm/as-clojure)
```

This pattern of forking and getting some value translated to Clojure data is so common that there are 2 namespaces providing shotcuts, depending on whether the user
is dealing with forms expressed as Clojure data or as text:

```clojure
(= 4
   (convex.cvm.eval/result base-ctx
                           '(my-dec (my-inc 42)))
   (convex.cvm.eval.src/result base-ctx
                               "(my-dec (my-inc 42))"))
```


##### Watching Convex Lisp files

Developping Convex Lisp interactively with a Clojure environment provides a uniquely productive flow akin to having a REPL.

The following starts a file watcher for all mentioned files which are re-imported as aliased libraries on each change. Derefencing the result provides a fresh context with
all required imports:

```clojure
;; Starting a watcher with a map of `file path` -> `alias`
;;
(def w*ctx
     ($.cvm/watch {"src/convex/break/util.cvx" '$}))

;; Using it as needed
;;
@w*ctx

;; Stopping the watcher
;;
(.close w*ctx)
```


### convex.lib

Official Convex libraries are maintained here. Each library has or will have a dev namespace and a test namespace using the same methods that are employed in the
`convex.break` project.


### convex.lisp

Convex Lisp and Clojure are so close that it is very convenient templating the former with the latter. This project provides CLJC utilities focusing on the language itself.


##### Templating Convex Lisp code

The following macro provides a templating experience close to Clojure's syntax quote by leveraging `~` and `~@`, unquoting and unquote-splicing.

Convex Lisp also uses those symbols. For avoiding confusing, symbols are used for injecting values from Clojure whereas the form notation (`(unquote x)` and `(unquote-splicing x)`) is left as such for Convex Lisp. Unlike the syntax quote, symbols are not qualified automatically since this is about templating Convex Lisp code.

For example:

```clojure
(let [kw :foo
      xs [2 3]
      y  42]
  (convex.lisp/templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing xs)]))
```

Produces the following vector:

```clojure
[:foo 1 2 3 4 42 y (unquote y) (unquote-splicing xs)]
```


##### Generating Convex Lisp forms

The `convex.lisp.gen` namespaces provides `test.check` generators for Convex Lisp forms and various related utilities. Those generators are heavily used in the `convex.break` and
`convex.lib` projects. Thus, the user is encouraged to use them as well when writing Convex Lisp code.



## Development and testing <a name="develop">

This repository is organized with [Babashka](https://github.com/babashka/babashka), a wonderful tool for any Clojurist.

All tasks can be listed by running:

```shell
$ bb tasks
```

For instance, a task starting a Clojure dev environment:

```shell
$ bb dev:clojure
```


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
