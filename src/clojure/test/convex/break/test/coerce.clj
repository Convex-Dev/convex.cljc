(ns convex.break.test.coerce

  "Tests Convex core coercions."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
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

   (let [suite   (fn [_x x-2 cast?]
                   [["Consistent with Clojure"
                     #(clojure-pred x-2)]

                    ["Properly cast"
                     #(identity cast?)]])
         suite-2 (if clojure-cast
                   (fn [x x-2 cast?]
                     (conj (suite x
                                  x-2
                                  cast?)
                           ["Comparing cast with Clojure's"
                            #(= x-2
                                (clojure-cast x))]))
                   suite)]
     (TC.prop/for-all [x gen]
       (let [ctx   ($.break.eval/ctx* (def -cast
                                           (~form-cast ~x)))
             -cast ($.break.eval/result ctx
                                        '-cast)]
         (mprop/mult

           "Properly cast"

           -cast


           "Predicate is consistent with Clojure"

           (clojure-pred ($.break.eval/result ctx
                                              '-cast))

           "Comparing cast with Clojure's"

           (if clojure-cast
             (= -cast
                (clojure-cast x))
             true)))))))



;; TODO. Tests failing cases when the following is resolved: https://github.com/Convex-Dev/convex/issues/162
;;
(defn prop-error-cast

  "Checks that trying to cast an item that should not be cast fails indeed with a `:CAST` error."

  [form-cast gen]

  (TC.prop/for-all [x gen]
    ($.break.eval/error-cast?* (~form-cast ~x))))


;;;;;;;;;;


(mprop/deftest address--

  (prop-coerce 'address
               'address?
               $.lisp/address?
               (TC.gen/one-of [$.lisp.gen/address
                               $.lisp.gen/blob-8
                               $.lisp.gen/hex-string-8
                               (TC.gen/large-integer* {:min 0})])))



;; (mprop/deftest address--fail
;; 
;;   (prop-error-cast 'address
;;                    (TC.gen/such-that #(if (int? %)
;;                                         (neg? %)
;;                                         %)
;;                                      ($.lisp.gen/any-but #{$.lisp.gen/blob-8
;;                                                       $.lisp.gen/hex-string-8}))))



(mprop/deftest blob--

  ;; TODO. Also test hashes, special type of blob that can be coerced to an actual blob.

  (prop-coerce 'blob
               'blob?
               $.lisp/blob?
               (TC.gen/one-of [$.lisp.gen/address
                               $.lisp.gen/blob
                               $.lisp.gen/hex-string])))



;(mprop/deftest blob--fail
;
;  (prop-error-cast 'blob
;                   ($.lisp.gen/any-but #{$.lisp.gen/
;

(mprop/deftest boolean--

  (prop-coerce 'boolean
               'boolean?
               true?
               (TC.gen/such-that #(and (some? %)
                                       (not (false? %)))
                                 $.lisp.gen/any)))



(mprop/deftest byte--

  (prop-coerce 'byte
               'number?
               #(bit-and 0xFF
                         (unchecked-byte %))
               #(<= 0
                    %
                    255)
               ($.lisp.gen/number-bounded {:max 1e6
                                           :min -1e6})))



(mprop/deftest char--

  (prop-coerce 'char
               '(fn [_] true)  ;; TODO. Incorrect, see https://github.com/Convex-Dev/convex/issues/92
               unchecked-char
               char?
               ($.lisp.gen/number-bounded {:max 1e6
                                           :min -1e6})))



(mprop/deftest encoding--

  (TC.prop/for-all [x $.lisp.gen/any]
    (let [ctx ($.break.eval/ctx* (do
                                   (def x
                                        (quote ~x))
                                   (def -encoding
                                        (encoding x))))]
      (mprop/mult

        "Result is a blob"

        ($.break.eval/result ctx
                             '(blob? -encoding))


        "Encoding is deterministic"

        ($.break.eval/result ctx
                             '(= -encoding
                                 (encoding x)))))))



(mprop/deftest hash--

  ;; Also tests `hash?`.

  (TC.prop/for-all [x (TC.gen/one-of [$.lisp.gen/address
                                      $.lisp.gen/blob])]
    (let [ctx ($.break.eval/ctx* (def -hash
                                      (hash ~x)))]
      (mprop/mult

        "`hash?`"

        ($.break.eval/result ctx
                            '(hash? -hash))


        "Hashing is deterministic"

        ($.break.eval/result* ctx
                              (= -hash
                                 (hash ~x)))


        "Hashes are not mere blobs"

        ($.break.eval/result ctx
                            '(not (blob? -hash)))


        "Hashing a hash produces a hash"

        ($.break.eval/result ctx
                            '(hash? (hash -hash)))))))



(mprop/deftest keyword--

  (prop-coerce 'keyword
               'keyword?
               keyword?
               (TC.gen/one-of [$.lisp.gen/keyword
                               $.lisp.gen/string-symbolic
                               $.lisp.gen/symbol-quoted
                               $.lisp.gen/symbol-ns-quoted])))



(mprop/deftest long--

  (prop-coerce 'long
               'long?
               int?
               (TC.gen/one-of [$.lisp.gen/address
                               $.lisp.gen/boolean
                               $.lisp.gen/byte
                               $.lisp.gen/char
                               $.lisp.gen/double
                               $.lisp.gen/long])))



;; TODO. Currently failing, see https://github.com/Convex-Dev/convex/issues/77
;;
#_(mprop/deftest set--

  (prop-coerce 'set
               'set?
               set
               set?
               $.lisp.gen/collection))



(mprop/deftest str--

  ;; TODO. Improve to be variadic.

  (prop-coerce 'str
               'str?
               ;; str ;; No comparable Clojure coercion, Convex prints vectors with "," instead of spaces, unlike Clojure
               string?
               $.lisp.gen/any))



(mprop/deftest symbol--

  (prop-coerce 'symbol
               'symbol?
               symbol?
               (TC.gen/one-of [$.lisp.gen/keyword
                               $.lisp.gen/string-symbolic
                               $.lisp.gen/symbol-quoted
                               $.lisp.gen/symbol-ns-quoted])))



(mprop/deftest vec--

  (prop-coerce 'vec
               'vector?
               ;; `vec` cannot be used because Convex implements order differently in maps and sets
               vector?
               $.lisp.gen/collection))
