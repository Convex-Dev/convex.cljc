(ns convex.app.run

  "Convex Lisp Runner.

   CLI app on top of the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]
            [clojure.tools.cli]
            [convex.app.run.help :as $.app.run.help]
            [convex.run          :as $.run]))


;;;;;;;;;; Miscellaneous


(def env

  "Base environment map used in this app.

   See [[convex.run]] namespace."

  {})



(defn error

  "In production, exits the application printing the given `message` with an error code
   of 42.

   In dev, only prints the message."


  ([message]

   (error message
          nil))


  ([message _exception]

   (println message)
   (flush)
   (when (not= (System/getenv "CONVEX_DEV")
               "true")
     (System/exit 42))))



(defn ensure-path

  "Given `arg+` (CLI arguments to a command) contains a file `path`, call `(f env path)`.

   If not, terminates with [[error]].

   See [[env]]."

  [f arg+]

  (if-some [path (first arg+)]
    (f env
       path)
    (error "Path fo main file is missing.")))


;;;;;;;;;; Main command


(def cli-option+

  "CLI options for `clojure.tools.cli`."

  [])



(defn -main

  "Main function.

   Executes a CLI command:

   - [[command]]
   - [[describe]]
   - [[eval]]
   - [[load]]
   - [[watch]]"

  [& arg+]

  (try
    (println :arg+ arg+ (seq arg+))
    ($.run/eval (if (seq arg+)
                  (clojure.string/join " "
                                       arg+)
                  "($.repl/start)"))
    (catch Throwable ex
      (println :EX ex)
      (error "An unknown exception happened."
             ex))))
