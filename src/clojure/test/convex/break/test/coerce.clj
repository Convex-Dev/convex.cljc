(ns convex.break.test.coerce

  "Tests Convex core coercions."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(defn prop-coerce

  "Checks coercing a value generated from `schema` by applying it to `form-cast`.
  
   Tests at least 2 properties:
  
   - Is the result consistent with Clojure by applying that value to `clojure-pred`?
   - Does the CVM confirm the result is of the right type by applying it to `form-pred`?
  
   If `clojure-cast is provided, 1 additional property is checked:
  
   - Does casting in Clojure provide the exact same result?"


  ([form-cast form-pred clojure-pred gen]

   (prop-coerce form-cast
                form-pred
                nil
                clojure-pred
                gen))


  ([form-cast form-pred clojure-cast clojure-pred gen]

   (TC.prop/for-all [x gen]
     (let [ctx   ($.clj.eval/ctx* (def -cast
                                         (~form-cast ~x)))
           -cast ($.clj.eval/result ctx
                                    '-cast)]
       (mprop/mult

         "Properly cast"

         ($.clj.eval/result* ctx
                             (~form-pred -cast))

         "Predicate is consistent with Clojure"

         (clojure-pred -cast)

         "Comparing cast with Clojure's"

         (if clojure-cast
           (= -cast
              (clojure-cast x))
           true))))))



;; TODO. Tests failing cases when the following is resolved: https://github.com/Convex-Dev/convex/issues/162
;;
(defn prop-error-cast

  "Checks that trying to cast an item that should not be cast fails indeed with a `:CAST` error."

  [form-cast gen]

  (TC.prop/for-all [x gen]
    ($.clj.eval/code?* :CAST
                       (~form-cast ~x))))


;;;;;;;;;;


(mprop/deftest address--

  {:ratio-num 100}

  (prop-coerce 'address
               'address?
               $.clj/address?
               (TC.gen/one-of [$.clj.gen/address
                               $.clj.gen/blob-8
                               $.clj.gen/hex-string-8
                               (TC.gen/large-integer* {:min 0})])))



;; (mprop/deftest address--fail
;; 
;;   (prop-error-cast 'address
;;                    (TC.gen/such-that #(if (int? %)
;;                                         (neg? %)
;;                                         %)
;;                                      ($.clj.gen/any-but #{$.clj.gen/blob-8
;;                                                       $.clj.gen/hex-string-8}))))



(mprop/deftest blob--

  ;; TODO. Also test hashes, special type of blob that can be coerced to an actual blob.

  {:ratio-num 100}

  (prop-coerce 'blob
               'blob?
               $.clj/blob?
               (TC.gen/one-of [$.clj.gen/address
                               $.clj.gen/blob
                               $.clj.gen/hex-string])))



;(mprop/deftest blob--fail
;
;  (prop-error-cast 'blob
;                   ($.clj.gen/any-but #{$.clj.gen/
;

(mprop/deftest boolean--

  {:ratio-num 100}

  (prop-coerce 'boolean
               'boolean?
               true?
               (TC.gen/such-that #(and (some? %)
                                       (not (false? %)))
                                 $.clj.gen/any)))



(mprop/deftest byte--

  {:ratio-num 100}

  (prop-coerce 'byte
               'number?
               #(bit-and 0xFF
                         (unchecked-byte %))
               #(<= 0
                    %
                    255)
               ($.clj.gen/number-bounded {:max 1e6
                                          :min -1e6})))



(mprop/deftest char--

  {:ratio-num 100}

  (prop-coerce 'char
               '(fn [_] true)  ;; TODO. Incorrect, see https://github.com/Convex-Dev/convex/issues/92
               unchecked-char
               char?
               ($.clj.gen/number-bounded {:max 1e6
                                          :min -1e6})))



(mprop/deftest encoding--

  {:ratio-num 100}

  (TC.prop/for-all [x $.clj.gen/any]
    (let [ctx ($.clj.eval/ctx* (do
                                 (def x
                                      (quote ~x))
                                 (def -encoding
                                      (encoding x))))]
      (mprop/mult

        "Result is a blob"

        ($.clj.eval/result ctx
                           '(blob? -encoding))


        "Encoding is deterministic"

        ($.clj.eval/result ctx
                           '(= -encoding
                               (encoding x)))))))



(mprop/deftest hash--

  ;; Also tests `hash?`.

  {:ratio-num 100}

  (TC.prop/for-all [x (TC.gen/one-of [$.clj.gen/address
                                      $.clj.gen/blob])]
    (let [ctx ($.clj.eval/ctx* (def -hash
                                    (hash ~x)))]
      (mprop/mult

        "`hash?`"

        ($.clj.eval/result ctx
                           '(hash? -hash))


        "Hashing is deterministic"

        ($.clj.eval/result* ctx
                            (= -hash
                               (hash ~x)))


        "Hashes are not mere blobs"

        ($.clj.eval/result ctx
                           '(not (blob? -hash)))


        "Hashing a hash produces a hash"

        ($.clj.eval/result ctx
                           '(hash? (hash -hash)))))))



(mprop/deftest keyword--

  {:ratio-num 100}

  (prop-coerce 'keyword
               'keyword?
               keyword?
               (TC.gen/one-of [$.clj.gen/keyword
                               $.clj.gen/string-symbolic
                               $.clj.gen/symbol-quoted
                               $.clj.gen/symbol-ns-quoted])))



(mprop/deftest long--

  {:ratio-num 100}

  (prop-coerce 'long
               'long?
               int?
               (TC.gen/one-of [$.clj.gen/address
                               $.clj.gen/boolean
                               $.clj.gen/byte
                               $.clj.gen/char
                               $.clj.gen/double
                               $.clj.gen/long])))



;; TODO. Currently failing, see https://github.com/Convex-Dev/convex/issues/77
;;
#_(mprop/deftest set--

  {:ratio-num 100}

  (prop-coerce 'set
               'set?
               set
               set?
               $.clj.gen/collection))



(mprop/deftest str--

  ;; TODO. Improve to be variadic.

  {:ratio-num 100}

  (prop-coerce 'str
               'str?
               ;; str ;; No comparable Clojure coercion, Convex prints vectors with "," instead of spaces, unlike Clojure
               string?
               $.clj.gen/any))



(mprop/deftest symbol--

  {:ratio-num 100}

  (prop-coerce 'symbol
               'symbol?
               symbol?
               (TC.gen/one-of [$.clj.gen/keyword
                               $.clj.gen/string-symbolic
                               $.clj.gen/symbol-quoted
                               $.clj.gen/symbol-ns-quoted])))



(mprop/deftest vec--

  {:ratio-num 100}

  (prop-coerce 'vec
               'vector?
               ;; `vec` cannot be used because Convex implements order differently in maps and sets
               vector?
               $.clj.gen/collection))
