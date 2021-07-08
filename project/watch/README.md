# `:project/watch`

**Namespaces of interest:** `$.watch`

Built on top of [`:project/sync`](../sync), this projects provides a live-reloading experience at the REPL.

Anytime a dependency is modified on disk, it is reloaded and a new, updated context is created.

A watcher is effectively a Clojure agent but the API makes that transparent. The agent itself is exposed so
that expert user can build more complex features.

In essence:

```clojure
(def a*env
     (-> ($.watch/init {:convex.watch/on-change (fn [env]
                                                  (update env
                                                          :convex.sync/ctx
                                                          $.clj.eval/ctx
                                                          '(def $
                                                                (deploy (first $)))))
                        :convex.watch/sym->dep  {'$ "project/break/src/cvx/main/convex/break.cvx"}})
         $.watch/start))


($.clj.eval/result* ($.watch/ctx a*env)
                    $/foo)


($.watch/stop a*env)
```

The above example live-reloads only one file and ressembles the example in [`:project/sync`](../sync).
