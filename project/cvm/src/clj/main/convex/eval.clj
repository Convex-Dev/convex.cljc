(ns convex.eval

  "Quick helpers built on top of [[convex.cvm/eval]].
  
   Systematically forks the used context before any operation so that it remains intact.
  
   Notably useful when writing tests."

  (:refer-clojure :exclude [true?])
  (:require [convex.cvm :as $.cvm]
            [convex.std :as $.std]))


;;;;;;;;;;


(defn ctx

  "Evaluates the given `cell` and returns `ctx`."

  [ctx cell]

  ($.cvm/eval ($.cvm/fork ctx)
              cell))


;;;;;;;;;;


(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."

  [ctx cell]

  (-> (convex.eval/ctx ctx
                       cell)
      $.cvm/exception))


(defn exception-code
  
  "Shortcut on top of [[exception]]. Returns the code of the exception associated with `ctx` or
   nil if no exception occured."

  [ctx cell]

  (some-> (exception ctx
                     cell)
          ($.cvm/exception-code)))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."

  [ctx cell]

  ($.cvm/result (convex.eval/ctx ctx
                                 cell)))


(defn true?

  "Shortcut on top of [[result]]. Returns true if the result is CVX true.
  
   Notably useful for test assertions."

  [ctx cell]

  ($.std/true? (result ctx
                       cell)))
