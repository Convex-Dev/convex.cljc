(ns convex.shell.exec.fail

  "About handling different failure scenarios."

  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute))
  (:require [clojure.java.io  :as java.io]
            [clojure.pprint   :as pprint]
            [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.shell.ctx :as $.shell.ctx]
            [convex.shell.kw  :as $.shell.kw]
            [convex.std       :as $.std]))


(declare rethrow)


;;;;;;;;;; Notifying a failure or full halt


(defn err

  "Must be called in case of failure related to executing CVX Lisp, `err` being an error map (see the [[convex.shell.err]]
   namespace).
  
   Under CVX `$.catch/*stack*` in the context is a stack of error handling transactions. This functions pops
   the next error handling transaction and prepends it to CVX `$.trx/*list*`, the list of transactions pending
   for execution.

   Also, error becomes available under `$/*result*`.

   This simple scheme allows sophisticated exception handling to be implemented from CVX Lisp, as seen in the
   `$.catch` acccount."

  [env err]

  (-> env
      (update :convex.shell/ctx
              $.cvm/exception-clear)
      (rethrow ($.std/assoc err
                            $.shell.kw/exception?
                            ($.cell/* true)))))



(let [trx-pop ($.cell/* ($.catch/pop))]

  (defn rethrow

    "Like [[err]] but assumes the error has already been prepared as an exception result to return and the exception
     on the CVM context has already been cleared."

    [env ex]

    (-> env
        (assoc :convex.shell/error
               ex)
        ($.shell.ctx/prepend-trx trx-pop)
        ($.shell.ctx/def-result ex))))



(defn top-exception

  "Called when a JVM exception is caught at the very top level of the shell.
   No `env` is available at that point. This is last resort."

  [ex]

  (let [path (str (Files/createTempFile "convex-shell-fatal-"
                                        ".edn"
                                        (make-array FileAttribute
                                                    0)))]
    (with-open [writer (java.io/writer path)]
      (pprint/pprint {:convex.shell/exception (Throwable->map ex)}
                     writer))
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
               path)
      (flush))
    path))
