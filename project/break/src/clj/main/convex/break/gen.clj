(ns convex.break.gen

  "Generators for `test.check` used throughout the Break test suite.
  
   Complements generators from the [[convex.clj.gen]] namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std])

  #_(:require [clojure.test.check.generators :as TC.gen]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [convex.clj.translate          :as $.clj.translate]))


;;;;;;;;;;


(defn any-but

  "Anything returning false on the given `pred`, quoted."

  [pred]

  (TC.gen/fmap $.cell/quoted
               (TC.gen/such-that (comp not
                                       pred)
                                 $.gen/any)))



(defn binding-raw+

  "Returns a 2-tuple containing a CVX vector of unique symbols and a CVX vector of quoted values.

   Returned a Clojure vector for easy destructuring.

   See [[binding+]]."


  ([n-min n-max]

   (binding-raw+ n-min
                 n-max
                 $.gen/any))


  ([n-min n-max gen-value]

   (TC.gen/let [sym+ (TC.gen/vector-distinct $.gen/symbol
                                             {:max-elements n-max
                                              :min-elements n-min})
                x+   (TC.gen/vector (TC.gen/fmap $.cell/quoted
                                                 gen-value)
                                    (count sym+))]
     [($.cell/vector sym+)
      ($.cell/vector x+)])))



(defn binding+

  "Vector of `symbol value...` where symbols are garanteed to be unique.

   An alternative generator for values can be provided.
  
   Useful for generating `let`-like bindings."


  ([n-min n-max]

   (binding+ n-min
             n-max
             $.gen/any))


  ([n-min n-max gen-value]

   (TC.gen/let [[sym+
                 x+]  (binding-raw+ n-min
                                    n-max
                                    gen-value)]
     ($.cell/vector (interleave sym+
                                x+)))))



(def not-address

  "Anything but an address, quoted."

  (any-but $.std/address?))



(def not-long

  "Anything but a long, quoted."

  (any-but #(or ($.std/byte? %)
                ($.std/long? %))))



(def not-number

  "Anything that is not a number, quoted."

  (any-but $.std/number?))



(def percent

  "Double between 0 and 1."

  ($.gen/double-bounded {:max 1
                         :min 0}))



(defn unused-address

  "Address that is not being used yet."

  [ctx]

  (TC.gen/such-that #($.eval/true? ctx
                                   ($.cell/* (nil? (account ~%))))
                    $.gen/address
                    100))



(comment





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
                         (comp (map (comp $.clj.translate/cvx->clj
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


)
