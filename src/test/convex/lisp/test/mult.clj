(ns convex.lisp.test.mult

  ""

  {:author "Adam Helinski"}

  (:require [convex.lisp           :as $]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


(defn fn?-

  ""

  [subprop+ form]

  (conj subprop+
        ["Function?"
         #($.test.eval/form (list 'fn?
                                  form))]))



(defn fn-call

  ""

  [subprop+ form arg+ ret]

  (conj subprop+
        ["Direct call"
         #($.test.eval/form (list '=
                                  ret
                                  (list* form
                                         arg+)))]

        ["Calling after interning"
         #($.test.prop/mult-result ($.test.eval/form ($/templ {'?call (list* 'f
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
                                    "Equal"])]

        ["Calling as local binding"
         #($.test.prop/mult-result ($.test.eval/form ($/templ {'?call (list* 'f
                                                                             arg+)
                                                               '?fn   form
                                                               '?ret  ret}
                                                              '(let [f ?fn]
                                                                 [(fn? f)
                                                                  (= ?ret
                                                                     ?call)])))
                                   ["Fn?"
                                    "Equal"])]))
