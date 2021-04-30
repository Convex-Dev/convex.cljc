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

  (:require[clojure.test                     :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.util           :as $.test.util]))


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
                    ($.test.util/eval-source x-str)
                    ($.test.util/eval-source (str "'" x-str)))))



(defn generator-E-notation

  "Helps creating a generator for scientific notation."

  [schema-exponent]

  ($.test.util/generator [:tuple
                          {:gen/fmap (fn [[m-1 m-2 e x]]
                                       (str m-1
                                            \.
                                            m-2
                                            e
                                            x))}
                          :convex/long
                          [:and
                           :convex/long
                           [:>= 0]]
                          [:enum
                           \e
                           \E]
                          schema-exponent]))


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
                                           $.test.util/eval-source))))))



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

  (t/is (nil? ($.test.util/eval-source "nil"))))
 


(tc.ct/defspec address

  (property-quotable :convex/address))



(tc.ct/defspec blob

  (property-quotable :convex/blob))



(tc.ct/defspec -boolean

  (property-quotable :convex/boolean))



(tc.ct/defspec -char

  (property-quotable :convex/char))



(tc.ct/defspec -double

  (tc.prop/for-all* [($.test.util/generator :double)]
                    (fn [x]
                      (if (Double/isNaN x)
                        (Double/isNaN (-> x
                                          $/clojure->source
                                          $.test.util/eval-source))
                        (cycle-quotable x)))))



(tc.ct/defspec -double-E-notation

  ;; TODO. Also ensure failing (see #70).

  (tc.prop/for-all* [(generator-E-notation :convex/long)]
                    #(-> %
                         $.test.util/eval-source
                         double?)))



#_(tc.ct/defspec -double-E-notation-fail

  ;; TODO. Must be fixed, see #70.

  (tc.prop/for-all* [(generator-E-notation :double)]
                    $.test.util/eval-exceptional-source))



(tc.ct/defspec -keyword

  (property-quotable :convex/keyword))



(tc.ct/defspec -long

  (property-quotable :convex/long))



(tc.ct/defspec -string

  ;; TODO. Suffers from #66.

  (property :convex/string))



(tc.ct/defspec -symbol

  ;; TODO. Suffers from #65.

  (property-quoted :convex/symbol))


;;;;;;;;;; Generative tests - Collections


(tc.ct/defspec -list

  {:max-size max-size-coll}

  (property-quoted :convex/list))



(tc.ct/defspec -map

  {:max-size max-size-coll}

  (property-quoted :convex/map))



(tc.ct/defspec -set

  {:max-size max-size-coll}

  (property-quoted :convex/set))



(tc.ct/defspec -vector

  {:max-size max-size-coll}

  (property-quoted :convex/vector))
