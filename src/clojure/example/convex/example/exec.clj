(ns convex.example.exec

  "Executing some Convex Lisp code against a test CVM."

  (:require [convex.cvm]
            [convex.clj.eval]
            [convex.clj]))


;;;;;;;;;;


(comment


  ;; Full cycle of executing code (context could be reused for further execution).
  ;;
  (let [;; It is convenient writing Convex Lisp as Clojure data
        form   '(+ 2 2)
        
        ;; Converting Clojure data to source code (a string)
        source (convex.clj/src form)
        
        ;; Reading source code as Convex object
        code   (convex.cvm/read source)
        
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
        convex.cvm/as-clojure))



  ;; Simplified execution
  ;;
  (-> (convex.clj.eval (convex.cvm/ctx)
                       (convex.cvm/read-form '(+ 2 2)))
      convex.cvm/result
      convex.cvm/as-clojure)



  ;; Creating a new context, modifying it by adding a couple of functions in the environment
  ;;
  (def base-ctx
       (convex.clj.eval/ctx (convex.cvm/ctx)
                            '(do
                               (defn my-inc [x] (+ x 1))
                               (defn my-dec [x] (- x 1)))))
  


  ;; Later, forking and reusing it ad libidum
  ;;
  (-> (convex.clj.eval (convex.cvm/fork base-ctx)
                       (convex.cvm/read-form '(= 42
                                                 (my-dec (my-inc 42)))))
      convex.cvm/result
      convex.cvm/as-clojure)



  ;; Using a helper (takes care of handling the form, forking the context, and translating into Clojure)
  ;;
  (= 4
     (convex.clj.eval/result base-ctx
                             '(my-dec (my-inc 42))))

  
  )
