# convex.lisp.cljc

[![Clojars](https://img.shields.io/clojars/v/helins/convex.lisp.cljc.svg)](https://clojars.org/helins/convex.lisp.cljc)

[![Cljdoc](https://cljdoc.org/badge/helins/convex.lisp.cljc)](https://cljdoc.org/d/helins/convex.lisp.cljc)

Working with Convex Lisp and the CVM (Convex Virtual Machine).

Toolset consist of:

- Reading, expanding, compiling, and executing Convex Lisp code from the JVM
- Translating between Convex object and Clojure data structures
- Templating Convex Lisp code from Clojure
- Specyfing Convex Lisp using Malli, providing:
    - Some basic level of validation
    - Generation of random forms
- Writing generative tests against the CVM


## Usage

The toolset already provides quite a lot of useful features for interacting with Convex Lisp. Currently, the API is unstable and unannounced breaking changes are to be expected.

More examples will follow as the API stabilizes.

Current examples are located in the [example directory](../main/src/example/convex/example).

Requiring namespaces for current examples:

```clojure
(require 'convex.cvm           ;; Expanding, compiling, and executing code on the CVM
         'convex.cvm.eval      ;; Helpers for quickly evaluating forms (dev + tests)
         'convex.cvm.eval.src  ;; Ditto, but when working with source strings
         'convex.lisp          ;; Using Clojure for writing Convex Lisp
         'convex.lisp.schema)  ;; Malli schemas for Convex Lisp
```

### Handling Convex Lisp code

Convex Lisp source code goes through 4 steps: reading, expanding, compiling, and executing. A CVM context is needed for handling such operations. The result of an operation is either a valid result or a handled error. There should never be an unhandled exception coming from the CVM.

Going through the whole cycle (context could be reused for further execution):

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

Often, a CVM context needs some preparation such as adding utility functions. It is a good idea forking the context when used so that any work is done on cheap copies, leaving the original context intact in order to be reused at will.

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

This pattern of forking and getting some value translated in Clojure data is so common that there are 2 namespaces providing shotcuts:

```clojure
(= 4
   (convex.cvm.eval/result base-ctx
                           '(my-dec (my-inc 42)))
   (convex.cvm.eval.src/result base-ctx
                               "(my-dec (my-inc 42))"))
```


### Templating Convex Lisp code

It is particularly convenient writing Convex Lisp code as Clojure data which is easily manipulated.

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


### Validating and generating Convex Lisp

The fact that Convex Lisp can be written as Clojure data means we can leverage the [Malli](https://github.com/metosin/malli) library for describing the language:

```clojure
(require '[malli.generator :as malli.gen])


(def registry

  "Malli registry containing everything that is needed."

  (convex.lisp.schema/registry))


(malli.gen/generate :convex/vector
                    {:registry registry
                     :size     5})
```

Generative tests targeting the CVM extensively relies on such a registry.

Currently, Malli struggles with recursive data definitions. This is why the size in this example is very slow. It is in the process of being fixed.

## Testing the CVM

For the time being, this repository bundles both general-purpose utilities for Convex Lisp
as well as an advanced test suite for the CVM. In the future, both will be split.

This suite consists of generative tests which are routinely being added and improved, all contributions welcome.


## Tasks and development

All useful tasks for development and testing are accessible via [Babashka](https://github.com/babashka/babashka). After a quick and easy installation, tasks can be listed with:

```bash
$ bb tasks
```

Launching a task such as starting NREPL for development on the JVM:

```bash
bb dev:clojure
```


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
