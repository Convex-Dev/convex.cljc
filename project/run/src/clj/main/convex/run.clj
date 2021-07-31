(ns convex.run

  "Core namespace implementing a Convex Lisp runner.

   The purpose of a runner is to provide a convenient framework for evaluating a Convex Lisp main file and
   related dependencies.


   ENV
   ===

   Those utilities are built using the 'sync' project and the 'watch' project. Just as in those project, a runner
   operates over an \"environment\" map. See [[init]].

   In a main file, each form is evaluated as a transaction and gradually modifies an environment.


   EVALUATION
   ==========

   [[eval]] serves to evaluate a given string as if it was a main file ; [[load]] reads and executes a main file ;
   [[watch]] behaves like [[load]] but provides a live-reloading experience.


   SPECIAL REQUESTS
   ================

   A runner can do useful side-effects on-demand called \"special requests\", such as outputting a value. Those special
   requests are nothing more than vector that the runner interprets at runtime.
  
   See the [[convex.run.sreq]] namespace.

  
   DYNAMIC VALUES AND HELP
   =======================

   A runner maintains a set of dynamic values that users can access in the address aliased by default as `env`. For more
   information, use [[eval]] to evaluate `(env/about env)`.


   OUTPUT
   ======

   Any value that is requested to be outputted (by the `(sreq/out ...)` special request) goes through the output hook.
   See hook section.


   ERRORS
   ======

   When a CVM exception or any other error occurs, [[convex.run.err/fail]] must be called with a CVX error map (built from
   scratch or by mappifying an actual CVM exception).


   HOOKS
   =====

   Hooks are transactions executed by the runner at key moments. They are defined through the `env` account:

   - `hook.end`
   - `hook.error`


   CLI APP
   =======

   The CLI Convex Lisp Runner is a light layer built on top of this project. It is not much more than a CLI interface with
   a description. See `:project/app.run` in Deps."

  ;; TODO. Improve reader error reporting when ANTLR gets stabilized.

  {:author "Adam Helinski"}

  (:gen-class)
  (:refer-clojure :exclude [eval])
  (:require [clojure.string]
            [convex.cvm         :as $.cvm]
            [convex.io          :as $.io]
            [convex.read        :as $.read]
            [convex.run.ctx     :as $.run.ctx]
            [convex.run.err     :as $.run.err]
            [convex.run.exec    :as $.run.exec]
            [convex.run.sreq]))


(declare sym->dep)


;;;;;;;;;; Initialization


(defn init

  "Initializes important functions and values in the given `env`, using defaults when relevant.

   Must be used prior to other preparatory functions such as [[main-file]].

   Operates over:

   | Key | Action | Mandatory |
   |---|---|---|
   | `:convex.run/path` | If present, ensures the path to the main file is canonical |
   | `:convex.run.hook/end` | Ensures a default end hook, see namespace description |
   | `:convex.run.hook/error` | Ensures a default error hook, see namespace description |
   | `:convex.run.hook/out` | Ensures a default output hook, see namespace description |
   | `:convex.run/single-run? | Whether code is run once or more (watch mode), defaults to false |
   | `:convex.sync/cx-base | Ensures a default base context, see [[convex.run.ctx/base]] |"


  [env]

  (-> env
      (assoc :convex.run/stream+  {0 $.io/stdin-txt
                                   1 $.io/stdout-txt
                                   2 $.io/stderr-txt}
             :convex.run/watch?    false
             :convex.run.stream/id 2)
      (update :convex.run/fail
              #(or %
                   $.run.exec/fail))
      (update :convex.run/fatal
              #(or %
                   (fn [_env err]
                     (print "FATAL: ")
                     (println err)
                     (flush)
                     (System/exit 42))))
      (update :convex.run/ctx
              #(or %
                   ($.cvm/fork $.run.ctx/base)))))


;;;;;;;;;; Evaluating a given source string


(defn eval

  ""

  ([string]

   (eval nil
         string))


  ([env string]

   (let [env-2  (init env)
         [ex
          trx+] (try
                  [nil
                   ($.read/string+ string)]
                  (catch Throwable ex
                    [ex
                     nil]))]
     (if ex
       ((env-2 :convex.run/fatal)
        env-2
        ($.run.err/reader))
       (-> env-2
           ($.run.ctx/precat-trx+ trx+)
           $.run.exec/trx+)))))


;;;;;;;;;;


(defn -main

  ""

  [& arg+]

  (try
    (eval (if (seq arg+)
            (clojure.string/join " "
                                 arg+)
            "($.repl/start {:intro? true})"))
    (catch Throwable _ex
      (println "An unknown exception happened.")
      (flush)
      (when (not= (System/getenv "CONVEX_DEV")
                  "true")
        (System/exit 42)))))
