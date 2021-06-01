(ns convex.break.eval

  "Mimicks directly `convex.lisp.eval`, hence the absence of docstrings. Those functions rely on [[ctx-base]]
   when no context is provided/
  
   Symbols ending with '*' designate a macro equivalent of a related function ([[result*]] for [[result]]) which
   template the given form using `convex.lisp.form/templ*`."

  {:author "Adam Helinski"}

  (:require [convex.break.prop :as $.break.prop]
            [convex.cvm        :as $.cvm]
            [convex.cvm.eval   :as $.cvm.eval]
            [convex.lisp       :as $.lisp]))


(declare ctx-base
         result)


;;;;;;;;;;


(defn ctx


  ([form]

   (convex.break.eval/ctx ctx-base
                          form))


  ([ctx form]

   ($.cvm.eval/ctx ctx
                   form)))



(defmacro ctx*


  ([form]

   `(convex.break.eval/ctx ($.lisp/templ* ~form)))


  ([ctx form]

   `(convex.break.eval/ctx  ~ctx
                            ($.lisp/templ* ~form))))



(defn exception


  ([form]

   (exception ctx-base
              form))


  ([ctx form]

   ($.cvm.eval/exception ctx
                         form)))



(defmacro exception*


  ([form]

   `(exception ($.lisp/templ* ~form)))


  ([ctx form]

   `(exception ~ctx
               ($.lisp/templ* ~form))))



(defn exception?


  ([form]

   (exception? ctx-base
           form))


  ([ctx form]

   ($.cvm.eval/exception? ctx
                          form)))



(defmacro exception?*


  ([form]

   `(exception? ($.lisp/templ* ~form)))


  ([ctx form]

   `(exception? ~ctx
                ($.lisp/templ* ~form))))



(defn error-arg?

  "Returns true if the given form is evaluated to an `:ARGUMENT` error."


  ([form]

   (error-arg? ctx-base
               form))


  ([ctx form]

   (= :ARGUMENT
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-arg?*


  ([form]

   `(error-arg? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-arg? ~ctx
                ($.lisp/templ* ~form))))




(defn error-arity?

  "Returns true if the given form is evaluated to an `:ARITY` error."


  ([form]

   (error-arity? ctx-base
                 form))


  ([ctx form]

   (= :ARITY
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-arity?*


  ([form]

   `(error-arity? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-arity? ~ctx
                  ($.lisp/templ* ~form))))



(defn error-cast?

  "Returns true if the given form is evaluated to a `:CAST` error."


  ([form]

   (error-cast? ctx-base
                form))


  ([ctx form]

   (= :CAST
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-cast?*


  ([form]

   `(error-cast? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-cast? ~ctx
                 ($.lisp/templ* ~form))))



(defn error-fund?

  "Returns true if the given form is evaluated to a `:FUNDS` error."


  ([form]

   (error-fund? ctx-base
                form))


  ([ctx form]

   (= :FUNDS
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-fund?*


  ([form]

   `(error-fund? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-fund? ~ctx
                 ($.lisp/templ* ~form))))



(defn error-memory?

  "Returns true if the given form is evaluated to an `:MEMORY` error."


  ([form]

   (error-arg? ctx-base
               form))


  ([ctx form]

   (= :MEMORY
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-memory?*


  ([form]

   `(error-memory? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-memory? ~ctx
                   ($.lisp/templ* ~form))))



(defn error-nobody?

  "Returns true if the given form is evaluated to an `:NOBODY` error."


  ([form]

   (error-nobody? ctx-base
                  form))


  ([ctx form]

   (= :NOBODY
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-nobody?*


  ([form]

   `(error-nobody? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-nobody? ~ctx
                   ($.lisp/templ* ~form))))



(defn error-state?

  "Returns true if the given form is evaluated to an `:NOBODY` error."


  ([form]

   (error-state? ctx-base
                  form))


  ([ctx form]

   (= :STATE
      (:convex.error/code (exception ctx
                                     form)))))



(defmacro error-state?*


  ([form]

   `(error-state? ($.lisp/templ* ~form)))


  ([ctx form]

   `(error-state? ~ctx
                  ($.lisp/templ* ~form))))



(defn like-clojure?

  "Returns true if applying `arg+` to `form` on the CVM produces the exact same result as
  `(apply f arg+)`"


  ([form]

   (like-clojure? ctx-base
                  form))


  ([ctx form]

   ($.lisp/= (eval form)
             ($.cvm.eval/result ctx
                                form)))


  ([form f arg+]

   (like-clojure? ctx-base
                  form
                  f
                  arg+))


  ([ctx form f arg+]

   ($.lisp/= (apply f
                    arg+)
             ($.cvm.eval/result ctx
                                (list* form
                                       arg+)))))



(defmacro like-clojure?*


  ([form]

   `(like-clojure? ($.lisp/templ* ~form)))


  ([ctx form]

   `(like-clojure? ~ctx
                   ($.lisp/templ* ~form)))


  ([form f arg+]

   `(like-clojure? ($.lisp/templ* ~form)
                   ~f
                   ~arg+))


  ([ctx form f arg+]

   `(like-clojure? ~ctx
                   ($.lisp/templ* ~form)
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

   `(result ($.lisp/templ* ~form)))


  ([ctx form]

   `(result ~ctx
            ($.lisp/templ* ~form))))



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
         ($.break.prop/fail {:convex.test/log (-> ctx-2
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

  (-> ($.cvm/import {"src/convex/break/util.cvx" '$})
      ($.cvm/set-juice 1e7)))
