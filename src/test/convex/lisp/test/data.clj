(ns convex.lisp.test.data

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
            [convex.lisp.form              :as $.form]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]
            [convex.lisp.test.util         :as $.test.util])) 


;;;;;;;;;; Suites


(defn suite-equal

  ""

  [x]

  ($.test.prop/checkpoint*

    "`=` returns true when an item is compared with itself"
    ($.test.eval/result* (= ~x
                            ~x))))


;;;;;;;;;; Properties


(defn prop-quotable

  "Property checking that going through the CVM returns the given generated value and that quoting it has no impact." 

  [gen]

  (TC.prop/for-all [x gen]
    ($.test.prop/and* (suite-equal x)
                      ($.test.prop/checkpoint*

                        "Round-trip through the CVM"

                        ($.test.util/eq x
                                        ($.test.eval/result* (identity ~x))
                                        ($.test.eval/result* (quote ~x)))))))


;;;;;;;;;; Scalar values


(t/deftest nil--

  (t/is (nil? ($.test.eval/result nil))))
 


($.test.prop/deftest address

  (prop-quotable $.gen/address))



($.test.prop/deftest blob

  (prop-quotable $.gen/blob))



($.test.prop/deftest boolean-

  (prop-quotable $.gen/boolean))



($.test.prop/deftest char-

  (prop-quotable $.gen/char))



($.test.prop/deftest double-

  (prop-quotable $.gen/double))



($.test.prop/deftest double-E-notation

  (TC.prop/for-all [x ($.test.gen/E-notation $.gen/long)]
    (= (Double/parseDouble (str x))
       ($.test.eval/result x))))



#_($.test.prop/deftest double-E-notation--fail

  ;; TODO. Must catch a Reader error, it is not at the CVM level.

  (TC.prop/for-all [x ($.test.gen/E-notation $.gen/double)]
    ($.test.eval/error? x)))



($.test.prop/deftest keyword-

  (prop-quotable $.gen/keyword))



($.test.prop/deftest long-

  (prop-quotable $.gen/long))



($.test.prop/deftest string-

  ;; TODO. Suffers from #66.

  (prop-quotable $.gen/string))



($.test.prop/deftest symbol-

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/symbol
                                      $.gen/symbol-ns])]
    ($.test.util/eq x
                    ($.test.eval/result* (identity (quote ~x))))))


;;;;;;;;;; Collections


($.test.prop/deftest list-

  ;; Quoting mess with some data values, that is why a subset of scalar generators is used.

  (TC.prop/for-all [x+ (TC.gen/vector (TC.gen/one-of [$.gen/address
                                                      $.gen/blob
                                                      $.gen/boolean
                                                      $.gen/char
                                                      $.gen/double
                                                      $.gen/keyword
                                                      $.gen/long
                                                      $.gen/nothing
                                                      $.gen/string]))]
    ($.test.eval/result* (= (list ~@x+)
                            (quote (~@x+))))))



($.test.prop/deftest map-

  (TC.prop/for-all [x $.gen/map]
    ($.test.eval/result* (= (hash-map ~@(mapcat identity
                                                x))
                            ~x))))



($.test.prop/deftest set-

  (TC.prop/for-all [x $.gen/set]
    ($.test.eval/result* (= (hash-set ~@x)
                            ~x))))



($.test.prop/deftest vector-

  (TC.prop/for-all [x $.gen/vector]
    ($.test.eval/result* (= (vector ~@x)
                            ~x))))


;;;;;;;;;; Negative tests


($.test.prop/deftest ==--fail

  (TC.prop/for-all [x+ (TC.gen/vector-distinct $.gen/any
                                               {:max-elements 6
                                                :min-elements 2})]
    ($.test.eval/result* (not (= ~@x+)))))
