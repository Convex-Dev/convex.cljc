(ns convex.lisp.test.data

  "Testing literal notation of Convex data (scalar values and collections).
  
   Consists of a cycle such as:
   
   - Generate value as Clojure data
   - Convert Clojure data to Convex Lisp source
   - Read Convex Lisp source
   - Eval Convex Lisp Source
   - Convert result to Clojure data
   - Result must be equal to generate value
  
   Also test quoting when relevant. For instance, like in Clojure, quoting a number must result in this very same number."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.schema         :as $.test.schema]))


;;;;;;;;;; Defaults


(def max-size-coll
     5)

;;;;;;;;;; Generative tests - Scalar values


(t/deftest nil--

  (t/is (nil? ($.test.eval/form nil))))
 


(tc.ct/defspec address

  ($.test.prop/data-quotable :convex/address))



(tc.ct/defspec blob

  ($.test.prop/data-quotable :convex/blob))



(tc.ct/defspec boolean-

  ($.test.prop/data-quotable :convex/boolean))



(tc.ct/defspec char-

  ($.test.prop/data-quotable :convex/char))



(tc.ct/defspec double-

  ($.test.prop/data-quotable :convex/double))



(tc.ct/defspec double-E-notation

  ($.test.prop/check ($.test.schema/E-notation :convex/long)
                     (fn [x]
                       (-> x
                           $.test.eval/source
                           double?))))



#_(tc.ct/defspec double-E-notation--fail

  ;; TODO. Must be fixed, see #70.

  ($.test.prop/check ($.test.schema/E-notation :convex/double)
                     $.test.eval/source-error?))



(tc.ct/defspec keyword-

  ($.test.prop/data-quotable :convex/keyword))



(tc.ct/defspec long-

  ($.test.prop/data-quotable :convex/long))



(tc.ct/defspec string-

  ;; TODO. Suffers from #66.

  ($.test.prop/data :convex/string))



(tc.ct/defspec symbol-

  ($.test.prop/data-quoted :convex/symbol))


;;;;;;;;;; Generative tests - Collections


(tc.ct/defspec list-

  {:max-size max-size-coll}

  ($.test.prop/data-quoted :convex/list))



(tc.ct/defspec map-

  {:max-size max-size-coll}

  ($.test.prop/data-quoted :convex/map))



(tc.ct/defspec set-

  {:max-size max-size-coll}

  ($.test.prop/data-quoted :convex/set))



(tc.ct/defspec vector-

  {:max-size max-size-coll}

  ($.test.prop/data-quoted :convex/vector))
