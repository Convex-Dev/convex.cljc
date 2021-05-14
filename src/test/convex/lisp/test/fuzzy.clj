(ns convex.lisp.test.fuzzy

  "Fuzzing testing random forms.
  
   The CVM should be resilient. A random form either succeeds or fails, but no exception should be
   thrown and unhandled."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;;


($.test.prop/deftest ^:recur resiliciency

  ($.test.prop/check :convex.core/call
                     (fn [x]
                       ($.test.eval/value x)
                       true)))



($.test.prop/deftest ^:recur error

  ($.test.prop/check [:and
                      [:cat
                       :convex.core/symbol
                       [:* :convex/data]]
                      [:fn (fn [x]
                             (not ($.test.schema/valid? :convex.core/result
                                                        x)))]]
                     (fn [x]
                       (println :form (list* (first x)
                                             (map $.form/quoted
                                                  (rest x))))
                       ($.test.eval/error? (list* (first x)
                                                  (map $.form/quoted
                                                       (rest x)))))))



($.test.prop/deftest ^:fuzz result

  ($.test.prop/check :convex.core/result
                     (fn [x]
                       ($.test.eval/result x)
                       true)))
