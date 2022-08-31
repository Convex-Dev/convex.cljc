(ns convex.shell

  "CONVEX SHELL

   This is a whole application. It is available as a library in case it needs to be embedded. Then, only [[-main]] is really
   useful.

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
  
   In case of error, [[convex.shell.exec/fail]] must be used so that the error is reported to the CVX executing environment.
  
   List of transactions pending for execution is accessible in the CVX execution environment under `$.trx/*list*`. This list
   can be modified by the user, allowing for powerful metaprogramming. Besides above-mentioned requests, this feature is used
   to implement another series of useful utilities such as exception catching."

  ;; TODO. Improve reader error reporting when ANTLR gets stabilized.

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval])
  (:require [clojure.string]
            [convex.cvm           :as $.cvm]
            [convex.read          :as $.read]
            [convex.shell.ctx     :as $.shell.ctx]
            [convex.shell.err     :as $.shell.err]
            [convex.shell.exec    :as $.shell.exec]
            [convex.shell.io      :as $.shell.io]
            [convex.shell.sreq]))


;;;;;;;;;; Initialization


(defn init

  "Used by [[eval]] to initiate `env`.

   Notably, prepares:

   - STDIO streams
   - Initial context
   - Function under `:convex.shell/fatal`, akin to [[convex.shell.exec/fail]], called only in case of
   a fatal error that cannot be reported to the CVX environment (seldom)"

  [env]

  (-> env
      (assoc :convex.shell/stream+  {0 $.shell.io/stdin-txt
                                     1 $.shell.io/stdout-txt
                                     2 $.shell.io/stderr-txt}
             :convex.shell.stream/id 2)
      (update :convex.shell/fatal
              #(or %
                   (fn [_env err]
                     (print "FATAL: ")
                     (println (str err))
                     (flush)
                     (System/exit 42))))
      (update :convex.shell/ctx
              #(or %
                   ($.cvm/fork $.shell.ctx/base)))))


;;;;;;;;;; Evaluating a given source string


(defn eval

  "Uses [[init]], reads the given `string` of transactions and starts executing them.
  
   Used by [[-main]]."


  ([string]

   (eval nil
         string))


  ([env string]

   (let [env-2  (init env)
         [ex
          trx+] (try
                  [nil
                   ($.read/string+ string)]
                  (catch Throwable ex
                    [ex
                     nil]))]
     (if ex
       ((env-2 :convex.shell/fatal)
        env-2
        ($.shell.err/reader))
       (-> env-2
           ($.shell.ctx/precat-trx+ trx+)
           ($.shell.exec/trx+))))))


;;;;;;;;;; Main functions


(defn -main

  "Reads and executes transactions.
  
   If no transaction is provided, starts the REPL.
  
   ```clojure
   (-main \"($.stream/out (+ 2 2))\")
   ```"

  [& trx+]

  (try
    (eval (if (seq trx+)
            (clojure.string/join " "
                                 trx+)
            "($.repl/!.start {:intro? true})"))
    (catch Exception _ex
      (println "An unknown exception happened.")
      (flush)
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



  )
