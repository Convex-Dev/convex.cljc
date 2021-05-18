(ns convex.lisp.test.core.fn

  "Testing creating functions and calling them."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Suites


(defn suite-fn?

  "Tests is `form` evaluates to a function."

  [form]

  ($.test.prop/checkpoint*

    "`fn?`"
    ($.test.eval/result* (fn? ~form))))



(defn suite-fn-call

  "Tests different ways of calling `form` as a function with `arg+` and ensuring it always
   returns `ret`."

  [form arg+ ret]

  (let [ctx ($.test.eval/ctx* (def ret
                                   ~ret))]
    ($.test.prop/checkpoint*

      "Calling a function"

      ($.test.prop/mult*

        #_#_"Direct call"
        ($.test.eval/result* ctx
                             (= ret
                                (~form ~@arg+)))

        "After def"
        (let [ctx-2 ($.test.eval/ctx* ctx
                                      (def f
                                           ~form))]
          ($.test.prop/mult*

            "`fn?`"
            ($.test.eval/result ctx-2
                                '(fn? f))

            "Calling"
            ($.test.eval/result* ctx-2
                                 (= ret
                                    (f ~@arg+)))))

        "From `let`, `fn?`"
        ($.test.eval/result* ctx
                             (let [f ~form]
                               (fn? f)))

        "From `let`, calling"
        ($.test.eval/result* ctx
                             (let [f ~form]
                               (= ret
                                  (f ~@arg+))))))))


;;;;;;;;;; Tests


($.test.prop/deftest ^:recur fn--arg-0

  ;; Calling no-arg functions.

  ($.test.prop/check :convex/data
                     (fn [x]
                      (let [x-2     ($.form/quoted x)
                            fn-form (list 'fn
                                           []
                                           x-2)]
                        ($.test.prop/and* (suite-fn? fn-form)
                                          (suite-fn-call fn-form
                                                         nil
                                                         x-2))))))



($.test.prop/deftest ^:recur fn--arg-fixed

  ;; Calling functions with a fixed number of arguments.

  ($.test.prop/check ($.test.schema/binding+ 1)
                     (fn [x]
                       (let [arg+     (mapv (comp $.form/quoted
                                                  second)
                                            x)
                             binding+ (mapv first
                                            x)
                             fn-form  (list 'fn
                                            binding+
                                            binding+)]
                         ($.test.prop/and* (suite-fn? fn-form)
                                           (suite-fn-call fn-form
                                                          arg+
                                                          arg+))))))



($.test.prop/deftest ^:recur fn--variadic

  ;; Calling functions with a variadic number of arguments.

  ($.test.prop/check ($.test.schema/binding+ 1)
                     (fn [x]
                       (let [arg+       (mapv (comp $.form/quoted
                                                    second)
                                              x)
                             binding+   (mapv first
                                              x)
                             pos-amper  (rand-int (count binding+))
                             binding-2+ (vec (concat (take pos-amper
                                                           binding+)
                                                     ['&]
                                                     (drop pos-amper
                                                           binding+)))
                             fn-form    (list 'fn
                                              binding-2+
                                              binding+)]
                         ($.test.prop/mult*
                           
                           "Right number of arguments"
                           ($.test.prop/and* (suite-fn? fn-form)
                                             (suite-fn-call fn-form
                                                            arg+
                                                            (update arg+
                                                                    pos-amper
                                                                    vector)))

                           "1 argument less"
                           (suite-fn-call fn-form
                                          (vec (concat (take pos-amper
                                                             arg+)
                                                       (drop (inc pos-amper)
                                                             arg+)))
                                          (assoc arg+
                                                 pos-amper
                                                 []))
                            
                           "Extra argument"
                           (suite-fn-call fn-form
                                          (vec (concat (take pos-amper
                                                             arg+)
                                                       [42]
                                                       (drop pos-amper
                                                             arg+)))
                                          (update arg+
                                                  pos-amper 
                                                  #(vector 42
                                                           %))))))))
