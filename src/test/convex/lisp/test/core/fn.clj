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
    ($.test.eval/result (list 'fn?
                              form))))



(defn suite-fn-call

  "Tests different ways of calling `form` as a function with `arg+` and ensuring it always
   returns `ret`."

  [form arg+ ret]

  ($.test.prop/checkpoint*

    "Calling a function"

    ($.test.prop/mult*

      "Direct call"
      ($.test.eval/result (list '=
                                ret
                                (list* form
                                       arg+)))

      "Calling after interning"
      ($.test.prop/mult-result ($.test.eval/result ($.form/templ {'?call (list* 'f
                                                                                arg+)
                                                                  '?fn   form
                                                                  '?ret  ret}
                                                                 '(do
                                                                    (def f
                                                                         ?fn)
                                                                    [(fn? f)
                                                                     (= ?ret
                                                                        ?call)])))
                               ["Fn?"
                                "Equal"])

      "Calling as local binding"
      ($.test.prop/mult-result ($.test.eval/result ($.form/templ {'?call (list* 'f
                                                                                arg+)
                                                                  '?fn   form
                                                                  '?ret  ret}
                                                                 '(let [f ?fn]
                                                                    [(fn? f)
                                                                     (= ?ret
                                                                        ?call)])))
                               ["Fn?"
                                "Equal"]))))


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
