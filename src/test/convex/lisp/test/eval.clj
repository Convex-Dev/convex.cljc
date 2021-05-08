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


(defn apply-one

  "After quoting it, applies `x` to `form` on the CVM.
  
   Similar to [[form]]."

  
  ([form x]

   (apply-one ($.ctx/create-fake)
              form
              x))


  ([ctx form x]

   (convex.lisp.test.eval/form ctx
                               (list form
                                     ($.form/quoted x)))))



(defn exceptional

  "Evals the given form and returns a boolean indicating if the result is exceptional or not."


  ([form]

   (exceptional ($.ctx/create-fake)
                form))


  ([ctx form]
   
   (->> form
        $.form/source
        $/read
        ($.ctx/eval ctx)
        $.ctx/exceptional)))



(defn form

  "Evals the given `form` representing Convex Lisp code and returns the result as Clojure data."


  ([form]

   (convex.lisp.test.eval/form ($.ctx/create-fake)
                               form))


  ([ctx form]

   (source ctx
           ($.form/source form))))



(defn form->ctx

  "Like [[form]] but returns the ctx, not the result prepared as Clojure data."


  ([form]

   (form->ctx ($.ctx/create-fake)
              form))


  ([ctx form]

   (->> form
        $.form/source
        $/read
        ($.ctx/eval ctx))))



(defn source

  "Reads Convex Lisp source, evals it and converts the result to a Clojure value."


  ([source]

   (convex.lisp.test.eval/source ($.ctx/create-fake)
                                 source))


  ([ctx source]

   (->> source
        $/read
        ($.ctx/eval ctx)
        $.ctx/result
        $/datafy)))



(defn source-exceptional

  "Reads Convex Lisp source, evals it and returns the resulting exceptional object."


  ([source]

   (source-exceptional ($.ctx/create-fake)
                       source))


  ([ctx source]

   (->> source
        $/read
        ($.ctx/eval ctx)
        $.ctx/exceptional)))
