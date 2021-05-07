(ns convex.lisp.test.eval

  "Evaling code in different ways, typically needed in generative tests.
  
   Unless specified otherwise, always returns a value as Clojure data."

  {:author "Adam Helinski"}

  (:require [convex.lisp      :as $]
            [convex.lisp.form :as $.form]))


(declare form
         source)


;;;;;;;;;;


(defn apply-one

  "After quoting it, applies `x` to `form` on the CVM.
  
   Similar to [[form]]."

  
  ([form x]

   (apply-one ($/context)
              form
              x))


  ([context form x]

   (convex.lisp.test.eval/form context
                               ($.form/templ {'?form form
                                              '?x    x}
                                             '(?form (quote ?x))))))



(defn exceptional

  "Evals the given form and returns a boolean indicating if the result is exceptional or not."


  ([form]

   (exceptional ($/context)
                form))


  ([context form]
   
   (->> form
        $.form/source
        $/read
        ($/eval context)
        $/exceptional)))



(defn form

  "Evals the given `form` representing Convex Lisp code and returns the result as Clojure data."


  ([form]

   (convex.lisp.test.eval/form ($/context)
                               form))


  ([context form]

   (source context
           ($.form/source form))))



(defn form->context

  "Like [[form]] but returns the context, not the result prepared as Clojure data."


  ([form]

   (form->context ($/context)
                  form))


  ([context form]

   (->> form
        $.form/source
        $/read
        ($/eval context))))



(defn source

  "Reads Convex Lisp source, evals it and converts the result to a Clojure value."


  ([source]

   (convex.lisp.test.eval/source ($/context)
                                 source))


  ([context source]

   (->> source
        $/read
        ($/eval context)
        $/result
        $/datafy)))



(defn source-exceptional

  "Reads Convex Lisp source, evals it and returns the resulting exceptional object."


  ([source]

   (source-exceptional ($/context)
                       source))


  ([context source]

   (->> source
        $/read
        ($/eval context)
        $/exceptional)))
