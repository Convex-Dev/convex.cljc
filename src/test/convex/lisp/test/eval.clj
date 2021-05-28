(ns convex.lisp.test.eval

  "Bridge to [[convex.lisp.eval]] but uses [[ctx]] when no context is provided.
  
   Undocumented symbols refer directly to the [[convex.lisp.eval]] namespace.
  
   Symbols ending with '*' designate a macro equivalent of a related function ([[result*]] for [[result]]) which
   template the given form using [[convex.lisp.form/templ*]]."

  {:author "Adam Helinski"}

  (:require [convex.cvm            :as $.cvm]
            [convex.cvm.eval       :as $.cvm.eval]
            [convex.lisp.form      :as $.form]
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

   ($.cvm.eval/ctx ctx
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

   ($.cvm.eval/error ctx
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

   ($.cvm.eval/error? ctx
                      form)))



(defmacro error?*


  ([form]

   `(error? ($.form/templ* ~form)))


  ([ctx form]

   `(error? ~ctx
            ($.form/templ* ~form))))



(defn error-arg?

  "Returns true if the given form is evaluated to an `:ARGUMENT` error."


  ([form]

   (error-arg? ctx-base
               form))


  ([ctx form]

   (= :ARGUMENT
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-arg?*


  ([form]

   `(error-arg? ($.form/templ* ~form)))


  ([ctx form]

   `(error-arg? ~ctx
                ($.form/templ* ~form))))




(defn error-arity?

  "Returns true if the given form is evaluated to an `:ARITY` error."


  ([form]

   (error-arity? ctx-base
                 form))


  ([ctx form]

   (= :ARITY
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-arity?*


  ([form]

   `(error-arity? ($.form/templ* ~form)))


  ([ctx form]

   `(error-arity? ~ctx
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



(defn error-fund?

  "Returns true if the given form is evaluated to a `:FUNDS` error."


  ([form]

   (error-fund? ctx-base
                form))


  ([ctx form]

   (= :FUNDS
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-fund?*


  ([form]

   `(error-fund? ($.form/templ* ~form)))


  ([ctx form]

   `(error-fund? ~ctx
                 ($.form/templ* ~form))))



(defn error-memory?

  "Returns true if the given form is evaluated to an `:MEMORY` error."


  ([form]

   (error-arg? ctx-base
               form))


  ([ctx form]

   (= :MEMORY
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-memory?*


  ([form]

   `(error-memory? ($.form/templ* ~form)))


  ([ctx form]

   `(error-memory? ~ctx
                   ($.form/templ* ~form))))



(defn error-nobody?

  "Returns true if the given form is evaluated to an `:NOBODY` error."


  ([form]

   (error-nobody? ctx-base
                  form))


  ([ctx form]

   (= :NOBODY
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-nobody?*


  ([form]

   `(error-nobody? ($.form/templ* ~form)))


  ([ctx form]

   `(error-nobody? ~ctx
                   ($.form/templ* ~form))))



(defn error-state?

  "Returns true if the given form is evaluated to an `:NOBODY` error."


  ([form]

   (error-state? ctx-base
                  form))


  ([ctx form]

   (= :STATE
      (:convex.error/code (error ctx
                                 form)))))



(defmacro error-state?*


  ([form]

   `(error-state? ($.form/templ* ~form)))


  ([ctx form]

   `(error-state? ~ctx
                  ($.form/templ* ~form))))



(defn like-clojure?

  "Returns true if applying `arg+` to `form` on the CVM produces the exact same result as
  `(apply f arg+)`"


  ([form]

   (like-clojure? ctx-base
                  form))


  ([ctx form]

   ($.test.util/eq (eval form)
                   ($.cvm.eval/result ctx
                                      form)))


  ([form f arg+]

   (like-clojure? ctx-base
                  form
                  f
                  arg+))


  ([ctx form f arg+]

   ($.test.util/eq (apply f
                          arg+)
                   ($.cvm.eval/result ctx
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

   ($.cvm.eval/log ctx
                   form)))



(defn result


  ([form]

   (result ctx-base
           form))


  ([ctx form]

   ($.cvm.eval/result ctx
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

   (let [ctx-2 ($.cvm.eval/ctx ctx
                               form)
         res   (-> ctx-2
                   $.cvm/result
                   $.cvm/as-clojure)]
     (or res
         ($.test.prop/fail {:convex.test/log (-> ctx-2
                                                 $.cvm/log
                                                 $.cvm/as-clojure)})))))



(defn value


  ([form]

   (value ctx-base
          form))


  ([ctx form]

   ($.cvm.eval/value ctx
                     form)))


;;;;;;;;;;


(def ctx-base

  "Base context to use for testing."

  ;; TODO. Needs 2 transactions because of: https://github.com/Convex-Dev/convex/issues/107

  (-> ($.cvm/ctx)
      ($.cvm.eval/ctx
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

                                (defn long-percentage

                                  [percent n]

                                  (long (floor (* percent
                                                  n))))

                                (defn some

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (let [x-2 (f x)]
                                                       (if x-2
                                                         (reduced x-2)
                                                         false)))
                                                   true
                                                   coll)))

                                (defn unused-address

                                  [addr]

                                  (if (account addr)
                                    (recur (address (inc (long addr))))
                                    addr))
                                )))))
      ($.cvm.eval/ctx
        '(import $ :as $))
      ($.cvm/set-juice 1e7)))
