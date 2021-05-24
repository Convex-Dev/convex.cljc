(ns convex.lisp.test.core.coerce

  "Tests Convex core coercions."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp.form              :as $.form]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


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
       (let [[x-2
              cast?] ($.test.eval/result* (let [x-2 (~form-cast ~x)]
                                            [x-2
                                             (~form-pred x-2)]))]
         ($.test.prop/mult (suite-2 x
                                    x-2
                                    cast?)))))))

;;;;;;;;;;


($.test.prop/deftest address--

  (prop-coerce 'address
               'address?
               $.form/address?
               (TC.gen/one-of [$.gen/address
                               $.gen/blob-8
                               $.gen/hex-string-8
                               (TC.gen/large-integer* {:min 0})])))



($.test.prop/deftest blob--

  ;; TODO. Also test hashes, special type of blob that can be coerced to an actual blob.

  (prop-coerce 'blob
               'blob?
               $.form/blob?
               (TC.gen/one-of [$.gen/address
                               $.gen/blob
                               $.gen/hex-string])))



($.test.prop/deftest boolean--

  (prop-coerce 'boolean
               'boolean?
               true?
               (TC.gen/such-that #(and (some? %)
                                       (not (false? %)))
                                 $.gen/any)))



($.test.prop/deftest byte--

  (prop-coerce 'byte
               'number?
               unchecked-byte
               #(<= Byte/MIN_VALUE
                    %
                    Byte/MAX_VALUE)
               ($.gen/number-bounded {:max 1e6
                                      :min -1e6})))



($.test.prop/deftest char--

  (prop-coerce 'char
               '(fn [_] true)           ;; TODO. Incorrect, see #68, #92
               unchecked-char
               char?
               ($.gen/number-bounded {:max 1e6
                                      :min -1e6})))



($.test.prop/deftest encoding--

  (TC.prop/for-all [x $.gen/any]
    (let [ctx ($.test.eval/ctx* (do
                                  (def x
                                       (quote ~x))
                                  (def -encoding
                                       (encoding x))))]
      ($.test.prop/mult*

        "Result is a blob"
        ($.test.eval/result ctx
                            '(blob? -encoding))

        "Encoding is deterministic"
        ($.test.eval/result ctx
                            '(= -encoding
                                (encoding x)))))))



($.test.prop/deftest hash--

  ;; Also tests `hash?`.

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/address
                                      $.gen/blob])]
    (let [ctx ($.test.eval/ctx* (def -hash
                                     (hash ~x)))]
      ($.test.prop/mult*

        "`hash?`"
        ($.test.eval/result ctx
                            '(hash? -hash))

        "Hashing is deterministic"
        ($.test.eval/result* ctx
                             (= -hash
                                (hash ~x)))

        "Hashes are not mere blobs"
        ($.test.eval/result ctx
                            '(not (blob? -hash)))

        "Hashing a hash produces a hash"
        ($.test.eval/result ctx
                            '(hash? (hash -hash)))))))



($.test.prop/deftest keyword--

  (prop-coerce 'keyword
               'keyword?
               keyword?
               (TC.gen/one-of [$.gen/keyword
                               $.gen/string-symbolic
                               $.gen/symbol-quoted
                               $.gen/symbol-ns-quoted])))



($.test.prop/deftest long--

  (prop-coerce 'long
               'long?
               unchecked-long
               int?
               :convex/number))



;; TODO. Currently failing, see #77
;;
#_($.test.prop/deftest ^:recur set--

  (prop-coerce 'set
               'set?
               set
               set?
               $.gen/collection))



($.test.prop/deftest str--

  ;; TODO. Improve to be variadic.

  (prop-coerce 'str
               'str?
               ;; str ;; No comparable Clojure coercion, Convex prints vectors with "," instead of spaces, unlike Clojure
               string?
               $.gen/any))



($.test.prop/deftest symbol--

  (prop-coerce 'symbol
               'symbol?
               symbol?
               (TC.gen/one-of [$.gen/keyword
                               $.gen/string-symbolic
                               $.gen/symbol-quoted
                               $.gen/symbol-ns-quoted])))



($.test.prop/deftest vec--

  (prop-coerce 'vec
               'vector?
               ;; `vec` cannot be used because Convex implements order differently in maps and sets
               vector?
               $.gen/collection))
