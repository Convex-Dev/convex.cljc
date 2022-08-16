(ns convex.test.break.math

  "Testing Convex core math functions."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.break.gen              :as $.break.gen]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Helpers


(defn ex-cast?
  
  "Does the given form result in a `:CAST` CVM exception?"

  [form]

  (= ($.cell/code-std* :CAST)
     ($.eval/exception-code $.break/ctx
                            form)))


;;;;;;;;;; Reusing properties


(defn prop-arithmetic

  "Checks applying a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  (TC.prop/for-all [x+ ($.gen/vector $.gen/long
                                     1
                                     16)]
    (mprop/mult
      
      "Numerical computation of longs must result in a long"

      ($.eval/true? $.break/ctx
                    ($.cell/* (long? (~form ~@x+))))


      "Numerical computation with at least one double must result in a double"

      ($.std/double? ($.eval/result $.break/ctx
                                    ($.cell/* (apply ~form
                                                     (let [i  ~($.cell/long (rand-int (count x+)))
                                                           x+ ~x+]
                                                       (assoc x+
                                                              i
                                                              (double (get x+
                                                                           i)))))))))))



(defn prop-comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result as in Clojure."

  [f-cvx f-clj]

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/number
                                      1
                                      16)]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/any (apply f-clj
                                                   (map $.clj/any
                                                        x+)))
                               (~f-cvx ~@x+))))))


;;;;;;;;;; Arithmetic operators


(mprop/deftest *--

  {:ratio-num 100}

  (prop-arithmetic ($.cell/* *)))



(mprop/deftest +--

  {:ratio-num 100}

  (prop-arithmetic ($.cell/* +)))



(mprop/deftest ---

  {:ratio-num 100}

  (prop-arithmetic ($.cell/* -)))



(mprop/deftest div--

  {:ratio-num 100}

  (TC.prop/for-all [x+ ($.gen/vector $.gen/number
                                     1
                                     16)]
    ($.std/double? ($.eval/result $.break/ctx
                                  ($.cell/* (/ ~@x+))))))


;;;;;;;;;; Comparators


(mprop/deftest <--

  {:ratio-num 100}

  (prop-comparison ($.cell/* <)
                   <))



(mprop/deftest <=--

  {:ratio-num 100}

  (prop-comparison ($.cell/* <=)
                   <=))



(mprop/deftest ==--

  {:ratio-num 100}

  (prop-comparison ($.cell/* ==)
                   ==))



(mprop/deftest >=--

  {:ratio-num 100}

  (prop-comparison ($.cell/* >=)
                   >=))



(mprop/deftest >--

  {:ratio-num 100}

  (prop-comparison ($.cell/* >)
                   >))



; (mprop/deftest max--
; 
;   ;; TODO. Fails because of https://github.com/Convex-Dev/convex/issues/366
; 
;   ;; In case of equal inputs, Clojure favors the last argument whereas Convex favors the first one.
;   ;; 
;   ;; (max 1 1.0)  =>  1.0 in Clojure, 1 in Convex
; 
;   {:ratio-num 100}
; 
;   (prop-comparison ($.cell/* max)
;                    (fn [& arg+]
;                      (apply max
;                             (reverse arg+)))))



; (mprop/deftest min--
; 
;   ;; TODO. Fails because of https://github.com/Convex-Dev/convex/issues/366
;
;   ;; See comment for [[max--]].
; 
;   {:ratio-num 100}
; 
;   (prop-comparison ($.cell/* min)
;                    (fn [& arg+]
;                      (apply min
;                             (reverse arg+)))))


;;;;;;;;;; Exponentiation


(mprop/deftest exp--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/double (StrictMath/exp ($.clj/any x)))
                               (exp ~x))))))



(mprop/deftest pow--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number
                    y $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/double (StrictMath/pow ($.clj/any x)
                                                               ($.clj/any y)))
                               (pow ~x
                                    ~y))))))



(mprop/deftest sqrt--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/double (StrictMath/sqrt ($.clj/any x)))
                               (sqrt ~x))))))


;;;;;;;;;; Increment / decrement


(mprop/deftest dec--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/long]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (def dec-
                                         (dec ~x))))]
      (mprop/mult

        "Returns a long"

        ($.eval/true? ctx
                      ($.cell/* (long? dec-)))

        "Consistent with `-`"

        ($.eval/true? ctx
                      ($.cell/* (= dec-
                                   (- ~x
                                      1))))

        "Consisent with `+`"

        ($.eval/true? ctx
                      ($.cell/* (= dec-
                                   (+ ~x
                                      -1))))))))



(T/deftest dec-underflow

  (T/is (= ($.cell/long Long/MAX_VALUE)
           ($.eval/result $.break/ctx
                          ($.cell/* (dec ~Long/MIN_VALUE))))))



(mprop/deftest inc--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/long]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (def inc-
                                         (inc ~x))))]
      (mprop/mult

        "Returns a long"

        ($.eval/true? ctx
                      ($.cell/* (long? inc-)))


        "Consistent with `-`"

        ($.eval/true? ctx
                      ($.cell/* (= inc-
                                   (- ~x
                                      -1))))
        
        "Consistent with `+`"

        ($.eval/true? ctx
                      ($.cell/* (= inc-
                                   (+ ~x
                                      1))))))))



(T/deftest inc-overflow

  (T/is (= ($.cell/long Long/MIN_VALUE)
           ($.eval/result $.break/ctx
                          ($.cell/* (inc ~Long/MAX_VALUE))))))


;;;;;;;;;; Integer operations


(mprop/deftest euclidian-div

  ;; `mod` and `quot`.

  {:ratio-num 20}

  (TC.prop/for-all [a $.gen/long
                    b (TC.gen/such-that #($.eval/true? $.break/ctx
                                                       ($.cell/* (not (zero? ~%))))
                                        $.gen/long)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
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
                                                b)))))]
      (mprop/mult

        "`mod` produces a long"

        ($.eval/true? ctx
                      ($.cell/* (long? -mod)))


        "`quot` produces a long"

        ($.eval/true? ctx
                      ($.cell/* (long? -quot)))


        "`rem` produces a long"

        ($.eval/true? ctx
                      ($.cell/* (long? -rem)))


        "`quot` and `rem` are consistent"

        ($.eval/true? ctx
                      ($.cell/* (= a
                                   (+ -rem
                                      (* b
                                         -quot)))))))))


;;;;;;;;;; Miscellaneous


(mprop/deftest zero?--false

  {:ratio-num 20}

  (TC.prop/for-all [x $.gen/any]
    ($.eval/true? $.break/ctx
                  ($.cell/* (let [x (quote ~x)]
                              (= (zero? x)
                                 (or (= x
                                        0)
                                     (= x
                                        (byte 0))
                                     (= x
                                        0.0)
                                     (= x
                                        -0.0))))))))



(T/deftest zero?--true

  (T/is ($.eval/true? $.break/ctx
                      ($.cell/* (zero? 0))))

  (T/is ($.eval/true? $.break/ctx
                      ($.cell/* (zero? 0.0))))

  (T/is ($.eval/true? $.break/ctx
                      ($.cell/* (zero? -0.0)))))



;;;;;;;;;; Rounding


(mprop/deftest ceil

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/double (StrictMath/ceil ($.clj/any x)))
                               (ceil ~x))))))



(mprop/deftest floor

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~($.cell/double (StrictMath/floor ($.clj/any x)))
                               (floor ~x))))))


;;;;;;;;;; Sign operations


(mprop/deftest abs--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/such-that #(not (= ($.cell/* ##NaN)
                                                 %))
                                        $.gen/number)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (def abs-
                                         (abs ~x))))]
      (mprop/mult

        "Must be positive"

        ($.eval/true? ctx
                      ($.cell/* (>= abs-
                                    0)))


        "Type is preserved"

        (= (type ($.eval/result ctx
                                ($.cell/* abs-)))
           (type x))))))



(T/deftest abs--NaN

  (T/is ($.eval/true? $.break/ctx
                      ($.cell/* (nan? (abs ##NaN))))))



(mprop/deftest signum--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/number]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= ~x
                               (* (abs ~x)
                                  (signum ~x)))))))


;;;;;;;;;; Failing cases


(mprop/deftest error-cast-long-1

  ;; Functions that should accept only one long argument.
  
  {:ratio-num 20}

  (TC.prop/for-all [x $.break.gen/not-long]
    (mprop/mult

      "`dec`"

      (ex-cast? ($.cell/* (dec ~x)))


      "`inc`"

      (ex-cast? ($.cell/* (inc ~x))))))



(mprop/deftest error-cast-long-2

  ;; Functions that should accept only two long arguments.

  {:ratio-num 20}
  
  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-long
                                     b (TC.gen/one-of [(TC.gen/fmap $.cell/quoted
                                                                    $.gen/any)
                                                       $.gen/long])]
                          (TC.gen/shuffle [a
                                           b]))]
    (mprop/mult

      "`mod`"

      (ex-cast? ($.cell/* (mod ~a
                               ~b)))


      "`rem`"

      (ex-cast? ($.cell/* (rem ~a
                               ~b)))


      "`quot`"

      (ex-cast? ($.cell/* (quot ~a
                                ~b))))))


(mprop/deftest error-cast-number-1

  ;; Functions with one argument that should accept a number only.

  {:ratio-num 10}

  (TC.prop/for-all [x $.break.gen/not-number]
    (mprop/mult

      "`abs`"

      (ex-cast? ($.cell/* (abs ~x)))


      "`ceil`"

      (ex-cast? ($.cell/* (ceil ~x)))


      "`exp`"

      (ex-cast? ($.cell/* (exp ~x)))


      "`floor`"

      (ex-cast? ($.cell/* (floor ~x)))


      "`signum`"

      (ex-cast? ($.cell/* (signum ~x)))


      "`sqrt`"
      (ex-cast? ($.cell/* (sqrt ~x))))))



(mprop/deftest error-cast-number-2

  ;; Functions that should accept only two number arguments.

  {:ratio-num 20}

  (TC.prop/for-all [[a
                     b] (TC.gen/let [a $.break.gen/not-number
                                     b (TC.gen/one-of [$.gen/any
                                                       $.gen/number])]
                          (TC.gen/shuffle [a b]))]
    (mprop/check

      "`pow`"

      (ex-cast? ($.cell/* (pow (quote ~a)
                               (quote ~b)))))))



(mprop/deftest error-cast-variadic

  ;; Functions that accepts a variadic number of number arguments only.
  ;;
  ;; Comparison functions are variadic but are tested in [[error-cast-number-2]] since
  ;; they test arguments 2 by 2 (which would even succeed in these negative tests).

  {:ratio-num 5}

  (TC.prop/for-all [x+ (TC.gen/let [a  $.break.gen/not-number
                                    b+ (TC.gen/vector (TC.gen/one-of [$.gen/any
                                                                      $.gen/number])
                                                      0
                                                      7)]
                         (TC.gen/shuffle (map $.cell/quoted
                                              (cons a
                                                    b+))))]
    (mprop/mult

      "`*`"

      (ex-cast? ($.cell/* (* ~@x+)))


      "`+`"

      (ex-cast? ($.cell/* (+ ~@x+)))


      "`-`"

      (ex-cast? ($.cell/* (- ~@x+)))


      "`/`"

      (ex-cast? ($.cell/* (/ ~@x+)))

      "Relative comparators"

      (let [x-2+ (sort-by (comp $.std/number?
                                second)
                          x+)]
        (mprop/mult

          "`<`"

          (ex-cast? ($.cell/* (< ~@x-2+)))
    
          "`<=`"

          (ex-cast? ($.cell/* (<= ~@x-2+)))
    
          "`==`"

          (ex-cast? ($.cell/* (== ~@x-2+)))

          "`>=`"

          (ex-cast? ($.cell/* (>= ~@x-2+)))

          "`>`"

          (ex-cast? ($.cell/* (> ~@x-2+)))


          "`max`"

          (ex-cast? ($.cell/* (max ~@x-2+)))


          "`min`"

          (ex-cast? ($.cell/* (min ~@x-2+))))))))
