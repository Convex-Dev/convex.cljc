(ns convex.lisp.test.eval

  "Bridge to [[convex.lisp.eval]] but uses [[ctx]] when no context is provided."

  {:author "Adam Helinski"}

  (:require [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.eval :as $.eval]))


(declare ctx)


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


;;;;;;;;;;


(def ctx

  "Base context to use for testing."

  (-> ($.ctx/create-fake)
      ($.eval/form->ctx
        '(call *registry*
               (cns-update '$
                           (deploy
                             '(do
                                (defn every?

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (or (f x)
                                                         (reduced false)))
                                                   true
                                                   coll)))
                                )))))
      ($.eval/form->ctx
        '(import $ :as $))))
