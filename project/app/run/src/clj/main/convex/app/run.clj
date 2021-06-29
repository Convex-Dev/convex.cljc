(ns convex.app.run

  ""

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

  ""

  {:convex.run.hook/out (fn [env-2 x]
                          (println (str x))
                          (flush)
                          env-2)})



(defn error

  ""


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

  ""

  [f arg+]

  (if-some [path (first arg+)]
    (f env
       path)
    (error "Path fo main file is missing.")))


;;;;;;;;;; Commands


(defn command

  ""

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

  ""

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

  ""

  [arg+ _option+]

  (if-some [string (first arg+)]
    ($.run/eval env
                string)
    (error "No code to evaluate.")))



(defn load

  ""

  [arg+ _option+]

  (ensure-path $.run/load
               arg+))



(defn watch

  ""

  [arg+ _option+]

  (ensure-path $.run/watch
               arg+))


;;;;;;;;;; Main command


(def cli-option+

  ""

  [])



(defn -main

  ""

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
