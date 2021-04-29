(ns convex.lisp.test.data

  "Testing literal notation of Convex data (scalar values and collections).
  
   Consists of a cycle such as:
   
   - Generate value as Clojure data
   - Convert Clojure data to Convex Lisp source
   - Read Convex Lisp source
   - Eval Convex Lisp Source
   - Convert result to Clojure data
   - Result must be equal to generate value
  
   Also test quoting when relevant. For instance, like in Clojure, quoting a number must result in this number."

  {:author "Adam Helinski"}

  (:require [clojure.core]
            [clojure.test                    :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.util           :as $.test.util])
  (:refer-clojure :exclude [boolean
                            char
                            double
                            keyword
                            list
                            long
							map
                            set
                            symbol
                            vector]))


;;;;;;;;;; Defaults


(def max-size-coll
     5)


;;;;;;;;;; Helpers


(defn cycle-quotable

  "Cycles `x`, after quoting it, as described in the namespace description, and returns true
   if the result equals `x`."

  [x]

  (let [x-str ($/clojure->source x)]
    ($.test.util/eq x
                    ($.test.util/source->clojure x-str)
                    ($.test.util/source->clojure (str "'" x-str)))))


;;;;;;;;;; Creating properties


(defn property

  "Returns a property that goes through the cycle described in the namespace description.
  
   `f` can be provided for mapping a generated result."

  
  ([k-schema]

   (property k-schema
             identity))


  ([k-schema f]

   (tc.prop/for-all* [($.test.util/generator k-schema)]
                     (fn [x]
                       ($.test.util/eq x
                                       (-> x
                                           f
                                           $/clojure->source
                                           $.test.util/source->clojure))))))



(defn property-quotable

  "Like [[property]] but ensures that quoting the generated value does not change anything in the result."

  [k-schema]

  (tc.prop/for-all* [($.test.util/generator k-schema)]
                    cycle-quotable))



(defn property-quoted

  "Like [[property]] but quotes the generated values.
  
   Useful for collections since they might contain symbol which, unquoted, are evaled."

  [k-schema]

  (property k-schema
            $/quote-clojure))


;;;;;;;;;; Generative tests
;;;;;;;;;;
;;;;;;;;;; Cycles of generating values, converting to source, reading, compiling, and comparing result


;;;;;;;;;; Generative tests - Scalar values


(t/deftest -nil

  (t/is (nil? ($.test.util/source->clojure "nil"))))
 


(tc.ct/defspec address

  (property-quotable :convex/address))



(tc.ct/defspec blob

  (property-quotable :convex/blob))



(tc.ct/defspec boolean

  (property-quotable :convex/boolean))



(tc.ct/defspec char

  (property-quotable :convex/char))



(tc.ct/defspec double

  (tc.prop/for-all* [($.test.util/generator :convex/double)]
                    (fn [x]
                      (if (Double/isNaN x)
                        (Double/isNaN (-> x
                                          $/clojure->source
                                          $.test.util/source->clojure))
                        (cycle-quotable x)))))



(tc.ct/defspec keyword

  (property-quotable :convex/keyword))



(tc.ct/defspec long

  (property-quotable :convex/long))



(tc.ct/defspec string

  ;; TODO. Suffers from #66.

  (property :convex/string))



(tc.ct/defspec symbol

  ;; TODO. Suffers from #65.

  (property-quoted :convex/symbol))


;;;;;;;;;; Generative tests - Collections


(tc.ct/defspec list

  {:max-size max-size-coll}

  (property-quoted :convex/list))



(tc.ct/defspec map

  {:max-size max-size-coll}

  (property-quoted :convex/map))



(tc.ct/defspec set

  {:max-size max-size-coll}

  (property-quoted :convex/set))



(tc.ct/defspec vector

  {:max-size max-size-coll}

  (property-quoted :convex/vector))
