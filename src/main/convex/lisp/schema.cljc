(ns convex.lisp.schema

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as tc.gen]))


;;;;;;;;;;


(defn data

  ""

  ;; TODO. No address nor blob since no equivalent litteral notation.


  ([]

   (data nil))


  ([registry]

   (assoc registry
          :convex/boolean :boolean
          :convex/char    char?
          :convex/data    [:or
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
          :convex/double  [:double
                           ;; TODO. Currently, Convex does not support infinity and Malli does not generate NaN.
						   ;; TODO. NaN is supported but buggy (see #67).
                           {:gen/infinite? false
                            :gen/NaN?      false}]
          :convex/keyword :keyword
          :convex/list    [:and
                           ;; TODO. Currently, Malli does not support something like `:list`.
                           {:gen/fmap   #(or (list* %)
                                             '())
                            :gen/schema :convex/vector}
                           seq?
                           [:sequential [:ref :convex/data]]]
          :convex/long    :int
          :convex/map     [:map-of
                           [:ref :convex/data]
                           [:ref :convex/data]]
          :convex/nil     :nil
          :convex/set     [:set
                           [:ref :convex/data]]
          :convex/string  :string
          :convex/symbol  :symbol
          :convex/vector  [:vector [:ref :convex/data]])))


;;;;;;;;;;


(defn registry

  ""

  
  ([]

   (registry nil))


  ([registry]

   (-> registry
       data)))
