# convex.lisp.cljc

[![Clojars](https://img.shields.io/clojars/v/helins/convex.lisp.cljc.svg)](https://clojars.org/helins/convex.lisp.cljc)

[![Cljdoc](https://cljdoc.org/badge/helins/convex.lisp.cljc)](https://cljdoc.org/d/helins/convex.lisp.cljc)

Working with Convex Lisp and the CVM (Convex Virtual Machine) in Clojure.

This repository holds a diversity of tools and libraries, this document being only an overview.

Namespaces are well documented and the user is expected to explore them.

Current examples are located in the [example directory](../main/src/clojure/example/convex/example).

For brievety and consistency with the source, when mentioning a namespace, `convex` is replaced with `$` such as: `convex.cvm` -> `$.cvm`. 

**Attention, while those projects are getting more and more stable, unannonced breaking changes are still happening.**


## Interacting with the CVM

**Namespaces of interest:** `$.cvm`.

A context represents the interface of the CVM. It holds the current state as well as an executing account under which
all operations are performed. Each operation consumes some amount of `juice`. This namespace provides utilities for creating contextes and interacting
with them in many ways. Any new endaveour mostlikely starts with:

```clojure
(def ctx
     ($.cvm/ctx))
```


## Writing Convex Lisp code programmatically

**Namespaces of interest:** `$.code`, `$.clj`

The CVM handles Java objects that essentially represent Convex Lisp. The `$.code` namespace offers a series of functions for creating such objects. It is
mostly useful for writing advanced applications.

More often, especially during development and testing, the user will want to leverage the fact that Convex Lisp is so close to Clojure itself. The `$.clj`
namespace helps in writing Convex Lisp code directly as Clojure data:

```clojure
(= "(+ 2 2)"
   ($.clj/src '(+ 2 2)))

(= "#42"
   ($.clj/src ($.clj/address 42)))
```

In Clojure, templating code with `~` and `~@` is a powerful feature. The following macro provides an almost exact experience for templating Convex Lisp:

```clojure
(let [kw :foo
      xs [2 3]
      y  42]
  ($.clj/templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing xs)]))

;; [:foo 1 2 3 4 42 y (unquote y) (unquote-splicing xs)]
```

Notice how `unquote` and `unquote-splicing`, written in plain text, remain intact. This is because Convex Lisp has templating as well. For avoiding confusion,
when using this macro, only `~` and `~@` are actually processed. The rest is in the realm of Convex Lisp.


## Evaluating Convex Lisp

**Namespaces of interest:** `$.clj.eval`, `$.cvm`

Armed with a context, Convex Lisp code goes through 4 operations before ultimately producing a result: read, expand, compile, and execute. Besides producing a result,
the CVM can also enter in an exceptional state when an error occurs or when using features such as `return`. Note that the CVM **MUST** never throw an actual Java
exception. This would be considered a bug and shall be reported.

First, for thorough understanding, going through all 4 operations one by one:

```clojure
(let [;; It is convenient writing Convex Lisp as Clojure data
      form   '(+ 2 2)
      
      ;; Converting Clojure data to source code (a string)
      source ($.clj/src form)
      
      ;; Reading source code as Convex object
      code   ($.cvm/read source)
      
      ;; Creating a test context
      ctx    ($.cvm/ctx)
      
      ;; Using context for expanding, compiling, and running code
      ctx-2  (-> ($.cvm/expand ctx
                               code)
                 $.cvm/compile
                 $.cvm/run)]

  ;; Getting result and converting to Clojure data
  (-> ctx-2
      $.cvm/result
      $.cvm/as-clojure))
```

In practice, evaluating is more commonly used instead of going through all steps:

```clojure
(-> ($.cvm/eval ($.cvm/ctx)
                ($.cvm/read-form '(+ 2 2)))
    $.cvm/result
    $.cvm/as-clojure)
```

During development and testing, evaluating code written in Clojure for various purposes is so common that `$.clj.eval` is solely dedicated to that purpose:

```clojure
($.clj.eval/result ($.cvm/ctx)
                   '(+ 2 42))

;; 44
```

Each function has a macro variant that uses the `$.clj/templ*` macro under the hood so that the user can simply:

```clojure
(let [foo 42]
  ($.clj.eval/result* ($.cvm/ctx)
                      (+ 2 ~foo)))

;; 44
```


## Preparing a context

**Namespaces of interest:** `$.cvm`

Often, a CVM context needs some preparation such as adding utility functions. Then, prior to using it, a cheap copy can be obtained by forking it, leaving the original
intact and reusable at will.

```clojure
;; Creating a new context, modifying it by adding a couple of functions in the environment
;;
(def base-ctx
     ($.clj.eval/ctx ($.cvm/ctx)
                     '(do
                        (defn my-inc [x] (+ x 1))
                        (defn my-dec [x] (- x 1)))))


;; Later, forking and reusing it ad libidum
;;
($.cvm/fork base-ctx)


;; All functions from `$.clj.eval` fork the given context by default, no need to do it manually
;;
($.clj.eval/result base-ctx
                   '(= 42
                       (my-dec (my-inc 42))))
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


## Loading Convex Lisp files into a context

**Namespaces of interest:** `$.clj.eval`, `$.sync`

Writing Convex Lisp code as Clojure or in any programmatic way is convenient during development and testing. However, any non-trivial source should be written in proper
`.cvx` files.

In the following example, starting from a new context, each file is read and interned under its related symbol as unevaluated code. Then, through the power of Lisp,
all this code can be handled exactly as needed once the context is ready. Often, `eval` or `deploy` are involved.

Without error checking:

```clojure
(def ctx
     (-> ($.sync/disk {'$ "src/convex/break/util.cvx"})
         :convex.sync/ctx
         ($.clj.eval/ctx '(def $
                               (deploy $)))))


($.clj.eval/result* ctx
                    $/foo)
```


## Live-reloading Convex Lisp files

**Namespaces of interest:** `$.clj.eval`, `$.watch`

Akin to the previous section, files can be loaded into a context and also live-reloaded. In other words, the context can be keep in sync everytime a file
is changed.

A watcher is effectively a Clojure agent but the API makes that transparent. The agent itself is exposed so that expert user can build more complex features.

In essence:

```clojure
(def a*env
     (-> ($.watch/init {:on-change (fn [env]
                                     (update env
                                             :ctx
                                             $.clj.eval/ctx
                                             '(def $
                                                   (deploy $))))
                        :sym->dep  {'$ "src/convex/break/util.cvx"}})
         $.watch/start))


($.clj.eval/result* ($.watch/ctx a*env)
                    $/foo)


($.watch/stop a*env)
```


## Testing Convex Lisp code

**Namespaces of interest:** `$.clj.eval`, `$.clj.gen`, `$.disk`

Reusing Clojure tooling provides a productive experience for testing Convex Lisp. The plethora of tests in the `$.break.test.*` namespaces provide a good example.

Generative testing with [test.check](https://github.com/clojure/test.check) is highly recommended, especially when testing smart contracts or Convex libraries that
need to be extremely secure. For that effect, the `$.clj.gen` namespace offers a variety of generators for producing random Convex Lisp forms. Combined with the
`$.clj/templ*` macro and the `$.clj.eval` namespace, powerful tests are easily written.

For organizing generative tests, we recommend the [Mprop](https://github.com/helins/mprop.cljc) library, used extensively in this repository.


## Official Convex libraries

The official Convex libraries are maintained and incubated in this repository. Ultimately, each will have an extensive generative test suite
under a dedicated test namespace. For development, each has or will have a dedicated dev namespace as well.

For example, the Trust library is developped in the `convex.lib.trust` namespace and tested in the `convex.lib.test.trust` namespace.

For starting a new one, please follow the following structure: development in `convex.lib.lab.NAME` and testing in `convex.lib.test.lab.NAME`.


## Repository organization

This repository is organized around [Clojure Deps](https://clojure.org/guides/deps_and_cli). All tasks are written in Clojure and executed with [Babashka](https://github.com/babashka/babashka) instead of plain ol' Bash.

For listing tasks:

```bash
$ bb tasks
```

For instance, executing the `dev:clojure` task which starts a REPL and require the `$.dev` namespace:

```bash
$ bb dev:clojure
```

All Convex source is located in `./src/convex` while all Clojure source is located in `./src/clojure`.


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
