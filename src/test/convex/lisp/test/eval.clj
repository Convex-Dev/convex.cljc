(ns convex.lisp.test.eval

  "Evaling code in different ways, typically needed in generative tests.
  
   Unless specified otherwise, always returns a value as Clojure data."

  {:author "Adam Helinski"}

  (:require [convex.lisp      :as $]
            [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.form :as $.form]))


(declare form
         source)


;;;;;;;;;;


(def ctx

  ""

  (->> '(def foo 42)
       $.form/source
       $/read
       ($.ctx/eval ($.ctx/create-fake))))


;;;;;;;;;;


(defn apply-one

  "After quoting it, applies `x` to `form` on the CVM.
  
   Similar to [[form]]."

  
  ([form x]

   (apply-one ctx
              form
              x))


  ([ctx form x]

   (convex.lisp.test.eval/form ($.ctx/fork ctx)
                               (list form
                                     ($.form/quoted x)))))



(defn error?

  "Evals the given form and returns true if an error occured and the context entered in an
   exceptional state."


  ([form]

   (error? ctx
           form))


  ([ctx form]
   
   (->> form
        $.form/source
        $/read
        ($.ctx/eval ($.ctx/fork ctx))
        $.ctx/error
        boolean)))



(defn form

  "Evals the given `form` representing Convex Lisp code and returns the result as Clojure data."


  ([form]

   (convex.lisp.test.eval/form ctx
                               form))


  ([ctx form]

   (source ($.ctx/fork ctx)
           ($.form/source form))))



(defn form->ctx

  "Like [[form]] but returns the ctx, not the result prepared as Clojure data."


  ([form]

   (form->ctx ctx
              form))


  ([ctx form]

   (->> form
        $.form/source
        $/read
        ($.ctx/eval ($.ctx/fork ctx)))))



(defn source

  "Reads Convex Lisp source, evals it and converts the result to a Clojure value."


  ([source]

   (convex.lisp.test.eval/source ctx
                                 source))


  ([ctx source]

   (->> source
        $/read
        ($.ctx/eval ($.ctx/fork ctx))
        $.ctx/result
        $/datafy)))



(defn source-error?

  "Reads Convex Lisp source, evals it and returns true if an error occured and the context
   entered in an exceptional state."


  ([source]

   (source-error? ctx
                  source))


  ([ctx source]

   (->> source
        $/read
        ($.ctx/eval ($.ctx/fork ctx))
        $.ctx/error
        boolean)))
