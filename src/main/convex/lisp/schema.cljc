(ns convex.lisp.schema

  ""

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]
            [convex.lisp.hex               :as $.hex]))


;;;;;;;;;; Schemas


(defn data

  ""

  ;; TODO. No address nor blob since no equivalent litteral notation.


  ([]

   (data nil))


  ([registry]

   (assoc registry
          :convex/address      [:and
                                {:gen/fmap   #(symbol (str "#"
                                                           %))
                                 :gen/schema pos-int?}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches #"#\d+"
                                                        (name sym))))]]
          :convex/blob         [:and
                                {:gen/fmap   (fn [x]
                                               (symbol (str "0x"
                                                            ($.hex/from-int x))))
                                 :gen/schema pos-int?}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex
                                                        (name sym))))]]
          :convex/boolean      :boolean
          :convex/char         char?
          :convex/collection   [:or
                                :convex/list
                                :convex/map
                                :convex/set
                                :convex/vector]
          :convex/data         [:or
                                :convex/address
                                :convex/blob
                                :convex/boolean
                                :convex/char
                                :convex/double
                                :convex/keyword
                                :convex/list
                                :convex/long
                                :convex/map
                                :convex/nil
                                :convex/string
                                :convex/set
                                :convex/symbol
                                :convex/vector]
          :convex/double       [:double
                                ;; TODO. Currently, Convex does not support infinity and Malli does not generate NaN.
						        ;; TODO. NaN is supported but buggy (see #67).
                                {:gen/infinite? false
                                 :gen/NaN?      false}]
          :convex/hexstring    [:re
                                {:gen/gen    42  ;; Malli bug, :gen/gen must be defined for custom generation of regex
                                 :gen/fmap   $.hex/from-int
                                 :gen/schema pos-int?}
                                $.hex/regex]
          :convex/hexstring-32 [:re
                                {:gen/gen    42  ;; Malli bug, :gen/gen must be defined for custom generation of regex
                                 :gen/fmap   $.hex/pad-32
                                 :gen/schema :convex/hexstring}
                                $.hex/regex-32]
          :convex/keyword      :keyword
          :convex/list         [:and
                                ;; TODO. Currently, Malli does not support something like `:list`.
                                {:gen/fmap   (partial into
                                                      '())
                                 :gen/schema :convex/vector}
                                seq?
                                [:sequential [:ref :convex/data]]]
          :convex/long         :int
          :convex/map          [:map-of
                                [:ref :convex/data]
                                [:ref :convex/data]]
          :convex/nil          :nil
          :convex/number       [:or
                                :convex/double
                                :convex/long]
          :convex/set          [:set
                                [:ref :convex/data]]
          :convex/string       [:string
                                 ;; TODO. Should not be necessary, see #66.
                                {:gen/gen tc.gen/string-alphanumeric}]
          :convex/symbol       [:and
                                :symbol
                                [:fn
                                 ;; TODO. Should not be necessary, see #65.
                                 (fn [x]
                                   (not (.contains (name x)
                                                   ".")))]]
          :convex/vector       [:vector [:ref :convex/data]])))


;;;;;;;;;;


(defn registry

  ""

  
  ([]

   (registry nil))


  ([registry]

   (-> registry
       data)))
