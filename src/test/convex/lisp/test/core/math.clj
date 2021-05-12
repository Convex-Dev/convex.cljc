(ns convex.lisp.test.core.math

  "Testing Convex core math functions."

  ;; TODO. Failing tests must be written when implicit cast of chars, booleans, and addresses is dealt with.
  ;;
  ;;       https://github.com/Convex-Dev/convex/issues/68
  ;;	   https://github.com/Convex-Dev/convex/issues/73
  ;;       https://github.com/Convex-Dev/convex/issues/89

  {:author "Adam Helinski"}

  (:require [clojure.test          :as t]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]
            [convex.lisp.test.util :as $.test.util]))


;;;;;;;;;; Reusing properties


(defn prop-arithmetic

  "Checks applying a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/long]
                     (fn [x]
                       ($.test.prop/mult*
                         
                         "Numerical computation of longs must result in a long"
                         (int? ($.test.eval/result (list* form
                                                          x)))

                         "Numerical computation with at least one double must result in a double"
                         (double? ($.test.eval/result (list* form
                                                             (update x
                                                                     (rand-int (dec (count x)))
                                                                     double))))))))



(defn prop-comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f]

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/number]
                     (partial $.test.eval/like-clojure?
                              form
                              f)))


;;;;;;;;;; Arithmetic operators


($.test.prop/deftest *--

  (prop-arithmetic '*))



($.test.prop/deftest +--

  (prop-arithmetic '+))



($.test.prop/deftest ---

  (prop-arithmetic '-))



($.test.prop/deftest div--

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/number]
                     (fn [x]
                       (double? ($.test.eval/result (list* '/
                                                           x))))))


;;;;;;;;;; Comparators


($.test.prop/deftest <--

  (prop-comparison '<
                   <))



($.test.prop/deftest <=--

  (prop-comparison '<=
                   <=))



($.test.prop/deftest =--

  (prop-comparison '=
                   =))



($.test.prop/deftest >=--

  (prop-comparison '>=
                   >=))



($.test.prop/deftest >--

  (prop-comparison '>
                   >))



#_($.test.prop/deftest max--

  ;; In case of equal inputs, Clojure favors the last argument whereas Convex favors the first one.
  ;; 
  ;; (max 1 1.0)  =>  1.0 in Clojure, 1 in Convex

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/99

  (prop-comparison 'max
                   (fn [& arg+]
                     (apply max
                            (reverse arg+)))))



#_($.test.prop/deftest min--

  ;; See comment for [[max--]].

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/99

  (prop-comparison 'min
                   (fn [& arg+]
                     (apply min
                            (reverse arg+)))))


;;;;;;;;;; Exponentiation


($.test.prop/deftest exp--

  ($.test.prop/check [:tuple :convex/number]
                     (partial $.test.eval/like-clojure?
                              'exp
                              #(StrictMath/exp %))))



($.test.prop/deftest pow--

  ($.test.prop/check [:tuple
                      :convex/number
                      :convex/number]
                     (fn [[x y]]
                       ($.test.util/eq (StrictMath/pow x
                                                       y)
                                       ($.test.eval/result (list 'pow
                                                                 x
                                                                 y))))))



#_($.test.prop/deftest ^:recur pow--fail

  ;; TODO. Failing, see https://github.com/Convex-Dev/convex/issues/89.

  ($.test.prop/check [:and
                      [:vector
                       {:max 2
                        :min 2}
                       :convex/data]
                      [:fn
                       #(not (every? number?
                                     %))]]
                     (fn [[x y]]
                       ($.test.eval/exceptional (list 'pow
                                                      x
                                                      y)))))



($.test.prop/deftest sqrt--

  ($.test.prop/check [:tuple :convex/number]
                     (partial $.test.eval/like-clojure?
                              'sqrt
                              #(StrictMath/sqrt %))))


;;;;;;;;;; Increment / decrement


($.test.prop/deftest dec--double

  ;; Unintuitive behavior. When sufficiently small double, is cast to 0.
  ;; Not small enough, get cast to `Long/MIN_VALUE` and underflows.

  ($.test.prop/check [:double
                      {:min (double Long/MIN_VALUE)}]
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'dec
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
           ($.test.eval/result (list 'dec
                                     (double Long/MIN_VALUE))))))



($.test.prop/deftest dec--long

  ($.test.prop/check :convex/long
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'dec
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



($.test.prop/deftest inc--double

  ;; See [[dec-double]].

  ($.test.prop/check [:double
                      {:min (double Long/MIN_VALUE)}]
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'inc
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



($.test.prop/deftest inc--long

  ($.test.prop/check :convex/long
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'inc
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


($.test.prop/deftest ceil--

  ($.test.prop/check [:tuple :convex/number]
                     (partial $.test.eval/like-clojure?
                              'ceil
                              #(StrictMath/ceil %))))



($.test.prop/deftest floor--

  ($.test.prop/check [:tuple :convex/number]
                     (partial $.test.eval/like-clojure?
                              'floor
                              #(StrictMath/floor %))))


;;;;;;;;;; Sign operations


($.test.prop/deftest abs--

  ($.test.prop/check [:and
                      :convex/number
                      [:fn
                       #(not (Double/isNaN %))]]
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'abs
                                                           x))]
                         ($.test.prop/mult*

                           "Must be positive"
                           (>= x-2
                               0)

                           "Type is preserved"
                           (= (type x-2)
                              (type x)))))))



(t/deftest abs--NaN

  (t/is (Double/isNaN ($.test.eval/result '(abs ##NaN)))))




#_($.test.prop/deftest signum--

  ;; TODO. Fail because of: https://github.com/Convex-Dev/convex/issues/100

  ($.test.prop/check :convex/number
                     (fn [x]
                       (let [x-2 ($.test.eval/result (list 'signum
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
