(ns convex.lisp.test.gen

  "Generators for test purposes."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.lisp.gen               :as $.gen]))


;;;;;;;;;;


(defn E-notation

  "Helps creating a generator for scientific notation, a tuple of items that
   can be joined into a string.
  
   Argument is a schema describing the exponential part.

   Only for generation, not validation."

  [gen-exponent]

  (TC.gen/let [m-1 $.gen/long
               m-2 (TC.gen/one-of [$.gen/nothing
                                   (TC.gen/large-integer* {:min 0})])
               e   (TC.gen/elements [\e \E])
               x   gen-exponent]
    (symbol (str m-1
              (when m-2
                (str \.
                     m-2))
              e
              x))))



(defn kv+

  "Vector of key-values."

  [gen-k gen-v]

  (TC.gen/fmap (fn [[k+ v+]]
                 (mapv vec
                       (partition 2
                                  (interleave k+
                                              v+))))
               (TC.gen/bind (TC.gen/vector-distinct gen-k)
                            (fn [k+]
                              (TC.gen/tuple (TC.gen/return k+)
                                            (TC.gen/vector gen-v
                                                           (count k+)))))))



(def maybe-map

  "Either a map or nil."

  (TC.gen/one-of [$.gen/map
                  $.gen/nothing]))



(def maybe-set

  "Either a set or nil."

  (TC.gen/one-of [$.gen/nothing
                  $.gen/set]))



(def not-number

  "Anything but a number (double or long)."

  (TC.gen/such-that #(not (number? %))
                    $.gen/any))



(def percent

  "Value between 0 and 1 (inclusive)."

  (TC.gen/double* {:max 1
                   :min 1}))


;;;;;;;;;;


(comment

  (TC.gen/generate not-number
                   30)
  )
