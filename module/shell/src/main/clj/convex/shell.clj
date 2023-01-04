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
            [convex.cvm             :as $.cvm]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.exec      :as $.shell.exec]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.io        :as $.shell.io]
            [convex.shell.kw        :as $.shell.kw]
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
         :convex.shell/ctx             ($.cvm/fork $.shell.ctx/ctx-base)
         :convex.shell/stream+         {$.shell.kw/stderr $.shell.io/stderr-txt
                                        $.shell.kw/stdin  $.shell.io/stdin-txt
                                        $.shell.kw/stdout $.shell.io/stdout-txt}
         :convex.shell.etch/read-only? false
         :convex.shell.juice/limit     $.shell.exec/max-juice
         :convex.shell.stream/id       2))


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
        (System/exit 1)))))




(comment

  (-main "(+ 4 4)")

  (-main "[:cvm.sreq :dep.deploy \"/Users/adam/Code/convex/clj/tool/private/lab/dep/vote\" '[convex.vote [main convex vote] vote.trust [main vote trust]]] [convex.vote vote.trust]")

  (-main "($.file/!.exec \"/Users/adam/Code/convex/clj/tool/private/lab/dep/dao/dev/workspace.cvx\")")
  
  
  )
