(ns convex.lisp.eval

  "Shortcuts for evaluating Convex Lisp code, useful for development and testing.
  
   Given context is always forked, meaning the argument is left intact.
  
   See [[convex.lisp.ctx/fork]]."

  {:author "Adam Helinski"}

  (:require [convex.lisp      :as $]
            [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.form :as $.form]))


(declare form
         source
         source->ctx
         source-error?)


;;;;;;;;;;


(defn error?

  "Evaluatess the given `form` and returns true if an error occured and the context entered in an
   exceptional state."

  [ctx form]
   
  (source-error? ctx
                 ($.form/source form)))



(defn form

  "Evaluates the given `form` representing Convex Lisp code and returns the result as Clojure data."

  [ctx form]

  (source ($.ctx/fork ctx)
          ($.form/source form)))



(defn form->ctx

  "Like [[form]] but returns the `ctx` instead of the result prepared as Clojure data."

  [ctx form]

  (source->ctx ctx
               ($.form/source form)))



(defn source

  "Reads Convex Lisp source, evaluates it and converts the result to a Clojure value."

  [ctx source]

  (-> (source->ctx ctx
                   source)
      $.ctx/result
      $/datafy))



(defn source->ctx

  "Like [[source]] but returns the `ctx` instead of the result prepared as Clojure data."

  [ctx source]

  (->> source
       $/read
       ($.ctx/eval ($.ctx/fork ctx))))


(defn source-error?

  "Reads Convex Lisp source, evaluates it and returns true if an error occured and the context
   entered in an exceptional state."

  [ctx source]

  (->> source
       $/read
       ($.ctx/eval ($.ctx/fork ctx))
       $.ctx/error
       boolean))
