(ns convex.lisp.example.exec

  "Executing some Convex Lisp code against a test CVM."

  (:require [convex.cvm]
            [convex.cvm.eval]
            [convex.cvm.eval.src]
            [convex.lisp]
            [convex.lisp.form]))


;;;;;;;;;;


(comment


  ;; Full cycle of executing code (context could be reused for further execution).
  ;;
  (let [;; It is convenient writing Convex Lisp as Clojure data
        form   '(+ 2 2)
        
        ;; Converting Clojure data to source code (a string)
        source (convex.lisp.form/src form)
        
        ;; Reading source code as Convex object
        code   (convex.lisp/read source)
        
        ;; Creating a test context
        ctx    (convex.cvm/ctx)
        
        ;; Using context for expanding, compiling, and running code
        ctx-2  (-> (convex.cvm/expand ctx
                                      code)
                   convex.cvm/compile
                   convex.cvm/run)]

    ;; Getting result and converting to Clojure data
    (-> ctx-2
        convex.cvm/result
        convex.lisp/datafy))



  ;; Simplified execution
  ;;
  (-> (convex.cvm/eval (convex.cvm/ctx)
                       (convex.lisp/read-form '(+ 2 2)))
      convex.cvm/result
      convex.lisp/datafy)



  ;; Creating a new context, modifying it by adding a couple of functions in the environment
  ;;
  (def base-ctx
       (convex.cvm.eval/ctx (convex.cvm/ctx)
                            '(do
                               (defn my-inc [x] (+ x 1))
                               (defn my-dec [x] (- x 1)))))
  


  ;; Later, forking and reusing it ad libidum
  ;;
  (-> (convex.cvm/eval (convex.cvm/fork base-ctx)
                       (convex.lisp/read-form '(= 42
                                                  (my-dec (my-inc 42)))))
      convex.cvm/result
      convex.lisp/datafy)



  ;; Using helpers (takes care of forking and datafying)
  ;;
  (= 4
     (convex.cvm.eval/result (convex.cvm/ctx)
                             '(+ 2 2))
     (convex.cvm.eval.src/result (convex.cvm/ctx)
                                 "(+ 2 2)"))

  
  )
