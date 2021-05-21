(ns convex.lisp.example.exec

  "Executing some Convex Lisp code against a test CVM."

  (:require [convex.lisp]
            [convex.lisp.eval]
            [convex.lisp.eval.src]
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



  ;; Simplified execution
  ;;
  (->> '(+ 2 2)
       convex.lisp/read-form
       (convex.lisp.ctx/eval (convex.lisp.ctx/create-fake))
       convex.lisp.ctx/result
       convex.lisp/datafy)

  ;; Using helpers
  ;;
  (= 4
     (convex.lisp.eval/result (convex.lisp.ctx/create-fake)
                              '(+ 2 2))
     (convex.lisp.eval.src/result (convex.lisp.ctx/create-fake)
                                  "(+ 2 2)"))



  ;; Creating a new context, modifying it by adding a couple of functions in the environment
  ;;
  (def base-ctx
       (convex.lisp.eval/ctx (convex.lisp.ctx/create-fake)
                             '(do
                                (defn my-inc [x] (+ x 1))
                                (defn my-dec [x] (- x 1)))))
  
  ;; Later, forking and reusing it ad libidum
  ;;
  (convex.lisp.eval/result base-ctx
                           '(= 42
                               (my-dec (my-inc 42))))
  
  )
