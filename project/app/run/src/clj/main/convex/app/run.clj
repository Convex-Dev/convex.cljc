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

  {:convex.run.hook/out (fn [env-2 x]
                          (println (str x))
                          (flush)
                          env-2)})



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


;;;;;;;;;; Commands


(defn command

  "Implementation for the 'command' CLI command.
  
   Prints a description of the user given command."

  [arg+ _option+]

  (if-some [command (first arg+)]
    (case command
      "command"  ($.app.run.help/command)
      "describe" ($.app.run.help/describe)
      "eval"     ($.app.run.help/eval)
      "load"     ($.app.run.help/load)
      "watch"    ($.app.run.help/watch)
      (str "Unknown command: "
           command))
    ($.app.run.help/main)))



(defn describe

  "Implementation for the 'describe' CLI command.

   Uses the Convex Lisp Help library for printing information about the user given account or symbol."

  [arg+ _option+]

  (case (count arg+)
    1 ($.run/eval env
                  (format "(help/about %s)"
                          (first arg+)))
    2 ($.run/eval env
                  (format "(help/about %s '%s)"
                          (first arg+)
                          (second arg+)))
    (error "Describe command expects 1 or 2 arguments.")))



(defn eval

  "Implementation for the 'eval' CLI command.
  
   Evaluatues the user given string."

  [arg+ _option+]

  (if-some [string (first arg+)]
    ($.run/eval env
                string)
    (error "No code to evaluate.")))



(defn load

  "Implementation for the 'load' CLI command.
  
   Loads and executes the user given main file."

  [arg+ _option+]

  (ensure-path $.run/load
               arg+))



(defn watch

  "Implementation for the 'watch' CLI command.
  
   Like [[load]] but provides live-reloading of main file and its declared dependencies."

  [arg+ _option+]

  (ensure-path $.run/watch
               arg+))


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
    (let [{arg+    :arguments
           option+ :options}  (clojure.tools.cli/parse-opts arg+
                                                            cli-option+)
          cmd      (first arg+)
          f                   (case cmd
                                "command"  command
                                "describe" describe
                                "eval"     eval
                                "load"     load
                                "watch"    watch
                                nil)]
      (if f
        (f (rest arg+)
           option+)
        ($.app.run.help/main)))


    (catch Throwable ex
      (error "An unknown exception happened."
             ex))))


;;;;;;;;;; Dev


(comment


  (-main)

  (-main "describe" "help" "about")

  (-main "eval" "(help/about sreq 'dep)")

  (-main "command" "eval")



  (def a*
       (-main "watch" "project/run/src/cvx/dev/convex/app/run/dev.cvx"))

  (require '[convex.watch])

  (convex.watch/stop a*)


  )
