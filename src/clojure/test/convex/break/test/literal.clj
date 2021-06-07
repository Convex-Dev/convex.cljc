(ns convex.break.test.literal

  "Testing literal notation of Convex data (scalar values and collections).
  
   Consists of a cycle such as:
   
   - Generate value as Clojure data
   - Convert Clojure data to Convex Lisp source
   - Read Convex Lisp source
   - Eval Convex Lisp Source
   - Convert result to Clojure data
   - Result must be equal to generate value
  
   Also test quoting when relevant. For instance, like in Clojure, quoting a number must result in this very same number."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as t]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.gen              :as $.break.gen]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites


(defn suite-equal

  ""

  [x]

  (mprop/check

    "`=` returns true when an item is compared with itself"
    ($.cvm.eval/result* (= ~x
                           ~x))))


;;;;;;;;;; Properties


(defn prop-quotable

  "Property checking that going through the CVM returns the given generated value and that quoting it has no impact." 

  [gen]

  (TC.prop/for-all [x gen]
    (mprop/and
      
      (suite-equal x)

      (mprop/check

        "Round-trip through the CVM"

        ($.lisp/= x
                  ($.cvm.eval/result* (identity ~x))
                  ($.cvm.eval/result* (quote ~x)))))))


;;;;;;;;;; Scalar values


(t/deftest nil--

  (t/is (nil? ($.cvm.eval/result nil))))
 


(mprop/deftest address

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/address))



(mprop/deftest blob

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/blob))



(mprop/deftest boolean-

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/boolean))



(mprop/deftest char-

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/char))



(mprop/deftest double-

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/double))



(mprop/deftest double-E-notation

  {:ratio-num 100}

  (TC.prop/for-all [x ($.break.gen/E-notation $.lisp.gen/long)]
    (= (Double/parseDouble (str x))
       ($.cvm.eval/result x))))



#_(mprop/deftest double-E-notation--fail

  ;; TODO. Must catch a Reader error, it is not at the CVM level.

  {:ratio-num 100}

  (TC.prop/for-all [x ($.break.gen/E-notation $.lisp.gen/double)]
    ($.cvm.eval/exception? x)))



(mprop/deftest keyword-

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/keyword))



(mprop/deftest long-

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/long))



(mprop/deftest string-

  ;; TODO. Suffers from https://github.com/Convex-Dev/convex/issues/66

  {:ratio-num 100}

  (prop-quotable $.lisp.gen/string))



(mprop/deftest symbol-

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.lisp.gen/symbol
                                      $.lisp.gen/symbol-ns])]
    ($.lisp/= x
              ($.cvm.eval/result* (identity (quote ~x))))))


;;;;;;;;;; Collections


(mprop/deftest list-

  ;; Quoting mess with some data values, that is why a subset of scalar generators is used.

  {:ratio-num 10}

  (TC.prop/for-all [x+ (TC.gen/vector (TC.gen/one-of [$.lisp.gen/address
                                                      $.lisp.gen/blob
                                                      $.lisp.gen/boolean
                                                      $.lisp.gen/char
                                                      $.lisp.gen/double
                                                      $.lisp.gen/keyword
                                                      $.lisp.gen/long
                                                      $.lisp.gen/nothing
                                                      $.lisp.gen/string]))]
    ($.cvm.eval/result* (= (list ~@x+)
                           (quote (~@x+))))))



(mprop/deftest map-

  {:ratio-num 10}

  (TC.prop/for-all [x $.lisp.gen/map]
    ($.cvm.eval/result* (= (hash-map ~@(mapcat identity
                                               x))
                            ~x))))



(mprop/deftest set-

  {:ratio-num 10}

  (TC.prop/for-all [x $.lisp.gen/set]
    ($.cvm.eval/result* (= (hash-set ~@x)
                           ~x))))



(mprop/deftest vector-

  {:ratio-num 10}

  (TC.prop/for-all [x $.lisp.gen/vector]
    ($.cvm.eval/result* (= (vector ~@x)
                           ~x))))


;;;;;;;;;; Negative tests


(mprop/deftest ==--fail

  {:ratio-num 10}

  (TC.prop/for-all [x+ (TC.gen/vector-distinct $.lisp.gen/any
                                               {:max-elements 6
                                                :min-elements 2})]
    ($.cvm.eval/result* (not (= ~@x+)))))
