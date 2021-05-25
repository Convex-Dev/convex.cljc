(ns convex.lisp.test.fuzz

  "Fuzzing testing random forms.
  
   The CVM should be resilient. A random form either succeeds or fails, but no exception should be
   thrown and unhandled."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;;


#_($.test.prop/deftest ^:recur error

  ;; Generating forms that are known to result in a CVM error.

  ($.test.prop/check [:and
                      [:cat
                       :convex.core/symbol
                       [:* :convex/data]]
                      [:fn (fn [x]
                             (not ($.test.schema/valid? :convex.core/result
                                                        x)))]]
                     (fn [x]
                       #_(println :form (list* (first x)
                                             (map $.form/quoted
                                                  (rest x))))
                       ($.test.eval/error? (list* (first x)
                                                  (map $.form/quoted
                                                       (rest x)))))))



($.test.prop/deftest random

  ;; Generating randorm forms that should either fail or succeed on the CVM, but no
  ;; JVM exception should be thrown without being handled.

  (TC.prop/for-all [form ($.gen/random-call)]
    ($.test.eval/value form)
    true))



#_($.test.prop/deftest ^:fuzz result

  ;; Generating forms that are known to lead to a successful result.

  ($.test.prop/check :convex.core/result
                     (fn [x]
                       ($.test.eval/result x)
                       true)))
