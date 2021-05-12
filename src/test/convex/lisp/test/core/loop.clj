(ns convex.lisp.test.core.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;; 


($.test.prop/deftest ^:recur reduce--

  ($.test.prop/check [:and
                      ;; TODO. Should be all collections but fails because of sets: https://github.com/Convex-Dev/convex/issues/109
                      ;:convex/collection
                      :convex/vector
                      [:fn #(pos? (count %))]]
                     (fn [x]
                       ($.test.eval/result ($.form/templ {'?i (rand-int (count x))
                                                          '?x x}
                                                         '(let [x '?x
                                                                v (nth x
                                                                       '?i)]
                                                            (= v
                                                               (reduce (fn [acc item]
                                                                         (if (= item
                                                                                v)
                                                                           (reduced item)
                                                                           acc))
                                                                       :convex-sentinel
                                                                       x))))))))
