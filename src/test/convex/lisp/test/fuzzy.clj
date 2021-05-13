(ns convex.lisp.test.fuzzy

  "Fuzzing testing random forms.
  
   The CVM should be resilient. A random form either succeeds or fails, but no exception should be
   thrown and unhandled."

  {:author "Adam Helinski"}

  (:require [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur resiliciency

  ($.test.prop/check :convex.core/call
                     (fn [x]
                       ($.test.eval/value x)
                       true)))
