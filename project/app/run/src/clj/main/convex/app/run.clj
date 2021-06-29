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
    (println "Describe command expects 1 or 2 arguments.")))



(defn eval

  ""

  [arg+ _option+]

  ($.run/eval env
              (first arg+)))



(defn load

  ""

  [arg+ _option+]

  ($.run/load env
              (first arg+)))



(defn watch

  ""

  [arg+ _option+]

  ($.run/watch env
               (first arg+)))


;;;;;;;;;; Main command


(defn handle-exception

  ""

  [err]

  (throw err))



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


    (catch clojure.lang.ExceptionInfo err
      (let [data (ex-data err)]
        (if (::error? data)
          (do
            (println [:error (.getMessage err)])
            ;(System/exit 42)
            )
          (handle-exception err))))


    (catch Throwable err
      (handle-exception err))))


;;;;;;;;;; Dev


(comment


  (-main)

  (-main "describe" "help" "about")

  (-main "eval" "(help/about sreq 'dep)")

  (-main "command" "eval")

  )
