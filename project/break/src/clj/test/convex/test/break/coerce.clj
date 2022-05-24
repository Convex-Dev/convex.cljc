(ns convex.test.break.coerce

  "Tests Convex core coercions."

  ;; TODO. Test failing cases as well and demonstrate consistency in coercions.

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


;; Some tests might be failing cases when the following is resolved: https://github.com/Convex-Dev/convex/issues/162
;;
(defn prop-error-cast

  "Checks that trying to cast an item that should not be cast fails indeed with a `:CAST` error."

  [f gen]

  (TC.prop/for-all [x gen]
    (= ($.cell/code-std* :CAST)
       ($.eval/exception-code $.break/ctx
                              ($.cell/* (~f ~x))))))


;;;;;;;;;;


(mprop/deftest address--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/address
                                      (TC.gen/such-that #($.eval/true? $.break/ctx
                                                                       ($.cell/* (<= 0
                                                                                     (long ~%)
                                                                                     9223372036854775807)))
                                                        ($.gen/blob 8)
                                                        100)
                                      (TC.gen/such-that #($.eval/true? $.break/ctx
                                                                       ($.cell/* (<= 0
                                                                                     (long (blob ~%))
                                                                                     9223372036854775807)))
                                                        ($.gen/hex-string 8)
                                                        100)
                                      (TC.gen/fmap $.cell/long
                                                   (TC.gen/large-integer* {:min 0}))])]
    ($.eval/true? $.break/ctx
                  ($.cell/* (address? (address ~x))))))



(mprop/deftest blob--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/address
                                      ($.gen/blob)
                                      ($.gen/hex-string)])]
    ($.eval/true? $.break/ctx
                  ($.cell/* (blob? (blob ~x))))))



(mprop/deftest boolean--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/any]
    ($.eval/true? $.break/ctx
                  ($.cell/* (boolean? (boolean (quote ~x)))))))



;; TODO. No `byte?` in CVX
;
; (mprop/deftest byte--
; 
;   {:ratio-num 100}
; 
;   (TC.prop/for-all [x ($.gen/number-bounded {:max 1e6
;                                              :min 1e6})]
;     ($.eval/true? $.break/ctx
;                   ($.cell/* (byte? (byte ~x))))))




;; TODO. No `char?` in CVX
;
; (mprop/deftest char--
; 
;   {:ratio-num 100}
; 
;   (TC.prop/for-all [x ($.gen/number-bounded {:max 1e6
;                                              :min 1e6})]
;     ($.eval/true? $.break/ctx
;                   ($.cell/* (char? (char ~x))))))



(mprop/deftest encoding--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/any]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def x
                                           (quote ~x))
                                      (def -encoding
                                           (encoding x)))))]
      (mprop/mult

        "Result is a blob"

        ($.eval/true? ctx
                      ($.cell/* (blob? -encoding)))


        "Encoding is deterministic"

        ($.eval/true? ctx
                      ($.cell/* (= -encoding
                                   (encoding x))))))))



(mprop/deftest hash--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/address
                                      ($.gen/blob)])]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (def -hash
                                         (hash ~x))))]
      (mprop/mult

        "Hashing is deterministic"

        ($.eval/true? ctx
                      ($.cell/* (= -hash
                                   (hash ~x))))


        "Hashes are blobs"

        ($.eval/true? ctx
                      ($.cell/* (blob? -hash)))

        "Hashes are 32-byte long"

        ($.eval/true? ctx
                      ($.cell/* (= 32
                                   (count -hash))))))))



(mprop/deftest keyword--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/keyword
                                      $.gen/string-symbolic
                                      $.gen/symbol])]
    ($.eval/true? $.break/ctx
                  ($.cell/* (keyword? (keyword (quote ~x)))))))



(mprop/deftest long--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/address
                                      $.gen/boolean
                                      $.gen/byte
                                      $.gen/char
                                      $.gen/double
                                      $.gen/long])]
    ($.eval/true? $.break/ctx
                  ($.cell/* (long? (long ~x))))))



(mprop/deftest set--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/any-coll]
    ($.eval/true? $.break/ctx
                  ($.cell/* (set? (set (quote ~x)))))))



(mprop/deftest str--

  ;; TODO. Improve to be variadic.

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/any]
    ($.eval/true? $.break/ctx
                  ($.cell/* (str? (str (quote ~x)))))))



(mprop/deftest symbol--

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/keyword
                                      $.gen/string-symbolic
                                      $.gen/symbol])]
    ($.eval/true? $.break/ctx
                  ($.cell/* (symbol? (symbol (quote ~x)))))))



(mprop/deftest vec--

  {:ratio-num 100}

  (TC.prop/for-all [x $.gen/any-coll]
    ($.eval/true? $.break/ctx
                  ($.cell/* (vector? (vec (quote ~x)))))))
