(ns convex.lisp.test.eval

  "Bridge to [[convex.lisp.eval]] but uses [[ctx]] when no context is provided."

  {:author "Adam Helinski"}

  (:require [convex.lisp      :as $]
            [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.eval :as $.eval]
            [convex.lisp.form :as $.form]))


;;;;;;;;;;


(def ctx

  "Base context to use for testing."

  (->> '(def foo 42)
       $.form/source
       $/read
       ($.ctx/eval ($.ctx/create-fake))))


;;;;;;;;;;


(defn error?


  ([form]

   (error? ctx
           form))


  ([ctx form]

   ($.eval/error? ctx
                  form)))



(defn form


  ([form]

   (convex.lisp.test.eval/form ctx
                               form))


  ([ctx form]

   ($.eval/form ctx
                form)))



(defn form->ctx


  ([form]

   (form->ctx ctx
              form))


  ([ctx form]

   ($.eval/form->ctx ctx
                     form)))



(defn source


  ([source]

   (convex.lisp.test.eval/source ctx
                                 source))


  ([ctx source]

   ($.eval/source ctx
                  source)))



(defn source-error?


  ([source]

   (source-error? ctx
                  source))


  ([ctx source]

   ($.eval/source-error? ctx
                         source)))
