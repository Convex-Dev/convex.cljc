(ns convex.example.exec

  "Executing some Convex Lisp code against a test CVM."

  (:require [convex.cvm      :as $.cvm]
            [convex.clj      :as $.clj]
            [convex.clj.eval :as $.clj.eval]))


;;;;;;;;;;


(comment


  ;; Full cycle of executing code (context could be reused for further execution).
  ;;
  (let [;; It is convenient writing Convex Lisp as Clojure data
        form   '(+ 2 2)
        
        ;; Converting Clojure data to source code (a string)
        source ($.clj/src form)
        
        ;; Reading source code as Convex object
        code   ($.cvm/read source)
        
        ;; Creating a test context
        ctx    ($.cvm/ctx)
        
        ;; Using context for expanding, compiling, and running code
        ctx-2  (-> ($.cvm/expand ctx
                                 code)
                   $.cvm/compile
                   $.cvm/run)]

    ;; Getting result and converting to Clojure data
    (-> ctx-2
        $.cvm/result
        $.cvm/as-clojure))



  ;; Simplified execution, `eval` takes care of expansion, compilation, and execution
  ;;
  (-> ($.cvm/eval ($.cvm/ctx)
                  ($.cvm/read-form '(+ 2 2)))
      $.cvm/result
      $.cvm/as-clojure)



  ;; Creating a new context, modifying it by adding a couple of functions in the environment
  ;;
  (def base-ctx
       ($.clj.eval/ctx ($.cvm/ctx)
                       '(do
                          (defn my-inc [x] (+ x 1))
                          (defn my-dec [x] (- x 1)))))
  


  ;; Later, forking and reusing it ad libidum
  ;;
  (-> ($.cvm/eval ($.cvm/fork base-ctx)
                  ($.cvm/read-form '(= 42
                                       (my-dec (my-inc 42)))))
      $.cvm/result
      $.cvm/as-clojure)



  ;; Using a helper (takes care of handling the form, forking the context, and translating into Clojure)
  ;;
  (= 42
     ($.clj.eval/result base-ctx
                        '(my-dec (my-inc 42))))

  
  )
