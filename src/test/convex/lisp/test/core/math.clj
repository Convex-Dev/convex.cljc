(ns convex.lisp.test.core.math

  "Testing Convex Core math functions."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.util           :as $.test.util]))


;;;;;;;;;; Arithmetic operators


(tc.ct/defspec *--

  ($.test.prop/arithmetic '*))



(tc.ct/defspec +--

  ($.test.prop/arithmetic '+))



(tc.ct/defspec ---

  ($.test.prop/arithmetic '-))



(tc.ct/defspec div--

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/number]
                     (fn [x]
                       (double? ($.test.eval/form (list* '/
                                                         x))))))


;;;;;;;;;; Comparators


(tc.ct/defspec <--

  ($.test.prop/comparison '<
                          <))



(tc.ct/defspec <=--

  ($.test.prop/comparison '<=
                          <=))



(tc.ct/defspec =--

  ($.test.prop/comparison '=
                          =))



(tc.ct/defspec >=--

  ($.test.prop/comparison '>=
                          >=))



(tc.ct/defspec >--

  ($.test.prop/comparison '>
                          >))



(tc.ct/defspec max--

  ;; In case of equal inputs, Clojure favors the last argument whereas Convex favors the first one.
  ;; 
  ;; (max 1 1.0)  =>  1.0 in Clojure, 1 in Convex

  ($.test.prop/comparison 'max
                          (fn [& arg+]
                            (apply max
                                   (reverse arg+)))))



(tc.ct/defspec min--

  ;; See comment for [[max--]].

  ($.test.prop/comparison 'min
                          (fn [& arg+]
                            (apply min
                                   (reverse arg+)))))


;;;;;;;;;; Exponentiation


(tc.ct/defspec exp--

  ($.test.prop/like-clojure 'exp
                            #(StrictMath/exp %)
                            [:tuple :convex/number]))



(tc.ct/defspec pow--

  ($.test.prop/check [:tuple
                      :convex/number
                      :convex/number]
                     (fn [[x y]]
                       ($.test.util/eq (StrictMath/pow x
                                                       y)
                                       ($.test.eval/form (list 'pow
                                                               x
                                                               y))))))



(tc.ct/defspec sqrt--

  ($.test.prop/like-clojure 'sqrt
                            #(StrictMath/sqrt %)
                            [:tuple :convex/number]))


;;;;;;;;;; Increment / decrement


(tc.ct/defspec dec--double

  ;; Unintuitive behavior. When sufficiently small double, is cast to 0.
  ;; Not small enough, get cast to `Long/MIN_VALUE` and underflows.

  ($.test.prop/check [:double
                      {:min (double Long/MIN_VALUE)}]
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'dec
                                                         x))]
                         ($.test.prop/mult*
                            
                            "Result is always a long"
                            (int? x-2)

                            "Decrement higher than maximum long"
                            (if (>= x
                                    Long/MAX_VALUE)
                              (= x-2
                                 (dec Long/MAX_VALUE))
                              true)

                            "Decrement in long range"
                            (if (< Long/MIN_VALUE
                                   x
                                  Long/MAX_VALUE)
                             (= x-2
                                (dec (long x)))
                             true))))))



(t/deftest dec--double-underflow

  (t/is (= Long/MAX_VALUE
           ($.test.eval/form (list 'dec
                                   (double Long/MIN_VALUE))))))



(tc.ct/defspec dec--long

  ($.test.prop/check :convex/long
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'dec
                                                         x))]
                         ($.test.prop/mult*

                           "Result is always a long"
                           (int? x-2)

                           "Decrement or underflow"
                           (= x-2
                              (if (= x
                                     Long/MIN_VALUE)
                                Long/MAX_VALUE
                                (dec x))))))))



(tc.ct/defspec inc--double

  ;; See [[dec-double]].

  ($.test.prop/check [:double
                      {:min (double Long/MIN_VALUE)}]
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'inc
                                                         x))]
                         ($.test.prop/mult*

                           "Result is always a long"
                           (int? x-2)

                           "Overflow"
                           (if (>= x
                                   Long/MAX_VALUE)
                             (= x-2
                                Long/MIN_VALUE)
                             true)

                           "Increment in long range"
                           (if (< Long/MIN_VALUE
                                  x
                                  Long/MAX_VALUE)
                             (= x-2
                                (inc (long x)))
                             true))))))



(tc.ct/defspec inc--long

  ($.test.prop/check :convex/long
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'inc
                                                         x))]
                         ($.test.prop/mult*

                           "Result is always a long"
                           (int? x-2)

                           "Increment or overflow"
                           (= x-2
                              (if (= x
                                     Long/MAX_VALUE)
                                Long/MIN_VALUE
                                (inc x))))))))


;;;;;;;;;; Rounding


(tc.ct/defspec ceil--

  ($.test.prop/like-clojure 'ceil
                            #(StrictMath/ceil %)
                            [:tuple :convex/number]))



(tc.ct/defspec floor--

  ($.test.prop/like-clojure 'floor
                            #(StrictMath/floor %)
                            [:tuple :convex/number]))


;;;;;;;;;; Sign operations


(tc.ct/defspec abs--

  ($.test.prop/check :convex/number
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'abs
                                                         x))]
                         ($.test.prop/mult*

                           "Must be positive"
                           (>= x-2
                               0)

                           "Type is preserved"
                           (= (type x-2)
                              (type x)))))))



(tc.ct/defspec signum--

  ($.test.prop/check :convex/number
                     (fn [x]
                       (let [x-2 ($.test.eval/form (list 'signum
                                                         x))]
                         ($.test.prop/mult*

                           "Negative"
                           (if (neg? x)
                             (= -1
                                x-2)
                             true)

                           "Positive"
                           (if (pos? x)
                             (= 1
                                x-2)
                             true)

                           "Zero"
                           (if (zero? x)
                             (zero? x-2)
                             true))))))
