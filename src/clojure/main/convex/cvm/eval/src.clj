(ns convex.cvm.eval.src

  "Mimicks [[convex.cvm.eval]] but for evaluating Convex Lisp source, strings of code."

  ;; In reality, it is the opposite. This namespace does all the work while [[convex.cvm.eval]] delegates to it?

  {:author "Adam Helinski"}

  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(def ^:dynamic *ctx-default*

  "Holder for a default context which can set at start-up or dynamically with Clojure's `binding`.

   For preparing one at start-up, set the `CVM_CTX` environment variable to a qualified symbol which points to a
   context producing function. This function is called without argument when this namespace is required.

   For instance, [[convex.cvm/ctx]] creates a basic context. Hence, if it does not need further preparation:

   ```bash
   $ env CVM_CTX='convex.cvm/ctx'  clojure ...
   ```"

  (when-some [ctx-string (not-empty (System/getenv "CVM_CTX"))]
    (try
      ((requiring-resolve (symbol ctx-string)))
      (catch Throwable e
        (throw (ex-info (str "While trying to produce a default CVM context with: "
                             ctx-string)
                        {::env ctx-string}
                        e))))))


;;;;;;;;;;


(defn ctx

  "Reads Convex Lisp source, evaluates it, and returns `ctx`.
  
   Uses [[convex.cvm/*ctx-default*]] if `ctx` is not provided."


  ([src]

   (ctx *ctx-default*
        src))

   
  ([ctx src]

   ($.cvm/eval ($.cvm/fork ctx)
               ($.cvm/read src))))



(defn exception

  "Like [[ctx]] but returns the current exception or nil if there is none."


  ([src]

   (exception *ctx-default*
              src))


  ([ctx src]

   (-> (convex.cvm.eval.src/ctx ctx
                                src)
       $.cvm/exception
       $.cvm/as-clojure)))



(defn exception?

  "Like [[ctx]] but returns a boolean indicating if an exception occured."


  ([src]

   (exception? *ctx-default*
               src))


  ([ctx src]

   (-> (convex.cvm.eval.src/ctx ctx
                                src)
       $.cvm/exception?)))



(defn result

  "Like [[ctx]] but returns the result as Clojure data."


  ([src]

   (result *ctx-default*
           src))


  ([ctx src]

   (-> (convex.cvm.eval.src/ctx ctx
                                src)
       $.cvm/result
       $.cvm/as-clojure)))



(defn value

  "Like [[ctx]] but returns either an [[exception]] or a [[result]]."
  

  ([src]

   (value *ctx-default*
          src))


  ([ctx src]

   (let [ctx-2     (convex.cvm.eval.src/ctx ctx
                                            src)
         exception ($.cvm/exception ctx-2)]
     (-> (if (nil? exception)
           ($.cvm/result ctx-2)
           exception)
         $.cvm/as-clojure))))
