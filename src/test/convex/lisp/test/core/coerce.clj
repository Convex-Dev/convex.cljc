(ns convex.lisp.test.core.coerce

  "Tests Convex core coercions."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.schema              :as $.schema]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.schema         :as $.test.schema]
            [convex.lisp.test.util           :as $.test.util]))


(def max-size-coll 5)


;;;;;;;;;;


(tc.ct/defspec address--

  ($.test.prop/coerce 'address
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



(tc.ct/defspec blob--

  ;; TODO. Also test hashes, special type of blob that can be coerced to an actual blob.

  ($.test.prop/coerce 'blob
                      'blob?
                      (partial $.test.schema/valid?
                               :convex/blob)
                      [:or
                       :convex/address
                       :convex/blob
                       :convex/hexstring]))



(tc.ct/defspec boolean--

  {:max-size max-size-coll}

  ($.test.prop/coerce 'boolean
                      'boolean?
                      true?
                      [:and
                       :convex/data
                       [:not [:enum false
                                    nil]]]))



(tc.ct/defspec byte--

  ($.test.prop/coerce 'byte
                      'number?
                      unchecked-byte
                      (fn clojure-pred [x-2]
                        (<= Byte/MIN_VALUE
                            x-2
                            Byte/MAX_VALUE))
                      [:and :convex/number
                       [:>= -1e6]
                       [:<= 1e6]]))



(tc.ct/defspec char--

  ($.test.prop/coerce 'char
                      'number?               ;; TODO. Incorrect, see #68
                      unchecked-char
                      char?
                      [:and :convex/number
                       [:>= -1e6]
                       [:<= 1e6]]))



(tc.ct/defspec hash--

  ;; Also tests `hash?`.

  ($.test.prop/check [:or
                      :convex/address
                      :convex/blob-32]
                     (fn [x]
                       (let [[h
                              h-1?
                              h-2?] ($.test.eval/form ($/templ {'X x}
                                                               '(let [h (hash X)]
                                                                  [h
                                                                   (hash? h)
                                                                   (hash? (hash h))])))]
                          ($.test.prop/mult*

                            "Result is a hash"
                            ($.test.schema/valid? :convex/hash
                                                  h)

                            "Hashing does produce a hash"
                            h-1?

                            "Hashing a hash produces a hash"
                            h-2?)))))



(tc.ct/defspec keyword--

  ($.test.prop/coerce 'keyword
                      'keyword?
                      keyword
                      keyword?
                      ($.schema/sym-coercible)))



(tc.ct/defspec long--

  ($.test.prop/coerce 'long
                      'long?
                      unchecked-long
                      int?
                      :convex/number))



;; TODO. Currently failing, see #77
;;
#_(tc.ct/defspec set--

  {:max-size max-size-coll}

  ($.test.prop/coerce 'set
                      'set?
                      set
                      set?
                      :convex/collection))



(tc.ct/defspec str--

  {:max-size max-size-coll}

  ($.test.prop/coerce 'str
                      'str?
                      ;; No comparable Clojure coercion, Convex prints vectors with "," instead of spaces, unlike Clojure
                      string?
                      [:vector
                       :convex/data]))



(tc.ct/defspec symbol--

  ($.test.prop/coerce 'symbol
                      'symbol?
                      symbol
                      symbol?
                      ($.schema/sym-coercible)))



(tc.ct/defspec vec--

  {:max-size max-size-coll}

  ($.test.prop/coerce 'vec
                      'vector?
                      ;; `vec` cannot be used because Convex implements order differently in maps and sets
                      vector?
                      :convex/collection))
