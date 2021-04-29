(ns convex.lisp.test.core

  "Testing Convex Core by generating Convex Lisp forms as Clojure data, converting them to source,
   and evaling."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.util           :as $.test.util]))


;;;;;;;;;;


(defn prop-double

  ""

  [core-symbol]

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/number])]
                    (fn [x]
                      (double? ($.test.util/eval (list* core-symbol
                                                        x))))))



(defn prop-numeric

  ""

  [core-symbol]

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/long])]
                    (fn [x]
                      ($.test.util/prop+

                        "Numerical computation of longs must result in a long"
                        (double? ($.test.util/eval (list* core-symbol
                                                          x)))

                        "Numerical computation with at least one double must result in a double"
                        (not (double? ($.test.util/eval (list* core-symbol
                                                               (update x
                                                                       (rand-int (dec (count x)))
                                                                       double)))))))))
  
;;;;;;;;;;


(tc.ct/defspec -+

  (prop-numeric '+))



(tc.ct/defspec --

  (prop-numeric '-))



(tc.ct/defspec -div

  (prop-double '/))



(tc.ct/defspec -+

  (prop-numeric '+))
