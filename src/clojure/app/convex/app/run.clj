(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.tools.cli]
            [convex.run         :as $.run]))


;;;;;;;;;; Commands


(defn eval

  ""

  [arg+ _option+]

  ($.run/eval (first arg+)))



(defn load

  ""

  [arg+ _option+]

  ($.run/load (first arg+)))



(defn watch

  ""

  [arg+ _option+]

  ($.run/watch (first arg+)))


;;;;;;;;;; Main command


(defn handle-exception

  ""

  [err]

  ;(*output* [:exception.java err])
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
        ($.run/error (format "Unknown command: %s"
                             command))))


    (catch clojure.lang.ExceptionInfo err
      (let [data (ex-data err)]
        (if (::error? data)
          (do
            ($.run/*output* [:error (.getMessage err)])
            ;(System/exit 42)
            )
          (handle-exception err))))


    (catch Throwable err
      (handle-exception err))))
