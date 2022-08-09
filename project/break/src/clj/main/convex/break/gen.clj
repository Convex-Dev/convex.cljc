(ns convex.break.gen

  "Generators for `test.check` used throughout the Break test suite.
  
   Complements generators from the [[convex.gen]] namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cell                   :as $.cell]
            [convex.cvm                    :as $.cvm]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]))


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



(defn core-symbol

  "Any of the core symbols."

  [ctx]

  (TC.gen/elements (into []
                         (filter #(not (contains? #{($.cell/* actor)  ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                                                    ($.cell/* expand) ;; TODO. https://github.com/Convex-Dev/convex/issues/149
                                                    ($.cell/* fn)     ;; TODO. https://github.com/Convex-Dev/convex/issues/152
                                                    }
                                                  %)))
                         ($.std/keys ($.cvm/env ctx
                                                8)))))



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
