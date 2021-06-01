(ns convex.cvm.eval.src

  "Mimicks [[convex.cvm.eval]] but for evaling Convex Lisp source, strings of code."

  {:author "Adam Helinski"}

  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(defn ctx

  "Reads Convex Lisp source, evaluates it, and returns `ctx`."

  [ctx src]

  ($.cvm/eval ($.cvm/fork ctx)
              ($.cvm/read src)))



(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/exception
      $.cvm/as-clojure))



(defn exception?

  "Like [[ctx]] but returns a boolean indicating if an exception occured."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/exception?))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."

  [ctx src]

  (-> (convex.cvm.eval.src/ctx ctx
                               src)
      $.cvm/result
      $.cvm/as-clojure))



(defn value

  "Like [[ctx]] but returns either an [[exception]] or a [[result]]."
  
  [ctx src]

  (let [ctx-2     (convex.cvm.eval.src/ctx ctx
                                           src)
        exception ($.cvm/exception ctx-2)]
    (-> (if (nil? exception)
          ($.cvm/result ctx-2)
          exception)
        $.cvm/as-clojure)))
