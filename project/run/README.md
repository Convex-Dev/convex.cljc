# Project 'Run'

**Namespaces of interest**: `$.run`

Built on top of [project 'Sync'](../sync) and [project 'Watch'](../watch), this project hosts the core implementation of the Convex Lisp Runner
written in [project 'app/run'](../app/run) which is a very light CLI interface. Go there for concepts and rationale. The 3 evaluation modes it
supports are directly accessible with functions `eval`, `load`, and `watch`, which can be reused for different purposes.

For instance:

```clojure
(def env
     ($.run/load {:convex.run.hook/out (fn [env x]
                                         (println x)
                                         (flush)
                                         env)}
                 "./path/to/file.cvx"))
```

Other functions and namespaces serve the implementation and are probably not of public interest but for studying the method.


## For maintainers

Namespaces are documented and pretty straightforward. Special requests are implemented as multimethods in `$.run.sreq`.


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
