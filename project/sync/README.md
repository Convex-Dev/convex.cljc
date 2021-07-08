# `:project/sync`

**Namespaces of interest:** `$.sync`

While [`:project/cvm`](../cvm) offers a low-level API for evaluating Convex Lisp, there exists a higher-level
concern for integrating source code from different sources and syncing everything into a context.

An obvious need is reading source files from disk and ending up with a context that has somehow evaluated them.

The `$.sync` namespace defines a set of abstract utilities for reading source snippets from arbitrary endpoints and combining
them into one context.

Ultimately, most users will only need the concrete implemetation of loading files from disk (without error-handling):

```clojure
(def ctx
     (-> ($.sync/disk {'$ "project/break/src/cvx/convex/break.cvx"})
         :convex.sync/ctx
         ($.clj.eval/ctx '(def $
                               (deploy (first $))))))


($.clj.eval/result* ctx
                    $/foo)
```

The above example specifies a map of `symbol` -> `path`. A map is returned which, in case of success, contains a prepared
context. Each specified source file is passed through the Convex Lisp reader and produces a CVM list of forms which is defined
under its requested symbol, as unevaluated code.

Further, using utilities from [`:project/clojurify`](../clojurify), are used to deploy the code as an actor and query the `foo`
value from it.

This example showcases the power of Lisp and having code as data: users can manipulate and evaluate source code exactly as
needed.

[`:project/watch`](../watch) goes further and builds on top of this project for providing a live-reloading experience.
[`:project/run`](../run) relies on both.
