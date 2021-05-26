(ns convex.lisp.test.eval

  "Bridge to [[convex.lisp.eval]] but uses [[ctx]] when no context is provided.
  
   Undocumented symbols refer directly to the [[convex.lisp.eval]] namespace.
  
   Symbols ending with '*' designate a macro equivalent of a related function ([[result*]] for [[result]]) which
   template the given form using [[convex.lisp.form/templ*]]."

  {:author "Adam Helinski"}

  (:require [convex.lisp           :as $]
            [convex.lisp.ctx       :as $.ctx]
            [convex.lisp.form      :as $.form]
            [convex.lisp.eval      :as $.eval]
            [convex.lisp.test.prop :as $.test.prop]
            [convex.lisp.test.util :as $.test.util]))


(declare ctx-base
         result)


;;;;;;;;;;


(defn ctx


  ([form]

   (convex.lisp.test.eval/ctx ctx-base
                              form))


  ([ctx form]

   ($.eval/ctx ctx
               form)))



(defmacro ctx*


  ([form]

   `(convex.lisp.test.eval/ctx ($.form/templ* ~form)))


  ([ctx form]

   `(convex.lisp.test.eval/ctx  ~ctx
                                ($.form/templ* ~form))))



(defn error


  ([form]

   (error ctx-base
          form))


  ([ctx form]

   ($.eval/error ctx
                 form)))



(defmacro error*


  ([form]

   `(error ($.form/templ* ~form)))


  ([ctx form]

   `(error ~ctx
           ($.form/templ* ~form))))



(defn error?


  ([form]

   (error? ctx-base
           form))


  ([ctx form]

   ($.eval/error? ctx
                  form)))



(defmacro error?*


  ([form]

   `(error? ($.form/templ* ~form)))


  ([ctx form]

   `(error? ~ctx
            ($.form/templ* ~form))))



(defn error-cast?

  "Returns true if the given form is evaluated to a `:CAST` error."


  ([form]

   (error-cast? ctx-base
                form))


  ([ctx form]

   (= :CAST
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-cast?*


  ([form]

   `(error-cast? ($.form/templ* ~form)))


  ([ctx form]

   `(error-cast? ~ctx
                 ($.form/templ* ~form))))



(defn like-clojure?

  "Returns true if applying `arg+` to `form` on the CVM produces the exact same result as
   `(apply f arg+)`"


  ([form]

   (like-clojure? ctx-base
                  form))


  ([ctx form]

   ($.test.util/eq (eval form)
                   ($.eval/result ctx
                                  form)))


  ([form f arg+]

   (like-clojure? ctx-base
                  form
                  f
                  arg+))


  ([ctx form f arg+]

   ($.test.util/eq (apply f
                          arg+)
                   ($.eval/result ctx
                                  (list* form
                                         arg+)))))



(defmacro like-clojure?*

  
  ([form]

   `(like-clojure? ($.form/templ* ~form)))


  ([ctx form]

   `(like-clojure? ~ctx
                   ($.form/templ* ~form)))


  ([form f arg+]

   `(like-clojure? ($.form/templ* ~form)
                   ~f
                   ~arg+))


  ([ctx form f arg+]

   `(like-clojure? ~ctx
                   ($.form/templ* ~form)
                   ~f
                   ~arg+)))



(defn log


  ([form]

   (log ctx-base
        form))


  ([ctx form]

   ($.eval/log ctx
               form)))



(defn result


  ([form]

   (result ctx-base
           form))


  ([ctx form]

   ($.eval/result ctx
                  form)))



(defmacro result*


  ([form]

   `(result ($.form/templ* ~form)))


  ([ctx form]

   `(result ~ctx
            ($.form/templ* ~form))))



(defn result-log

  ""


  ([form]

   (result ctx-base
           form))


  ([ctx form]

   (let [ctx-2 ($.eval/ctx ctx
                           form)
         res   (-> ctx-2
                   $.ctx/result
                   $/datafy)]
     (or res
         ($.test.prop/fail {:convex.test/log (-> ctx-2
                                                 $.ctx/log
                                                 $/datafy)})))))



(defn value


  ([form]

   (value ctx-base
          form))


  ([ctx form]

   ($.eval/value ctx
                 form)))


;;;;;;;;;;


(def ctx-base

  "Base context to use for testing."

  ;; TODO. Needs 2 transactions because of: https://github.com/Convex-Dev/convex/issues/107

  (-> ($.ctx/create-fake)
      ($.eval/ctx
        '(call *registry*
               (cns-update '$
                           (deploy
                             '(do

                                (defn allowance

                                  ([]

                                   (allowance *address*))

                                  ([addr]

                                   (:allowance (account addr))))

                                (defn env

                                  ([]

                                   (env *address*))

                                  ([addr]

                                   (:environment (account addr))))

                                (defn every?

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (or (f x)
                                                         (reduced false)))
                                                   true
                                                   coll)))

                                (defn every-index?

                                  [f sequential]

                                  (loop [i (dec (count sequential))]
                                    (if (> i
                                           0)
                                      (if (f sequential
                                             i)
                                        (recur (dec i))
                                        false)
                                      true)))

                                (defn some

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (let [x-2 (f x)]
                                                       (if x-2
                                                         (reduced x-2)
                                                         false)))
                                                   true
                                                   coll)))
                                )))))
      ($.eval/ctx
        '(import $ :as $))
      ($.ctx/set-juice 1e7)))
