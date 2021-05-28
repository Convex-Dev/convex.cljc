(ns convex.cvm.eval

  "Shortcuts for evaluating Convex Lisp code, useful for development and testing.

   Deals with form (ie. Clojure data expressing Convex Lisp code), whereas [[convex.cvm.eval.src]] deals
   with source code (ie. strings).
  
   Given context is always forked, meaning the argument is left intact. See [[convex.cvm/fork]]."

  {:author "Adam Helinski"}

  (:require [convex.cvm          :as $.cvm]
            [convex.cvm.eval.src :as $.cvm.eval.src]
            [convex.lisp         :as $.lisp]
            [convex.lisp.form    :as $.form]))


;;;;;;;;;;


(defn ctx

  "Evaluates the given `form` and returns `ctx`."

  [ctx form]

  ($.cvm.eval.src/ctx ctx
                      ($.form/src form)))



(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."

  [ctx form]

  ($.cvm.eval.src/exception ctx
                            ($.form/src form)))



(defn exception?

  "Like [[ctx]] but returns a boolean indicating if an exception occured."

  [ctx form]
   
  ($.cvm.eval.src/exception? ctx
                             ($.form/src form)))



(defn log

  "Like [[ctx]] but returns the context log as Clojure data structure, where the last entry for the executing
   address is a map containing the given `form` as well as its return value.
  
   Useful for debugging, akin to using `println` with Clojure."

  [ctx form]

  (-> ($.cvm.eval.src/ctx ctx
                          ($.form/src ($.form/templ {'?form form}
                                                    '(log {:form   '?form
                                                           :return ?form}))))
      $.cvm/log
      $.cvm/as-clojure))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."

  [ctx form]

  ($.cvm.eval.src/result ctx
                         ($.form/src form)))



(defn value

  "Like [[ctx]] but returns either an [[exception]] or a [[result]]."
  
  [ctx form]

  ($.cvm.eval.src/value ctx
                        ($.form/src form)))
