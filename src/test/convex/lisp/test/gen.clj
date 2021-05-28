(ns convex.lisp.test.gen

  "Generators for test purposes."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]))


(declare kv+
         mix-one-in)


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

  "Vector of `[Key Value]`."

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



(defn mix-one-in

  "Ensures that an item from `gen-one` is present and shuffled in `gen-coll`."

  [gen-one gen-coll]

  (TC.gen/let [x  gen-one
               x+ gen-coll]
    (TC.gen/shuffle (conj x+
                          x))))



(def not-address

  "Anything but an address."

  (TC.gen/such-that #(not ($.lisp/address? %))
                    $.gen/any))



(def not-collection

  "Anything but a proper collection."

  (TC.gen/such-that some?
                    $.gen/scalar))



(def not-long

  "Anything but a long."

  (TC.gen/such-that #(not (int? %))
                    $.gen/any))



(def not-number

  "Anything but a number (double or long)."

  (TC.gen/such-that #(not (number? %))
                    $.gen/any))



(defn outlier

  "Produces a vector of items where each item is either a good item or anything.
  
   Ensures that at least one wrong item is produced.
  
   Both kind can be given explicitly or a set of good generators can be given from which
   a \"bad\" generator can be deduced."


  ([set-gen-good]

   (outlier (TC.gen/one-of (vec set-gen-good))
            ($.gen/any-but set-gen-good)))


  ([gen-good gen-wrong]

   (mix-one-in gen-wrong
               (TC.gen/vector (TC.gen/one-of [gen-good
                                              $.gen/any])))))



(def percent

  "Value between 0 and 1 (inclusive)."

  (TC.gen/double* {:infinite? false
                   :max       1
                   :min       0
                   :NaN?      false}))



(def unused-address

  "Address that is not being used yet."

  (TC.gen/such-that #($.test.eval/result* (nil? (account ~%)))
                    $.gen/address
                    100))


;;;;;;;;;;


(comment

  (TC.gen/generate unused-address
                   30)
  )
