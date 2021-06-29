(ns convex.app.run.help

  "Printing help description for the CLI runner."

  {:author "Adam Helinski"}
  
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]))


;;;;;;;;;; Private


(defn- -print

  ;;

  [line+]

  (println (clojure.string/join \newline
                                line+)))


;;;;;;;;;; Description


(defn eval

  ""

  []

  (-print
    ["Execute the given string as Convex Lisp code."
     ""
     ""
     "Usage:"
     ""
     "  eval STRING"
     ""
     ""
     "Example:"
     ""
     "  eval \"(sreq/out (+ 2 3))\""
     ""
     ""
     "This example uses a special request for outputting the result."
     ""
     "For more information, run:"
     ""
     "  eval \"(help/about sreq 'out)\""]))



(defn help

  ""

  []

  (-print
    ["Print out a complete description of the given command."
     ""
     ""
     "Usage:"
     ""
     "  help COMMAND"
     ""
     ""
     "Example:"
     ""
     "  help watch"]))



(defn load

  ""

  []

  (-print
    ["Load and execute the given main file."
     ""
     "Each form in that main file is executed as a transaction."
     ""
     ""
     "Usage:"
     ""
     "  load path/to/file.cvx"
     ""
     ""
     "Help and SReq libraries described in the general help are crucial in order to produce any"
     "meaningful work."]))



(defn main

  ""

  []

  (-print
    ["Convex Lisp Runner"
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
     "Each form is executed as a transaction."
     ""
     "This runner aliases 2 useful libraries."
     ""
     "The Help library, aliased as `help`, provides a series of useful dynamic values and a generic `about` function which outputs"
     "information about any account or symbol."
     ""
     "The SReq library, aliased as `sreq`, provides \"special requests\" that this runner understands: useful actions such as producing"
     "an output or advancing the timestamp."
     ""
     "For more information, run:"
     ""
     "  eval '(help/about help)'"
     "  eval '(help/about sreq)'"]))



(defn watch

  ""

  []

  (-print
    ["Like the load command but provide live-reloading."
     ""
     "Whenever the main file or any of its dependency changes, code is re-executed."
     ""
     "This workflow provides a very productive experience akin to a running a notebook and is adviced during"
     "development and testing."
     ""
     ""
     "On each run, state is reset."
     ""
     ""
     "Usage:"
     ""
     "  watch path/to/file.cvx"]))
