(ns convex.lisp.test.core.coerce

  "Tests Convex core coercions."

  {:author "Adam Helinski"}

  (:require [convex.lisp.schema      :as $.schema]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;;


(defn prop-coerce

  "Checks coercing a value generated from `schema` by applying it to `form-cast`.
  
   Tests at least 2 properties:
  
   - Is the result consistent with Clojure by applying that value to `clojure-pred`?
   - Does the CVM confirm the result is of the right type by applying it to `form-pred`?
  
   If `clojure-cast is provided, 1 additional property is checked:
  
   - Does casting in Clojure provide the exact same result?"


  ([form-cast form-pred clojure-pred schema]

   (prop-coerce form-cast
                form-pred
                nil
                clojure-pred
                schema))


  ([form-cast form-pred clojure-cast clojure-pred schema]

   ($.test.prop/check schema
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
                        (fn [x]
                          (let [[x-2
                                 cast?] ($.test.eval/result* (let [x-2 (~form-cast (quote ~x))]
                                                               [x-2
                                                                (~form-pred x-2)]))]
                            ($.test.prop/mult (suite-2 x
                                                       x-2
                                                       cast?))))))))

;;;;;;;;;;


($.test.prop/deftest address--

  (prop-coerce 'address
               'address?
               (partial $.test.schema/valid?
                        :convex/address)
               [:or
                :convex/address
                :convex/blob-8
                :convex/hexstring-8
                [:and
                 :convex/long
                 [:>= 0]]]))



($.test.prop/deftest blob--

  ;; TODO. Also test hashes, special type of blob that can be coerced to an actual blob.

  (prop-coerce 'blob
               'blob?
               (partial $.test.schema/valid?
                        :convex/blob)
               [:or
                :convex/address
                :convex/blob
                :convex/hexstring]))



($.test.prop/deftest ^:recur boolean--

  (prop-coerce 'boolean
               'boolean?
               true?
               [:and
                :convex/data
                [:not [:enum false
                             nil]]]))



($.test.prop/deftest byte--

  (prop-coerce 'byte
               'number?
               unchecked-byte
               (fn clojure-pred [x-2]
                 (<= Byte/MIN_VALUE
                     x-2
                     Byte/MAX_VALUE))
               [:and :convex/number
                [:>= -1e6]
                [:<= 1e6]]))



($.test.prop/deftest char--

  (prop-coerce 'char
               '(fn [_] true)           ;; TODO. Incorrect, see #68, #92
               unchecked-char
               char?
               [:and :convex/number
                [:>= -1e6]
                [:<= 1e6]]))



($.test.prop/deftest ^:recur encoding--

  ($.test.prop/check :convex/data
                     (fn [x]
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
                                                   (encoding x))))))))



($.test.prop/deftest hash--

  ;; Also tests `hash?`.

  ($.test.prop/check [:or
                      :convex/address
                      :convex/blob-32]
                     (fn [hash-]
                       (let [ctx ($.test.eval/ctx* (def -hash
                                                        (hash ~hash-)))]
                         ($.test.prop/mult*

                           "Result looks like a hash"
                           ($.test.schema/valid? :convex/hash
                                                 ($.test.eval/result ctx
                                                                     '-hash))

                           "`hash?`"
                           ($.test.eval/result ctx
                                               '(hash? -hash))

                           "Hashing is deterministic"
                           ($.test.eval/result* ctx
                                                (= -hash
                                                   (hash ~hash-)))

                           "Hashes are not mere blobs"
                           ($.test.eval/result ctx
                                               '(not (blob? -hash)))

                           "Hashing a hash produces a hash"
                           ($.test.eval/result ctx
                                               '(hash? (hash -hash))))))))



($.test.prop/deftest keyword--

  (prop-coerce 'keyword
               'keyword?
               keyword
               keyword?
               ($.schema/sym-coercible)))



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
               :convex/collection))



($.test.prop/deftest ^:recur str--

  (prop-coerce 'str
               'str?
               ;; No comparable Clojure coercion, Convex prints vectors with "," instead of spaces, unlike Clojure
               string?
               [:vector
                :convex/data]))



($.test.prop/deftest symbol--

  (prop-coerce 'symbol
               'symbol?
               symbol
               symbol?
               ($.schema/sym-coercible)))



($.test.prop/deftest ^:recur vec--

  (prop-coerce 'vec
               'vector?
               ;; `vec` cannot be used because Convex implements order differently in maps and sets
               vector?
               :convex/collection))
