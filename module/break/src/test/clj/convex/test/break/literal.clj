(ns convex.test.break.literal

  "Simply round-trip data through the CVM using a few ways, such as quoting, and ensure nothing changed."
  
  {:author "Adam Helinski"}

  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites


(defn prop-roundtrip

  "Property checking that going through the CVM returns the given generated value and that quoting it has no impact.
  
   Optionally, also tests building values through constructor functions."


  ([gen]

   (prop-roundtrip gen
                  nil))


  ([gen ctor]

   (TC.prop/for-all [x gen]
     (mprop/mult

       "`=` returns true when an item is compared with itself"

       ($.eval/true? $.break/ctx
                     ($.cell/* (= (quote ~x)
                                  (quote ~x))))

       "Round-trip through the CVM"

       (= x
          ($.eval/result $.break/ctx
                         ($.cell/* (quote ~x))))

       "Identity"

       (= x
          ($.eval/result $.break/ctx
                         ($.cell/* (identity (quote ~x)))))

       "Ctor"

       (if ctor
         (= x
            ($.eval/result $.break/ctx
                           (ctor x)))
         true)))))


;;;;;;;;;; Scalar values


(T/deftest nil--

  (T/is (nil? ($.eval/result $.break/ctx
                             nil))))
 


(mprop/deftest address

  {:ratio-num 100}

  (prop-roundtrip $.gen/address))



(mprop/deftest blob

  {:ratio-num 100}

  (prop-roundtrip ($.gen/blob)))



(mprop/deftest boolean-

  {:ratio-num 100}

  (prop-roundtrip $.gen/boolean))



(mprop/deftest char-

  {:ratio-num 100}

  (prop-roundtrip $.gen/char))



(mprop/deftest double-

  {:ratio-num 100}

  (prop-roundtrip $.gen/double))



(mprop/deftest keyword-

  {:ratio-num 100}

  (prop-roundtrip $.gen/keyword))



(mprop/deftest long-

  {:ratio-num 100}

  (prop-roundtrip $.gen/long))



(mprop/deftest string-

  {:ratio-num 100}

  (prop-roundtrip ($.gen/string)))



(mprop/deftest symbol-

  {:ratio-num 100}

  (prop-roundtrip $.gen/symbol))


;;;;;;;;;; Collections


(mprop/deftest list-

  {:ratio-num 10}

  (prop-roundtrip $.gen/any-list
                  #($.cell/* (list ~@(map $.cell/quoted
                                          %)))))



(mprop/deftest map-

  {:ratio-num 10}

  (prop-roundtrip $.gen/any-map
                  #($.cell/* (hash-map ~@(map $.cell/quoted
                                              (mapcat identity
                                                      %))))))



(mprop/deftest set-

  {:ratio-num 10}

  (prop-roundtrip $.gen/any-set
                  #($.cell/* (hash-set ~@(map $.cell/quoted
                                              %)))))



(mprop/deftest vector-

  {:ratio-num 10}

  (prop-roundtrip $.gen/any-vector
                  #($.cell/* (vector ~@(map $.cell/quoted
                                            %)))))


;;;;;;;;;; Negative tests


(mprop/deftest ==--fail

  {:ratio-num 10}

  (TC.prop/for-all [x+ (TC.gen/vector-distinct ($.gen/quoted $.gen/any)
                                               {:max-elements 6
                                                :min-elements 2})]
    ($.eval/true? $.break/ctx
                  ($.cell/* (not (= ~@x+))))))
