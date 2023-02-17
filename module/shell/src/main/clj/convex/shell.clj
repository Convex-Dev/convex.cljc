(ns convex.shell

  "CONVEX SHELL

   Convex Virtual Machine extended with side-effects.

   For using as a terminal application, see [[-main]].

   For using as a library, see [[transact]].
  
   Assumes knowledge of `:module/cvm`."

  {:author "Adam Helinski"}

  (:gen-class)
  (:import (convex.core.init Init))
  (:require [clojure.string    :as string]
            [convex.db         :as $.db]
            [convex.cell       :as $.cell]
            [convex.cvm        :as $.cvm]
            [convex.shell.ctx  :as $.shell.ctx]
            [convex.shell.fail :as $.shell.fail]
            [convex.shell.log]
            [convex.shell.req  :as $.shell.req]))


;;;;;;;;;; Main


(defn init

  "Initializes a genesis context, forking [[convex.shell.ctx/genesis]].
   It is important that each such context is initialized and used in a dedicated
   thread.
  
   Options may be:

   | Key                     | Value                            |
   |-------------------------|----------------------------------|
   | `:convex.shell/invoker` | See [[convex.shell.req/invoker]] |"

  ;; Each ctx must have its own thread.

  ([]

   (init nil))


  ([option+]

   ($.db/current-set nil)
   (-> $.shell.ctx/genesis
       ($.cvm/fork)
       ($.cvm/def Init/CORE_ADDRESS
                  ($.cell/* {.shell.invoke ~(or (:convex.shell/invoker option+)
                                                ($.shell.req/invoker))})))))



(defn transact

  "Applies the given transaction (a cell) to the given context.
  
   Context should come from [[init]].
  
   Returns a context with a result or an exception attached."

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



(defn transact-main

  "Core implementation of [[-main]].
  
   Passes the text cells to the `.shell.main` CVX function defined in the core account.
  
   `ctx` should come from [[init]] and will be passed to [[transact]].

   In case of a result, prints its to STDOUT and terminates with a 0 code.
   In case of an exception, prints it to STDERR and terminates with a non-0 code."

  [ctx txt-cell+]

  (let [ctx-2 (transact ctx
                        ($.cell/*
                          ((lookup ~($.cell/address 8)
                                   .shell.main)
                            ~($.cell/string (string/join " "
                                                         txt-cell+)))))
        ex  ($.cvm/exception ctx-2)]
    (if ex
      (if (= ($.cvm/exception-code ex)
             ($.cell/* :SHELL.FATAL))
        (do
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
                     (-> ex
                         ($.cvm/exception-message)
                         (get ($.cell/* :report))
                         (str)))
            (flush))
          (System/exit 2))
        (binding [*out* *err*]
          (println (str ($.shell.fail/mappify-cvm-ex ex)))
          (System/exit 1)))
      (do
        (println (str ($.cvm/result ctx-2)))
        (System/exit 0)))))


;;;


(defn -main

  "Main entry point for using Convex Shell as a terminal application.
  
   Expects cells as text to wrap and execute in a `(do)`.
   See [[transact-main]] for a reusable implementation.
  
   ```clojure
   (-main \"(+ 2 2)\")
   ```"

  [& txt-cell+]

  (transact-main (init)
                 txt-cell+))




(comment

  (-> (init)
      (transact ($.cell/* (do

                            (def kp
                                 (.kp.create))

                            (.kp.public-key kp)

                            )))
      ($.cvm/result))
  )
