(ns convex.cvm.eval.src

  "Mimicks [[convex.cvm.eval]] but for evaling Convex Lisp source, strings of code."

  {:author "Adam Helinski"}

  (:require [convex.cvm  :as $.cvm]
            [convex.lisp :as $.lisp]))


;;;;;;;;;;


(defn ctx

  "Reads Convex Lisp source, evaluates it, and returns `ctx`."

  [ctx src]

  ($.cvm/eval ($.cvm/fork ctx)
              ($.lisp/read src)))



(defn error

  "Like [[ctx]] but returns the error that has occured (or nil)."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/error
      $.lisp/datafy))



(defn error?

  "Like [[ctx]] but returns a boolean indicating if an error occured."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/error
      some?))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/result
      $.lisp/datafy))



(defn value

  "Like [[ctx]] but returns either an [[error]] or a [[result]]."
  
  [ctx src]

  (let [ctx-2 (convex.cvm.eval.src/ctx ctx
                                       src)
        error ($.cvm/error ctx-2)]
    (if (nil? error)
      (-> ctx-2
          $.cvm/result
          $.lisp/datafy)
      error)))
