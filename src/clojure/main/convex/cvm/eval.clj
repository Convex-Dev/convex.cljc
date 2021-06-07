(ns convex.cvm.eval

  "Shortcuts for evaluating Convex Lisp code, useful for development and testing.

   Deals with form (ie. Clojure data expressing Convex Lisp code), whereas [[convex.cvm.eval.src]] deals
   with source code (ie. strings).
  
   Given context is always forked, meaning the argument is left intact. See [[convex.cvm/fork]]."

  {:author "Adam Helinski"}

  (:require [convex.cvm          :as $.cvm]
            [convex.cvm.eval.src :as $.cvm.eval.src]
            [convex.lisp         :as $.lisp]))


;;;;;;;;;;


(defn ctx

  "Evaluates the given `form` and returns `ctx`."


  ([form]

   ($.cvm.eval.src/ctx ($.lisp/src form)))


  ([ctx form]

   ($.cvm.eval.src/ctx ctx
                       ($.lisp/src form))))



(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."


  ([form]

   ($.cvm.eval.src/exception ($.lisp/src form)))


  ([ctx form]

   ($.cvm.eval.src/exception ctx
                             ($.lisp/src form))))



(defn exception?

  "Like [[ctx]] but returns a boolean indicating if an exception occured."

  
  ([form]

   ($.cvm.eval.src/exception? ($.lisp/src form)))


  ([ctx form]
   
   ($.cvm.eval.src/exception? ctx
                              ($.lisp/src form))))



(let [src     (fn [form]
                ($.lisp/src ($.lisp/templ* (log {:form   (quote ~form)
                                                 :return ~form}))))
      process (fn [ctx]
                (-> ctx
                    $.cvm/log
                    $.cvm/as-clojure))]
  (defn log

    "Like [[ctx]] but returns the context log as Clojure data structure, where the last entry for the executing
     address is a map containing the given `form` as well as its return value.
    
     Useful for debugging, akin to using `println` with Clojure."


    ([form]

     (-> ($.cvm.eval.src/ctx (src form))
         process))


    ([ctx form]

     (-> ($.cvm.eval.src/ctx ctx
                             (src form))
         process))))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."


  ([form]

   ($.cvm.eval.src/result ($.lisp/src form)))


  ([ctx form]

   ($.cvm.eval.src/result ctx
                          ($.lisp/src form))))



(defn value

  "Like [[ctx]] but returns either an [[exception]] or a [[result]]."
  
  ([form]

   ($.cvm.eval.src/value ($.lisp/src form)))


  ([ctx form]

   ($.cvm.eval.src/value ctx
                         ($.lisp/src form))))
