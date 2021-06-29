(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]
            [clojure.tools.cli]
            [convex.run         :as $.run]))


;;;;;;;;;; Miscellaneous


(def env

  ""

  {:convex.run.hook/out (fn [env-2 x]
                          (println (str x))
                          (flush)
                          env-2)})


(def help

  ""

  (->> ["Convex Lisp Runner"
        ""
        "Execute code locally, without any server setup."
        ""
        ""
        "Commands:"
        ""
        "  eval   Execute the given string"
        "  help   Provide a description of the given command"
        "  load   Execute the given main file"
        "  watch  Live reloads the given main file"
        ""
        ""
        "This runner aliases 2 useful libraries."
        ""
        "The Help library, aliased as `help`, provides a series of useful dynamic values and a generic `about` function which outputs"
        "information about any account or symbol."
        ""
        "The SReq library, aliased as `sreq`, provides \"special requests\" that this runner understands: useful actions such as producing"
        "an output or advancing the timestamp."

        "For more information, run:"
        ""
        "  eval '(help/about help)'"
        "  eval '(help/about sreq)'"]
       (clojure.string/join \newline)))


;;;;;;;;;; Commands


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
          command             (first arg+)
          f                   (case command
                               "eval"  eval
                               "load"  load
                               "watch" watch
                               nil)]
      (if f
        (f (rest arg+)
           option+)
        (println help)))


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

  (-main "eval" "(help/about help)")

  )
