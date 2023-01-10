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
  (:require [clojure.string      :as string]
            [convex.cell         :as $.cell]
            [convex.cvm          :as $.cvm]
            [convex.shell.ctx    :as $.shell.ctx]
            [convex.shell.fail   :as $.shell.fail]
            [convex.shell.log]))


;;;;;;;;;; Main


(defn init

  []

  ($.cvm/fork $.shell.ctx/genesis))



(defn transact

  [ctx trx]

  (try
    ($.cvm/eval ctx
                trx)
    ;;
    (catch clojure.lang.ExceptionInfo ex
      (or (:convex.shell/ctx (ex-data ex))
          ($.shell.fail/top-exception ctx
                                      ex)))
    (catch Throwable ex
      ($.shell.fail/top-exception ctx
                                  ex))))



(defn -main

  "Reads and executes transactions.
  
   If no transaction is provided, starts the REPL.
  
   ```clojure
   (-main \"(+ 2 2)\")
   ```"

  [& trx+]

  (let [ctx (-> (init)
                (transact ($.cell/*
                            ((lookup ~($.cell/address 8)
                                     shell.main)
                              ~($.cell/string (string/join " "
                                                           trx+))))))
        ex  ($.cvm/exception ctx)]
    (if ex
      (if (= ($.cvm/exception-code ex)
             ($.cell/* :SHELL.FATAL))
        (do
          (when-some [path-report (-> ex
                                      ($.cvm/exception-message)
                                      (get ($.cell/* :report)))]
            (binding [*out* *err*]
              (println)
              (println "==================")
              (println)
              (println "  FATAL ERROR  :'(  ")
              (println)
              (println "==================")
              (println)
              (println "Please open an issue if necessary:")
              (println "    https://github.com/Convex-Dev/convex.cljc")
              (println)
              (println "Report printed to:")
              (println "   "
                       (str path-report))
              (flush)))
          (System/exit 1))
        (do
          (println (str ($.shell.fail/mappify-cvm-ex ex)))
          (System/exit 2)))
      (do
        (println (str ($.cvm/result ctx)))
        (System/exit 0)))))
