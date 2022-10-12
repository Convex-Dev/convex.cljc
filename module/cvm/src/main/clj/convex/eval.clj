(ns convex.eval

  "Quick helpers for evaluating Convex Lisp code.
  
   Systematically forks the used context before any operation so that it remains intact.
  
   Notably useful when writing tests."

  (:refer-clojure :exclude [true?])
  (:require [convex.cvm :as $.cvm]
            [convex.std :as $.std]))


;;;;;;;;;;


(defn ctx

  "Evaluates the given `cell` and the resulting `ctx`."

  [ctx cell]

  ($.cvm/eval ($.cvm/fork ctx)
              cell))


;;;;;;;;;;


(defn exception

  "Evaluates the given `cell` and returns the resulting CVM exception.
  
   Or nil."

  [ctx cell]

  (-> (convex.eval/ctx ctx
                       cell)
      $.cvm/exception))


(defn exception-code

  "Evaluates the given `cell` and returns the resulting CVM exception code.

   Or nil."
  
  [ctx cell]

  (some-> (exception ctx
                     cell)
          ($.cvm/exception-code)))



(defn result

  "Evaluates the given `cell` and returns the result."

  [ctx cell]

  ($.cvm/result (convex.eval/ctx ctx
                                 cell)))


(defn true?

  "Evaluates the given `cell` and returns JVM `true` if the result is CVM `true`.
  
   Notably useful for test assertions."

  [ctx cell]

  ($.std/true? (result ctx
                       cell)))
