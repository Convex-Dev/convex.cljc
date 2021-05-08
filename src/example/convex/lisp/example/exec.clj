(ns convex.lisp.example.exec

  "Executing some Convex Lisp code against a test CVM."

  (:require [convex.lisp]
            [convex.lisp.form]))


;;;;;;;;;;


(comment


  ;; Full cycle of executing code (context could be reused for further execution).
  ;;
  (let [;; It is convenient writing Convex Lisp as Clojure data
        form   '(+ 2 2)
        
        ;; Converting Clojure data to source code (a string)
        source (convex.lisp.form/source form)
        
        ;; Reading source code as Convex object
        code   (convex.lisp/read source)
        
        ;; Creating a test context
        ctx    (convex.lisp.ctx/create-fake)
        
        ;; Using context for expanding, compiling, and running code
        ctx-2  (-> (convex.lisp.ctx/expand ctx
                                           code)
                   convex.lisp.ctx/compile
                   convex.lisp.ctx/run)]

    ;; Getting result and converting to Clojure data
    (-> ctx-2
        convex.lisp.ctx/result
        convex.lisp/datafy))



  ;; Simplified executing
  ;;
  (->> '(+ 2 2)
       convex.lisp/read-form
       (convex.lisp.ctx/eval (convex.lisp.ctx/create-fake))
       convex.lisp.ctx/result
       convex.lisp/datafy)
  )
