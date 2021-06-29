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


(defn command

  ""

  []

  (-print
    ["Print out a complete description of the given command."
     ""
     ""
     "Usage:"
     ""
     "    command COMMAND"
     ""
     ""
     "Example:"
     ""
     "    command watch"]))



(defn describe

  ""

  []

  (-print
    ["Shortcut for calling the `about` function from the Help library."
     ""
     "Print a human-readable description of any account or any symbol defined in any account."
     ""
     ""
     "Usage:"
     ""
     "    describe ADDRESS-OR-SYMBOL"
     "    describe ADDRESS SYMBOL"
     ""
     ""
     "Examples:"
     ""
     "    describe help"
     "    describe help about"
     "    describe sreq"
     "    describe #8 +"]))



(defn eval

  ""

  []

  (-print
    ["Execute the given string as Convex Lisp code."
     ""
     ""
     "Usage:"
     ""
     "    eval STRING"
     ""
     ""
     "Example:"
     ""
     "    eval \"(sreq/out (+ 2 3))\""
     ""
     ""
     "This example uses a \"special request\" for outputting the result."
     ""
     "For more information, run:"
     ""
     "    describe sreq"
     "    describe sreq out"]))



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
     "    load path/to/file.cvx"
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
     "    command   Provide details regarding the given command"
     "    describe  Describe an account or any defined symbol"
     "    eval      Execute the given string"
     "    load      Execute the given main file"
     "    watch     Live reloads the given main file"
     ""
     ""
     "Each form is executed as a transaction."
     ""
     "Two libraries are aliased by default as `help` and `sreq`. Understanding them is crucial for"
     "producing usefl work."
     ""
     "For more information, run:"
     ""
     "    describe help"
     "    describe sreq"]))



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
     "    watch path/to/file.cvx"]))
