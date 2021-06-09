(ns convex.break.gen

  "Generators used throughout this test suite."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]))


(declare kv+)


;;;;;;;;;;


(defn E-notation

  "Helps creating a generator for scientific notation, a tuple of items that
   can be joined into a string.
  
   Argument is a schema describing the exponential part.

   Only for generation, not validation."

  [gen-exponent]

  (TC.gen/let [m-1 $.clj.gen/long
               m-2 (TC.gen/one-of [$.clj.gen/nothing
                                   (TC.gen/large-integer* {:min 0})])
               e   (TC.gen/elements [\e \E])
               x   gen-exponent]
    (symbol (str m-1
              (when m-2
                (str \.
                     m-2))
              e
              x))))



(def maybe-map

  "Either a map or nil."

  (TC.gen/one-of [$.clj.gen/map
                  $.clj.gen/nothing]))



(def maybe-set

  "Either a set or nil."

  (TC.gen/one-of [$.clj.gen/nothing
                  $.clj.gen/set]))



(def not-address

  "Anything but an address."

  (TC.gen/such-that #(not ($.clj/address? %))
                    $.clj.gen/any))



(def not-collection

  "Anything but a proper collection."

  (TC.gen/such-that some?
                    $.clj.gen/scalar))



(def not-long

  "Anything but a long."

  (TC.gen/such-that #(not (int? %))
                    $.clj.gen/any))



(def not-number

  "Anything but a number (double or long)."

  (TC.gen/such-that #(not (number? %))
                    $.clj.gen/any))



(def percent

  "Value between 0 and 1 (inclusive)."

  (TC.gen/double* {:infinite? false
                   :max       1
                   :min       0
                   :NaN?      false}))



(def unused-address

  "Address that is not being used yet."

  (TC.gen/such-that #($.clj.eval/result* (nil? (account ~%)))
                    $.clj.gen/address
                    100))


;;;;;;;;;; Core


(def core-symbol

  "Any of the core symbols."

  (TC.gen/elements (into []
                         (comp (map (comp $.cvm/as-clojure
                                          first))
                               (filter #(not (contains? #{'actor  ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                                                          'expand ;; TODO. https://github.com/Convex-Dev/convex/issues/149
                                                          'fn     ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                                                          }
                                                        %))))
                         ($.cvm/env (or $.clj.eval/*ctx-default*
                                        ($.cvm/ctx))
                                    8))))

;;;;;;;;;;


(comment

  (TC.gen/generate core-symbol
                   30)
  )
