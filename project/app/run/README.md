# Project 'app/run'

**Namespaces of interest**: `$.app.run`

Convex Lisp Runner. CLI interface built on top of [project 'run'](../../run) for executing Convex Lisp from the terminal.

Offers a highly-productive environment for writing, developping, testing, or scripting with Convex Lisp from the comfort
of your own editor. Depencency management included.

Each form is evaluated as a transaction. Evaluation stops whenever an unhandled error or CVM exception occurs.


## Usage

Printing help:

```clojure
($.app.run/-main)
```

Printing more help for a command:

```clojure
($.app.run/-main "command" "eval")
```

Describing in human language an account or a symbol:

```clojure
($.app.run/-main "describe" "#8")

($.app.run/-main "describe" "#8" "+")
```

Evaluating a given string:

```clojure
($.app.run/-main "eval" "(sreq/out (+ 2 2)))
```

Loading and executing a file:

```clojure
($.app.run/-main "load" "project/run/src/cvx/dev/convex/run/dev.cvx")
```

Same as previous, but provides live-reloading:
```clojure
(def a*
     ($.app.run/-main "watch" "project/run/src/cvx/dev/convex/run/dev.cvx"))

;; For stopping:
;;
(do
    (require '[convex.watch])
    (convex.watch/stop a*))
```


## Live-reloading

The experience ressembles more a notebook than a REPL. Whenever the main file or any or its depedencies change on disk, the modified files are
reloaded and the whole is re-executed from scratch. It provides a very fast feedback like the REPL while being easily reproducible like a
notebook.


## Special requests

The runner checks each result in case it might be a "special request": a vector which requests some special operation, typically a side-effect
(eg. outputting a value).

Convex Lisp functions which builds such vectors are located in a library deployed and aliased by default as `sreq`.

For more information:

```clojure
($.app.run/-main "describe" "sreq")
```

This [example file](./../../run/src/cvx/example/convex/run/example.cvx) demonstrates all supported special requests. User is invited to gradually
remove comments, run or watch the file and see what happens.


## Help library and special dynamic values

The Help library is deployed and aliased by default as `help`. In that account, the runner maintains a set of dynamic values which might be useful
to the user: juice consumed by the last transaction, result of the last transaction, current transaction number, etc.

For more information:

```clojure
($.app.run/-main "describe" "help")
```

This account is also meant to host helper utilities, albeit scarce at the moment. For instance, the `describe` CLI command actually delegate to a Convex
Lisp function. In Convex Lisp, one can run:

```clojure
(help/about sreq 'out)
```


## License

Currently unlicensed.

Copyright © 2021 Adam Helinski
