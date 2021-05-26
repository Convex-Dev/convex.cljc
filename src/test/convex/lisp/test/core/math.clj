(ns convex.lisp.test.core.math

  "Testing Convex core math functions."

  ;; TODO. Failing tests must be written when implicit cast of chars, booleans, and addresses is dealt with.
  ;;
  ;;       https://github.com/Convex-Dev/convex/issues/68
  ;;	   https://github.com/Convex-Dev/convex/issues/73
  ;;       https://github.com/Convex-Dev/convex/issues/89

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as t]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]
            [convex.lisp.test.util         :as $.test.util]))


;;;;;;;;;; Reusing properties


(defn prop-arithmetic

  "Checks applying a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/long
                                      1
                                      16)]
    ($.test.prop/mult*
      
      "Numerical computation of longs must result in a long"
      ($.test.eval/result* (long? (~form ~@x+)))

      "Numerical computation with at least one double must result in a double"
      (double? ($.test.eval/result* (~form ~@(update x+
                                                     (rand-int (dec (count x+)))
                                                     double)))))))



(defn prop-comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f]

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/number
                                      1
                                      16)]
    ($.test.eval/like-clojure? form
                               f
                               x+)))


;;;;;;;;;; Arithmetic operators


($.test.prop/deftest *--

  (prop-arithmetic '*))



($.test.prop/deftest +--

  (prop-arithmetic '+))



($.test.prop/deftest ---

  (prop-arithmetic '-))



($.test.prop/deftest div--

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/number
                                      1
                                      16)]
    (double? ($.test.eval/result* (/ ~@x+)))))


;;;;;;;;;; Comparators


($.test.prop/deftest <--

  (prop-comparison '<
                   <))



($.test.prop/deftest <=--

  (prop-comparison '<=
                   <=))



($.test.prop/deftest ==--

  (prop-comparison '==
                   ==))



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

  (TC.prop/for-all [x $.gen/number]
    ($.test.eval/like-clojure? 'exp
                               #(StrictMath/exp %)
                               [x])))



($.test.prop/deftest pow--

  (TC.prop/for-all [x $.gen/number
                    y $.gen/number]
    ($.test.eval/like-clojure? 'pow
                               #(StrictMath/pow %1
                                                %2)
                               [x
                                y])))



($.test.prop/deftest sqrt--

  (TC.prop/for-all [x $.gen/number]
    ($.test.eval/like-clojure? 'sqrt
                               #(StrictMath/sqrt %)
                               [x])))


;;;;;;;;;; Increment / decrement


($.test.prop/deftest dec--

  (TC.prop/for-all [x $.gen/long]
    (let [ctx ($.test.eval/ctx* (def dec-
                                     (dec ~x)))]
      ($.test.prop/mult*

        "Returns a long"
        ($.test.eval/result ctx
                            '(long? dec-))

        "Consistent with `-`"
        ($.test.eval/result* ctx
                             (= dec-
                                (- ~x
                                   1)))

        "Consisent with `+`"
        ($.test.eval/result* ctx
                             (= dec-
                                (+ ~x
                                   -1)))

        "Decrement or underflow"
        (= ($.test.eval/result ctx
                               'dec-)
           (if (= x
                  Long/MIN_VALUE)
             Long/MAX_VALUE
             (dec x)))))))



($.test.prop/deftest inc--

  (TC.prop/for-all [x $.gen/long]
    (let [ctx ($.test.eval/ctx* (def inc-
                                     (inc ~x)))]
      ($.test.prop/mult*

        "Returns a long"
        ($.test.eval/result ctx
                            '(long? inc-))

        "Consistent with `-`"
        ($.test.eval/result* ctx
                             (= inc-
                                (- ~x
                                   -1)))
        
        "Consistent with `+`"
        ($.test.eval/result* ctx
                             (= inc-
                                (+ ~x
                                   1)))

        "Increment or overflow"
        (= ($.test.eval/result ctx
                               'inc-)
           (if (= x
                  Long/MAX_VALUE)
             Long/MIN_VALUE
             (inc x)))))))


;;;;;;;;;; Integer operations


($.test.prop/deftest euclidian-div

  ;; Testing `mod` and `quot`.

  (TC.prop/for-all [a $.gen/long
                    b (TC.gen/such-that #(not (zero? %))
                                        $.gen/long)]
    (let [ctx ($.test.eval/ctx* (do
                                  (def a
                                       ~a)
                                  (def b
                                       ~b)
                                  (def -mod
                                       (mod a
                                            b))
                                  (def -quot
                                       (quot a
                                             b))
                                  (def -rem
                                       (rem a
                                            b))))]
      ($.test.prop/mult*

        "`mod` produces a long"
        ($.test.eval/result ctx
                            '(long? -mod))

        "`quot` produces a long"
        ($.test.eval/result ctx
                            '(long? -quot))

        "`rem` produces a long"
        ($.test.eval/result ctx
                            '(long? -rem))

        "`quot` is consistent with Clojure"
        (= (quot a
                 b)
           ($.test.eval/result ctx
                               '-quot))

        "`rem` is consistent with Clojure"
        (= (rem a
                b)
           ($.test.eval/result ctx
                               '-rem))

        "`quot` and `rem` are consistent"
        ($.test.eval/result ctx
                            '(= a
                                (+ -rem
                                   (* b
                                      -quot))))))))


;;;;;;;;;; Miscellaneous


($.test.prop/deftest zero?--false

  (TC.prop/for-all [x (TC.gen/such-that #(not (and (number? %)
                                                   (zero? %)))
                                        $.gen/any)]
    ($.test.eval/result* (not (zero? ~x)))))



(t/deftest zero?--true

  (t/is ($.test.eval/result '(zero? 0))))


;;;;;;;;;; Rounding


($.test.prop/deftest ceil--

  (TC.prop/for-all [x $.gen/number]
    ($.test.eval/like-clojure? 'ceil
                               #(StrictMath/ceil %)
                               [x])))



($.test.prop/deftest floor--

  (TC.prop/for-all [x $.gen/number]
    ($.test.eval/like-clojure? 'floor
                               #(StrictMath/floor %)
                               [x])))


;;;;;;;;;; Sign operations


($.test.prop/deftest abs--

  (TC.prop/for-all [x (TC.gen/such-that #(not (Double/isNaN %))
                                        $.gen/number)]
    (let [ctx ($.test.eval/ctx* (def abs-
                                     (abs ~x)))]
      ($.test.prop/mult*

        "Must be positive"
        ($.test.eval/result* ctx
                             (>= abs-
                                 0))

        "Type is preserved"
        (= (type ($.test.eval/result ctx
                                     'abs-))
           (type x))))))



(t/deftest abs--NaN

  (t/is (Double/isNaN ($.test.eval/result '(abs ##NaN)))))




#_($.test.prop/deftest signum--

  ;; TODO. Fail because of: https://github.com/Convex-Dev/convex/issues/147

  (TC.prop/for-all [x $.gen/number]
    ($.test.eval/result* (= ~x
                            (* (abs ~x)
                               (signum ~x))))))


;;;;;;;;;; Failing cases


($.test.prop/deftest error-cast-long-1

  ;; Functions that should accept only one long argument.
  
  (TC.prop/for-all [x $.test.gen/not-long]
    ($.test.prop/mult*

      "`dec`"
      ($.test.eval/error-cast?* (dec ~x))

      "`inc`"
      ($.test.eval/error-cast?* (inc ~x)))))



($.test.prop/deftest error-cast-long-2

  ;; Functions that should accept only two long arguments.
  
  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.test.gen/not-long
                                     b (TC.gen/one-of [$.gen/any
                                                       $.gen/long])]
                          (TC.gen/shuffle [a b]))]
    ($.test.prop/mult*

      "`mod`"
      ($.test.eval/error-cast?* (mod ~a
                                     ~b))

      "`rem`"
      ($.test.eval/error-cast?* (rem ~a
                                     ~b))

      "`quot`"
      ($.test.eval/error-cast?* (quot ~a
                                      ~b)))))



($.test.prop/deftest error-cast-number-1

  ;; Functions with one argument that should accept a number only.

  ;; TODO. Follow: https://github.com/Convex-Dev/convex/issues/154

  (TC.prop/for-all [x $.test.gen/not-number]
    ($.test.prop/mult*

      ;; "`abs`"
      ;; ($.test.eval/error-cast?* (abs ~x))

      "`ceil`"
      ($.test.eval/error-cast?* (ceil ~x))

      "`exp`"
      ($.test.eval/error-cast?* (exp ~x))

      "`floor`"
      ($.test.eval/error-cast?* (floor ~x))

      ;; "`signum`"
      ;; ($.test.eval/error-cast?* (signum ~x))

      "`sqrt`"
      ($.test.eval/error-cast?* (sqrt ~x)))))



($.test.prop/deftest error-cast-number-2

  ;; Functions that should accept only two number arguments
  ;;
  ;; Comparison functions are variadic but they test arguments 2 by 2.

  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.test.gen/not-number
                                     b (TC.gen/one-of [$.gen/any
                                                       $.gen/number])]
                          (TC.gen/shuffle [a b]))]
    ($.test.prop/mult*

      "`pow`"
      ($.test.eval/error-cast?* (pow ~a
                                     ~b)))))



($.test.prop/deftest error-cast-variadic

  ;; Functions that accepts a variadic number of number arguments only.
  ;;
  ;; Comparison functions are variadic but are tested in [[error-cast-number-2]] since
  ;; they test argument 2 by 2 (which would even succeed in these negative tests).

  (TC.prop/for-all [x+ (TC.gen/let [a  $.test.gen/not-number
                                    b+ (TC.gen/vector (TC.gen/one-of [$.gen/any
                                                                      $.gen/number])
                                                      0
                                                      7)]
                         (TC.gen/shuffle (cons a
                                               b+)))]
    ($.test.prop/mult*

      "`*`"
      ($.test.eval/error-cast?* (* ~@x+))

      "`+`"
      ($.test.eval/error-cast?* (+ ~@x+))

      "`-`"
      ($.test.eval/error-cast?* (- ~@x+))

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/154
      ;;
      ;; "`/`"
      ;; ($.test.eval/error-cast?* (/ ~@x+))

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/157
      ;;
      ;; "`==`"
      ;; ($.test.eval/error-cast?* (== ~@x+)))

      "`max`"
      ($.test.eval/error-cast?* (max ~@x+))

      "`min`"
      ($.test.eval/error-cast?* (min ~@x+))

      ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/157
      ;;
      ;; "Relative comparators"
      ;; (let [x-2+ (sort-by number?
      ;;                     x+)]
      ;;   ($.test.prop/mult*

      ;;     "`<`"
      ;;     ($.test.eval/error-cast?* (< ~@x-2+))
    
      ;;     "`<=`"
      ;;     ($.test.eval/error-cast?* (<= ~@x-2+))
    
      ;;     "`>=`"
      ;;     ($.test.eval/error-cast?* (>= ~@x-2+))
    
      ;;     "`>`"
      ;;     ($.test.eval/error-cast?* (> ~@x-2+))))
      )))
