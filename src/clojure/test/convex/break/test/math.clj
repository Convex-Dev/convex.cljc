(ns convex.break.test.math

  "Testing Convex core math functions."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as t]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.gen              :as $.break.gen]
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

      ($.break.eval/result* (long? (~form ~@x+)))


      "Numerical computation with at least one double must result in a double"

      (double? ($.break.eval/result* (~form ~@(update x+
                                                      (rand-int (dec (count x+)))
                                                      double)))))))



(defn prop-comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f]

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/number
                                      1
                                      16)]
    ($.break.eval/like-clojure? form
                                f
                                x+)))


;;;;;;;;;; Arithmetic operators


(mprop/deftest *--

  (prop-arithmetic '*))



(mprop/deftest +--

  (prop-arithmetic '+))



(mprop/deftest ---

  (prop-arithmetic '-))



(mprop/deftest div--

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/number
                                      1
                                      16)]
    (double? ($.break.eval/result* (/ ~@x+)))))


;;;;;;;;;; Comparators


(mprop/deftest <--

  (prop-comparison '<
                   <))



(mprop/deftest <=--

  (prop-comparison '<=
                   <=))



(mprop/deftest ==--

  (prop-comparison '==
                   ==))



(mprop/deftest >=--

  (prop-comparison '>=
                   >=))



(mprop/deftest >--

  (prop-comparison '>
                   >))



(mprop/deftest max--

  ;; In case of equal inputs, Clojure favors the last argument whereas Convex favors the first one.
  ;; 
  ;; (max 1 1.0)  =>  1.0 in Clojure, 1 in Convex

  (prop-comparison 'max
                   (fn [& arg+]
                     (apply max
                            (reverse arg+)))))



(mprop/deftest min--

  ;; See comment for [[max--]].

  (prop-comparison 'min
                   (fn [& arg+]
                     (apply min
                            (reverse arg+)))))


;;;;;;;;;; Exponentiation


(mprop/deftest exp--

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.break.eval/like-clojure? 'exp
                                #(StrictMath/exp %)
                                [x])))



(mprop/deftest pow--

  (TC.prop/for-all [x $.lisp.gen/number
                    y $.lisp.gen/number]
    ($.break.eval/like-clojure? 'pow
                                #(StrictMath/pow %1
                                                 %2)
                                [x
                                 y])))



(mprop/deftest sqrt--

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.break.eval/like-clojure? 'sqrt
                                #(StrictMath/sqrt %)
                                [x])))


;;;;;;;;;; Increment / decrement


(mprop/deftest dec--

  (TC.prop/for-all [x $.lisp.gen/long]
    (let [ctx ($.break.eval/ctx* (def dec-
                                      (dec ~x)))]
      (mprop/mult

        "Returns a long"

        ($.break.eval/result ctx
                             '(long? dec-))


        "Consistent with `-`"

        ($.break.eval/result* ctx
                              (= dec-
                                 (- ~x
                                    1)))


        "Consisent with `+`"

        ($.break.eval/result* ctx
                              (= dec-
                                 (+ ~x
                                    -1)))


        "Decrement or underflow"

        (= ($.break.eval/result ctx
                                'dec-)
           (if (= x
                  Long/MIN_VALUE)
             Long/MAX_VALUE
             (dec x)))))))



(mprop/deftest inc--

  (TC.prop/for-all [x $.lisp.gen/long]
    (let [ctx ($.break.eval/ctx* (def inc-
                                      (inc ~x)))]
      (mprop/mult

        "Returns a long"

        ($.break.eval/result ctx
                             '(long? inc-))


        "Consistent with `-`"

        ($.break.eval/result* ctx
                              (= inc-
                                 (- ~x
                                    -1)))
        

        "Consistent with `+`"

        ($.break.eval/result* ctx
                              (= inc-
                                 (+ ~x
                                    1)))


        "Increment or overflow"

        (= ($.break.eval/result ctx
                                'inc-)
           (if (= x
                  Long/MAX_VALUE)
             Long/MIN_VALUE
             (inc x)))))))


;;;;;;;;;; Integer operations


(mprop/deftest euclidian-div

  ;; Testing `mod` and `quot`.

  (TC.prop/for-all [a $.lisp.gen/long
                    b (TC.gen/such-that #(not (zero? %))
                                        $.lisp.gen/long)]
    (let [ctx ($.break.eval/ctx* (do
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

        ($.break.eval/result ctx
                             '(long? -mod))


        "`quot` produces a long"

        ($.break.eval/result ctx
                             '(long? -quot))


        "`rem` produces a long"

        ($.break.eval/result ctx
                             '(long? -rem))


        "`quot` is consistent with Clojure"

        (= (quot a
                 b)
           ($.break.eval/result ctx
                                '-quot))


        "`rem` is consistent with Clojure"

        (= (rem a
                b)
           ($.break.eval/result ctx
                                '-rem))


        "`quot` and `rem` are consistent"

        ($.break.eval/result ctx
                             '(= a
                                 (+ -rem
                                    (* b
                                       -quot))))))))


;;;;;;;;;; Miscellaneous


(mprop/deftest zero?--false

  (TC.prop/for-all [x (TC.gen/such-that #(not (and (number? %)
                                                   (zero? %)))
                                        $.lisp.gen/any)]
    ($.break.eval/result* (not (zero? ~x)))))



(t/deftest zero?--true

  (t/is ($.break.eval/result '(zero? 0))))


;;;;;;;;;; Rounding


(mprop/deftest ceil--

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.break.eval/like-clojure? 'ceil
                                #(StrictMath/ceil %)
                                [x])))



(mprop/deftest floor--

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.break.eval/like-clojure? 'floor
                                #(StrictMath/floor %)
                                [x])))


;;;;;;;;;; Sign operations


(mprop/deftest abs--

  (TC.prop/for-all [x (TC.gen/such-that #(not (Double/isNaN %))
                                        $.lisp.gen/number)]
    (let [ctx ($.break.eval/ctx* (def abs-
                                      (abs ~x)))]
      (mprop/mult

        "Must be positive"

        ($.break.eval/result* ctx
                              (>= abs-
                                  0))


        "Type is preserved"

        (= (type ($.break.eval/result ctx
                                      'abs-))
           (type x))))))



(t/deftest abs--NaN

  (t/is (Double/isNaN ($.break.eval/result '(abs ##NaN)))))




(mprop/deftest signum--

  (TC.prop/for-all [x $.lisp.gen/number]
    ($.break.eval/result* (= ~x
                             (* (abs ~x)
                                (signum ~x))))))


;;;;;;;;;; Failing cases


(mprop/deftest error-cast-long-1

  ;; Functions that should accept only one long argument.
  
  (TC.prop/for-all [x $.break.gen/not-long]
    (mprop/mult

      "`dec`"

      ($.break.eval/error-cast?* (dec ~x))


      "`inc`"

      ($.break.eval/error-cast?* (inc ~x)))))



(mprop/deftest error-cast-long-2

  ;; Functions that should accept only two long arguments.
  
  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-long
                                     b (TC.gen/one-of [$.lisp.gen/any
                                                       $.lisp.gen/long])]
                          (TC.gen/shuffle [a b]))]
    (mprop/mult

      "`mod`"

      ($.break.eval/error-cast?* (mod ~a
                                      ~b))


      "`rem`"

      ($.break.eval/error-cast?* (rem ~a
                                      ~b))


      "`quot`"

      ($.break.eval/error-cast?* (quot ~a
                                       ~b)))))



(mprop/deftest error-cast-number-1

  ;; Functions with one argument that should accept a number only.

  (TC.prop/for-all [x $.break.gen/not-number]
    (mprop/mult

      "`abs`"

      ($.break.eval/error-cast?* (abs ~x))


      "`ceil`"

      ($.break.eval/error-cast?* (ceil ~x))


      "`exp`"

      ($.break.eval/error-cast?* (exp ~x))


      "`floor`"

      ($.break.eval/error-cast?* (floor ~x))


      "`signum`"

      ($.break.eval/error-cast?* (signum ~x))


      "`sqrt`"
      ($.break.eval/error-cast?* (sqrt ~x)))))



(mprop/deftest error-cast-number-2

  ;; Functions that should accept only two number arguments

  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-number
                                     b (TC.gen/one-of [$.lisp.gen/any
                                                       $.lisp.gen/number])]
                          (TC.gen/shuffle [a b]))]
    (mprop/check

      "`pow`"

      ($.break.eval/error-cast?* (pow ~a
                                      ~b)))))



(mprop/deftest error-cast-variadic

  ;; Functions that accepts a variadic number of number arguments only.
  ;;
  ;; Comparison functions are variadic but are tested in [[error-cast-number-2]] since
  ;; they test argument 2 by 2 (which would even succeed in these negative tests).

  (TC.prop/for-all [x+ (TC.gen/let [a  $.break.gen/not-number
                                    b+ (TC.gen/vector (TC.gen/one-of [$.lisp.gen/any
                                                                      $.lisp.gen/number])
                                                      0
                                                      7)]
                         (TC.gen/shuffle (cons a
                                               b+)))]
    (mprop/mult

      "`*`"

      ($.break.eval/error-cast?* (* ~@x+))


      "`+`"

      ($.break.eval/error-cast?* (+ ~@x+))


      "`-`"

      ($.break.eval/error-cast?* (- ~@x+))


      "`/`"

      ($.break.eval/error-cast?* (/ ~@x+))


      "`max`"

      ($.break.eval/error-cast?* (max ~@x+))


      "`min`"

      ($.break.eval/error-cast?* (min ~@x+))


      "Relative comparators"

      (let [x-2+ (sort-by number?
                          x+)]
        (mprop/mult

          "`<`"

          ($.break.eval/error-cast?* (< ~@x-2+))

    
          "`<=`"

          ($.break.eval/error-cast?* (<= ~@x-2+))

    
          "`==`"

          ($.break.eval/error-cast?* (== ~@x-2+))


          "`>=`"

          ($.break.eval/error-cast?* (>= ~@x-2+))

    
          "`>`"

          ($.break.eval/error-cast?* (> ~@x-2+)))))))
