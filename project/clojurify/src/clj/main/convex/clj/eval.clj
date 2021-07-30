(ns convex.clj.eval

  "Shortcuts for evaluating Convex Lisp code, useful for development and testing.

   Takes a form (Clojure data expressing Convex Lisp code) and evaluates to some result returned as Clojure data.
  
   This namespace offers many flavors of how exceptions and results are handled and returned.

   Primarily useful for dev and testing.

   Given context is always forked, meaning the argument is left intact. See [[convex.cvm/fork]]."

  {:author "Adam Helinski"}

  (:require [convex.cvm           :as $.cvm]
            [convex.clj           :as $.clj]
            [convex.clj.translate :as $.clj.translate]
            [convex.cell          :as $.cell]
            [convex.read          :as $.read]))


(declare result)


;;;;;;;;;; Default context


(def ^:dynamic *ctx-default*

  "Holder for a default context which can set whenever suited or dynamically with Clojure's `binding`.

   Nil by default.

   See [[alter-ctx-default]]."

  nil)



(defn alter-ctx-default

  "Sets the value of *ctx-default*."

  [ctx]

  (alter-var-root #'*ctx-default*
                  (constantly ctx)))


;;;;;;;;;;



(defn ctx

  "Evaluates the given `form` and returns `ctx`.
  
   Uses [[*ctx-default*]] if `ctx` is not provided."


  ([form]

   (convex.clj.eval/ctx *ctx-default*
                        form))


  ([ctx form]

   ($.cvm/eval ($.cvm/fork ctx)
               (-> form
                   $.clj/src
                   $.read/string))))



(defmacro ctx*


  ([form]

   `(convex.clj.eval/ctx ($.clj/templ* ~form)))


  ([ctx form]

   `(convex.clj.eval/ctx ~ctx
                         ($.clj/templ* ~form))))


;;;;;;;;;;


(defn code

  "Like [[exception]] but returns an exception only if it matches the given `code`.
  
   See [[convex.cell/code-std*]]."

  
  ([code form]

   (convex.clj.eval/code *ctx-default*
                         code
                         form))


  ([ctx code form]

   (->> (convex.clj.eval/ctx ctx
                             form)
        ($.cvm/exception code)
        $.clj.translate/cvx->clj)))



(defn- ^:private -code

  ;; Helper for macros which converts the given `code` to an exception code if it is a keyword.

  [code]

  (if (keyword? code)
    `($.cell/code-std* ~code)
    code))



(defmacro code*

  "Note: if `code` is a keyword, it is passed to [[convex.cell/code-std*]]."

  ([code form]

   `(convex.clj.eval/code ~(-code code)
                          ($.clj/templ* ~form)))


  ([ctx code form]

   `(convex.clj.eval/code ~ctx
                          ~(-code code)
                          ($.clj/templ* ~form))))



(defn code?

  "Like [[code]] but returns a boolean indicating if an exception of the given `code occured."

  
  ([code form]

   (code? *ctx-default*
          code
          form))


  ([ctx code form]

   (->> (convex.clj.eval/ctx ctx
                             form)
        ($.cvm/exception? code))))



(defmacro code?*

  "Note: if `code` is a keyword, it is passed to [[convex.cell/code-std*]]."

  ([code form]

   `(convex.clj.eval/code? ~(-code code)
                           ($.clj/templ* ~form)))


  ([ctx code form]

   `(convex.clj.eval/code? ~ctx
                           ~(-code code)
                           ($.clj/templ* ~form))))



(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."


  ([form]

   (exception *ctx-default*
              form))


  ([ctx form]

   (-> (convex.clj.eval/ctx ctx
                            form)
       $.cvm/exception
       $.clj.translate/cvx->clj)))



(defmacro exception*


  ([form]

   `(exception ($.clj/templ* ~form)))


  ([ctx form]

   `(exception ~ctx
               ($.clj/templ* ~form))))



(defn exception?

  "Like [[ctx]] but returns a boolean indicating if an exception occured."


  ([form]

   (exception? *ctx-default*
               form))


  ([ctx form]

   (-> (convex.clj.eval/ctx ctx
                            form)
       $.cvm/exception?)))



(defmacro exception?*


  ([form]

   `(exception? ($.clj/templ* ~form)))


  ([ctx form]

   `(exception? ~ctx
                ($.clj/templ* ~form))))



(defn like-clojure?

  "Returns true if applying `arg+` to `form` on the CVM produces the exact same result as
  `(apply f arg+)`
  
   Alternatively, a unique `form` can be given for both execution environments. In that case,
   the form is processed in Clojure via `eval`.
  
   ```clojure
   (like-clojure? '(+ 2 2))

   (like-clojure? '+
                  +
                  [2 2)
   ```"


  ([form]

   (like-clojure? *ctx-default*
                  form))


  ([ctx form]

   ($.clj/= (eval form)
            (result ctx
                    form)))


  ([form f arg+]

   (like-clojure? *ctx-default*
                  form
                  f
                  arg+))


  ([ctx form f arg+]

   ($.clj/= (apply f
                   arg+)
            (result ctx
                    (list* form
                           arg+)))))



(defmacro like-clojure?*


  ([form]

   `(like-clojure? ($.clj/templ* ~form)))


  ([ctx form]

   `(like-clojure? ~ctx
                   ($.clj/templ* ~form)))


  ([form f arg+]

   `(like-clojure? ($.clj/templ* ~form)
                   ~f
                   ~arg+))


  ([ctx form f arg+]

   `(like-clojure? ~ctx
                   ($.clj/templ* ~form)
                   ~f
                   ~arg+)))



(let [src     (fn [form]
                ($.clj/templ* (log {:form   (quote ~form)
                                    :return ~form})))
      process (fn [ctx]
                (-> ctx
                    $.cvm/log
                    $.clj.translate/cvx->clj))]
  (defn log

    "Like [[ctx]] but returns the context log as Clojure data structure, where the last entry for the executing
     address is a map containing the given `form` as well as its return value.
    
     Useful for debugging, akin to using `println` with Clojure."


    ([form]

     (-> (convex.clj.eval/ctx (src form))
         process))


    ([ctx form]

     (-> (convex.clj.eval/ctx ctx
                              (src form))
         process))))



(defmacro log*


  ([form]

   `(log ($.clj/templ* ~form)))


  ([ctx form]

   `(log ~ctx
         ($.clj/templ* ~form))))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."


  ([form]

   (result *ctx-default*
           form))


  ([ctx form]

   (-> (convex.clj.eval/ctx ctx
                            form)
       $.cvm/result
       $.clj.translate/cvx->clj)))



(defmacro result*


  ([form]

   `(result ($.clj/templ* ~form)))


  ([ctx form]

   `(result ~ctx
            ($.clj/templ* ~form))))



(defn value

  "Like [[ctx]] but returns either an [[exception]] or a [[result]]."
  

  ([form]

   (value *ctx-default*
          form))


  ([ctx form]

   (let [ctx-2     (convex.clj.eval/ctx ctx
                                        form)
         exception ($.cvm/exception ctx-2)]
     (-> (if (nil? exception)
           ($.cvm/result ctx-2)
           exception)
         $.clj.translate/cvx->clj))))



(defmacro value*


  ([form]

   `(value ($.clj/templ* ~form)))


  ([ctx form]

   `(value ~ctx
           ($.clj/templ* ~form))))
