(ns convex.lisp.schema

  "Creating Malli schemas for working with Convex.
  
   See [[registry]]."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]
            [convex.lisp.hex               :as $.hex]))


;;;;;;;;;; Schemas


(defn data

  "Adds to the given `registry` all schemas for Convex data types."


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
                                {:gen/fmap   $.hex/to-blob-symbol
                                 :gen/schema :convex/hexstring}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex
                                                        (name sym))))]]
		  :convex/blob-8	   [:and
								{:gen/fmap   $.hex/to-blob-symbol
                                 :gen/schema :convex/hexstring-8}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex-8
                                                        (name sym))))]]
		  :convex/blob-32	   [:and
								{:gen/fmap   $.hex/to-blob-symbol
                                 :gen/schema :convex/hexstring-32}
                                :symbol
                                [:fn
                                 (fn [sym]
                                   (boolean (re-matches $.hex/regex-32
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
          :convex/hash         :convex/blob-32
          :convex/hexstring    [:re
                                {:gen/fmap   $.hex/from-int
                                 :gen/schema pos-int?}
                                $.hex/regex]
          :convex/hexstring-8  [:re
                                {:gen/fmap   $.hex/pad-8
                                 :gen/schema :convex/hexstring}
                                $.hex/regex-8]
          :convex/hexstring-32 [:re
                                {:gen/fmap   $.hex/pad-32
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
                                [:not= '_]
                                [:fn
                                 ;; TODO. Checking for "." should not be necessary, see #65.
                                 (fn [x]
                                   (let [string (name x)]
                                     (and (not (clojure.string/includes? string
                                                                         "."))
                                          (not (clojure.string/starts-with? string
                                                                            "#"))
                                          (not (clojure.string/starts-with? string
                                                                            "0x")))))]]
          :convex/vector       [:vector [:ref :convex/data]])))



(defn sym-coercible

  "Returns a schema for a value that can be coerced to a Convex keyword or symbol."

  []

  [:or
   :convex/keyword
   :convex/symbol
   [:and
    :convex/string
    [:fn
     #(< 0
         (count %)
         32)]]])


;;;;;;;;;;


(defn registry

  "Adds to the given `registry` all relevant schemas from this namespace for working with Convex.
  
   When used as a standalone registry, requires the core Malli registry."

  
  ([]

   (registry nil))


  ([registry]

   (-> registry
       data)))
