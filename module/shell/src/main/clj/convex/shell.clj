(ns convex.shell

  "CONVEX SHELL

   This is a whole application. It is available as a library in case it needs to be embedded. Then, only [[-main]] is really
   useful. It must be called only one at a time per thread otherwise Etch utilities may greatly misbehave.

   Executes each form as a transaction, moving from transaction to transaction.

   A transaction can return a request to perform operations beyond the scope of the CVM, such as file IO or
   advancing time. Those requests turn Convex Lisp, a somewhat limited and fully deterministic language, into
   a scripting facility.

   Requests are vectors following expected conventions and implementations can be found in the [[convex.shell.sreq]]
   namespace.

   A series of CVX libraries is embedded, building on those requests and the way the shell generally operates,
   providing features such as reading CVX files, unit testing, a REPL, or time-travel. All features are self
   documented in the grand tradition of Lisp languages.
  
   Functions throughout these namespaces often refer to `env`. It is an environment map passed around containing
   everything that is need by an instance: current CVM context, opened streams, current error if any, etc.
  
   In case of error, [[convex.shell.exec.fail/err]] must be used so that the error is reported to the CVX executing environment.
  
   List of transactions pending for execution is accessible in the CVX execution environment under `$.trx/*list*`. This list
   can be modified by the user, allowing for powerful metaprogramming. Besides above-mentioned requests, this feature is used
   to implement another series of useful utilities such as exception catching."

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval])
  (:require [clojure.string         :as string]
            [convex.cell            :as $.cell]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.exec      :as $.shell.exec]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.io        :as $.shell.io]
            [convex.shell.log]
            [convex.shell.sreq]))


;;;;;;;;;; Initialization


(defn init

  "Used by [[eval]] to initiate `env`.

   Notably, prepares:

   - STDIO streams
   - Initial CVM context"

  [env]

  (assoc env
         :convex.shell/ctx      $.shell.ctx/ctx-base
         :convex.shell/stream+  {0 $.shell.io/stdin-txt
                                 1 $.shell.io/stdout-txt
                                 2 $.shell.io/stderr-txt}
         :convex.shell.stream/id 2))


;;;;;;;;;; Evaluating a given source string


(defn eval

  "Uses [[init]], reads the given `string` of transactions and starts executing them.
  
   Used by [[-main]]."


  ([string]

   (eval nil
         string))


  ([env string]

   (-> env
       (init)
       ($.shell.ctx/def-trx+ ($.cell/* (($.code/!.read+ ~($.cell/string string))
                                        ($.trx/precat $/*result*))))
       ($.shell.exec/trx+))))


;;;;;;;;;; Main functions


(defn -main

  "Reads and executes transactions.
  
   If no transaction is provided, starts the REPL.
  
   ```clojure
   (-main \"(+ 2 2)\")
   ```"

  [& trx+]

  (try
    (eval (if (seq trx+)
            (string/join " "
                                 trx+)
            "($.repl/!.start {:intro? true})"))
    (catch Throwable ex
      ($.shell.exec.fail/top-exception ex)
      (when (not= (System/getProperty "convex.dev")
                  "true")
        (System/exit 42)))))




(comment

   
  (-main "[:cvm.sreq :etch.open \"/tmp/foo.etch\"] [:cvm.sreq :etch.root-write {:a :b}] ($.stream/!.outln $/*result*)")
  (-main "[:cvm.sreq :etch.open \"/tmp/foo.etch\"] [:cvm.sreq :etch.root-read] ($.stream/!.outln $/*result*)")
  (-main "42 [:cvm.sreq :etch.flush] ($.stream/!.outln $/*result*)")
  (-main "[:cvm.sreq :etch.path] ($.stream/!.outln $/*result*)")
  (-main "[:cvm.sreq :etch.write [:foo :bar]] [:cvm.sreq :etch.read $/*result*] ($.stream/!.outln $/*result*)")
  (-main "[:cvm.sreq :etch.open \"/tmp/foo2.etch\"] ($.stream/!.outln $/*result*)")
  (-main "[:cvm.sreq :etch.open \"/tmp/foo3.etch\"] [:cvm.sreq :etch.open \"/tmp/foo3.etch\"]")

  (-main)

  (-main "(+ 2 2)")



  )
