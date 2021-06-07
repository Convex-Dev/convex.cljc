(ns convex.break.test.math

  "Testing Convex core math functions."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as t]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.gen              :as $.break.gen]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Reusing properties


(defn prop-arithmetic

  "Checks applying a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/long
                                      1
                                      16)]
    (mprop/mult
      
      "Numerical computation of longs must result in a long"

      ($.cvm.eval/result* (long? (~form ~@x+)))


      "Numerical computation with at least one double must result in a double"

      (double? ($.cvm.eval/result* (~form ~@(update x+
                                                    (rand-int (dec (count x+)))
                                                    double)))))))



(defn prop-comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f]

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/number
                                      1
                                      16)]
    ($.cvm.eval/like-clojure? form
                              f
                              x+)))


;;;;;;;;;; Arithmetic operators


(mprop/deftest *--

  {:ratio-num 100}

  (prop-arithmetic '*))



(mprop/deftest +--

  {:ratio-num 100}

  (prop-arithmetic '+))



(mprop/deftest ---

  {:ratio-num 100}

  (prop-arithmetic '-))



(mprop/deftest div--

  {:ratio-num 100}

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/number
                                      1
                                      16)]
    (double? ($.cvm.eval/result* (/ ~@x+)))))


;;;;;;;;;; Comparators


(mprop/deftest <--

  {:ratio-num 100}

  (prop-comparison '<
                   <))



(mprop/deftest <=--

  {:ratio-num 100}

  (prop-comparison '<=
                   <=))



(mprop/deftest ==--

  {:ratio-num 100}

  (prop-comparison '==
                   ==))



(mprop/deftest >=--

  {:ratio-num 100}

  (prop-comparison '>=
                   >=))



(mprop/deftest >--

  {:ratio-num 100}

  (prop-comparison '>
                   >))



(mprop/deftest max--

  ;; In case of equal inputs, Clojure favors the last argument whereas Convex favors the first one.
  ;; 
  ;; (max 1 1.0)  =>  1.0 in Clojure, 1 in Convex

  {:ratio-num 100}

  (prop-comparison 'max
                   (fn [& arg+]
                     (apply max
                            (reverse arg+)))))



(mprop/deftest min--

  ;; See comment for [[max--]].

  {:ratio-num 100}

  (prop-comparison 'min
                   (fn [& arg+]
                     (apply min
                            (reverse arg+)))))


;;;;;;;;;; Exponentiation


(mprop/deftest exp--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.cvm.eval/like-clojure? 'exp
                              #(StrictMath/exp %)
                              [x])))



(mprop/deftest pow--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number
                    y $.lisp.gen/number]
    ($.cvm.eval/like-clojure? 'pow
                              #(StrictMath/pow %1
                                               %2)
                              [x
                               y])))



(mprop/deftest sqrt--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.cvm.eval/like-clojure? 'sqrt
                              #(StrictMath/sqrt %)
                              [x])))


;;;;;;;;;; Increment / decrement


(mprop/deftest dec--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/long]
    (let [ctx ($.cvm.eval/ctx* (def dec-
                                    (dec ~x)))]
      (mprop/mult

        "Returns a long"

        ($.cvm.eval/result ctx
                           '(long? dec-))


        "Consistent with `-`"

        ($.cvm.eval/result* ctx
                            (= dec-
                               (- ~x
                                  1)))


        "Consisent with `+`"

        ($.cvm.eval/result* ctx
                            (= dec-
                               (+ ~x
                                  -1)))


        "Decrement or underflow"

        (= ($.cvm.eval/result ctx
                              'dec-)
           (if (= x
                  Long/MIN_VALUE)
             Long/MAX_VALUE
             (dec x)))))))



(mprop/deftest inc--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/long]
    (let [ctx ($.cvm.eval/ctx* (def inc-
                                    (inc ~x)))]
      (mprop/mult

        "Returns a long"

        ($.cvm.eval/result ctx
                           '(long? inc-))


        "Consistent with `-`"

        ($.cvm.eval/result* ctx
                            (= inc-
                               (- ~x
                                  -1)))
        

        "Consistent with `+`"

        ($.cvm.eval/result* ctx
                            (= inc-
                               (+ ~x
                                  1)))


        "Increment or overflow"

        (= ($.cvm.eval/result ctx
                              'inc-)
           (if (= x
                  Long/MAX_VALUE)
             Long/MIN_VALUE
             (inc x)))))))


;;;;;;;;;; Integer operations


(mprop/deftest euclidian-div

  ;; `mod` and `quot`.

  {:ratio-num 20}

  (TC.prop/for-all [a $.lisp.gen/long
                    b (TC.gen/such-that #(not (zero? %))
                                        $.lisp.gen/long)]
    (let [ctx ($.cvm.eval/ctx* (do
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
      (mprop/mult

        "`mod` produces a long"

        ($.cvm.eval/result ctx
                           '(long? -mod))


        "`quot` produces a long"

        ($.cvm.eval/result ctx
                           '(long? -quot))


        "`rem` produces a long"

        ($.cvm.eval/result ctx
                           '(long? -rem))


        "`quot` is consistent with Clojure"

        (= (quot a
                 b)
           ($.cvm.eval/result ctx
                             '-quot))


        "`rem` is consistent with Clojure"

        (= (rem a
                b)
           ($.cvm.eval/result ctx
                            '-rem))


        "`quot` and `rem` are consistent"

        ($.cvm.eval/result ctx
                           '(= a
                               (+ -rem
                                  (* b
                                     -quot))))))))


;;;;;;;;;; Miscellaneous


(mprop/deftest zero?--false

  {:ratio-num 20}

  (TC.prop/for-all [x (TC.gen/such-that #(not (and (number? %)
                                                   (zero? %)))
                                        $.lisp.gen/any)]
    ($.cvm.eval/result* (not (zero? ~x)))))



(t/deftest zero?--true

  (t/is ($.cvm.eval/result '(zero? 0))))


;;;;;;;;;; Rounding


(mprop/deftest ceil--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.cvm.eval/like-clojure? 'ceil
                              #(StrictMath/ceil %)
                              [x])))



(mprop/deftest floor--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.cvm.eval/like-clojure? 'floor
                              #(StrictMath/floor %)
                              [x])))


;;;;;;;;;; Sign operations


(mprop/deftest abs--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/such-that #(not (Double/isNaN %))
                                        $.lisp.gen/number)]
    (let [ctx ($.cvm.eval/ctx* (def abs-
                                    (abs ~x)))]
      (mprop/mult

        "Must be positive"

        ($.cvm.eval/result* ctx
                            (>= abs-
                                0))


        "Type is preserved"

        (= (type ($.cvm.eval/result ctx
                                    'abs-))
           (type x))))))



(t/deftest abs--NaN

  (t/is (Double/isNaN ($.cvm.eval/result '(abs ##NaN)))))




(mprop/deftest signum--

  {:ratio-num 100}

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.cvm.eval/result* (= ~x
                           (* (abs ~x)
                              (signum ~x))))))


;;;;;;;;;; Failing cases


(mprop/deftest error-cast-long-1

  ;; Functions that should accept only one long argument.
  
  {:ratio-num 20}

  (TC.prop/for-all [x $.break.gen/not-long]
    (mprop/mult

      "`dec`"

      ($.cvm.eval/code?* :CAST
                         (dec ~x))


      "`inc`"

      ($.cvm.eval/code?* :CAST
                         (inc ~x)))))



(mprop/deftest error-cast-long-2

  ;; Functions that should accept only two long arguments.

  {:ratio-num 20}
  
  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-long
                                     b (TC.gen/one-of [$.lisp.gen/any
                                                       $.lisp.gen/long])]
                          (TC.gen/shuffle [a b]))]
    (mprop/mult

      "`mod`"

      ($.cvm.eval/code?* :CAST
                         (mod ~a
                              ~b))


      "`rem`"

      ($.cvm.eval/code?* :CAST
                         (rem ~a
                              ~b))


      "`quot`"

      ($.cvm.eval/code?* :CAST
                         (quot ~a
                               ~b)))))



(mprop/deftest error-cast-number-1

  ;; Functions with one argument that should accept a number only.

  {:ratio-num 10}

  (TC.prop/for-all [x $.break.gen/not-number]
    (mprop/mult

      "`abs`"

      ($.cvm.eval/code?* :CAST
                         (abs ~x))


      "`ceil`"

      ($.cvm.eval/code?* :CAST
                         (ceil ~x))


      "`exp`"

      ($.cvm.eval/code?* :CAST
                         (exp ~x))


      "`floor`"

      ($.cvm.eval/code?* :CAST
                         (floor ~x))


      "`signum`"

      ($.cvm.eval/code?* :CAST
                         (signum ~x))


      "`sqrt`"
      ($.cvm.eval/code?* :CAST
                         (sqrt ~x)))))



(mprop/deftest error-cast-number-2

  ;; Functions that should accept only two number arguments

  {:ratio-num 20}

  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-number
                                     b (TC.gen/one-of [$.lisp.gen/any
                                                       $.lisp.gen/number])]
                          (TC.gen/shuffle [a b]))]
    (mprop/check

      "`pow`"

      ($.cvm.eval/code?* :CAST
                         (pow ~a
                              ~b)))))



(mprop/deftest error-cast-variadic

  ;; Functions that accepts a variadic number of number arguments only.
  ;;
  ;; Comparison functions are variadic but are tested in [[error-cast-number-2]] since
  ;; they test argument 2 by 2 (which would even succeed in these negative tests).

  {:ratio-num 5}

  (TC.prop/for-all [x+ (TC.gen/let [a  $.break.gen/not-number
                                    b+ (TC.gen/vector (TC.gen/one-of [$.lisp.gen/any
                                                                      $.lisp.gen/number])
                                                      0
                                                      7)]
                         (TC.gen/shuffle (cons a
                                               b+)))]
    (mprop/mult

      "`*`"

      ($.cvm.eval/code?* :CAST
                         (* ~@x+))


      "`+`"

      ($.cvm.eval/code?* :CAST
                         (+ ~@x+))


      "`-`"

      ($.cvm.eval/code?* :CAST
                         (- ~@x+))


      "`/`"

      ($.cvm.eval/code?* :CAST
                         (/ ~@x+))


      "Relative comparators"

      (let [x-2+ (sort-by number?
                          x+)]
        (mprop/mult

          "`<`"

          ($.cvm.eval/code?* :CAST
                             (< ~@x-2+))

    
          "`<=`"

          ($.cvm.eval/code?* :CAST
                             (<= ~@x-2+))

    
          "`==`"

          ($.cvm.eval/code?* :CAST
                             (== ~@x-2+))


          "`>=`"

          ($.cvm.eval/code?* :CAST
                             (>= ~@x-2+))

    
          "`>`"

          ($.cvm.eval/code?* :CAST
                             (> ~@x-2+))


          "`max`"

          ($.cvm.eval/code?* :CAST
                             (max ~@x-2+))


          "`min`"

          ($.cvm.eval/code?* :CAST
                             (min ~@x-2+)))))))
