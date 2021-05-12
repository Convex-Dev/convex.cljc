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

  (:require [clojure.test            :as t]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Generative tests - Scalar values


(t/deftest nil--

  (t/is (nil? ($.test.eval/result nil))))
 


($.test.prop/deftest address

  ($.test.prop/data-quotable :convex/address))



($.test.prop/deftest blob

  ($.test.prop/data-quotable :convex/blob))



($.test.prop/deftest boolean-

  ($.test.prop/data-quotable :convex/boolean))



($.test.prop/deftest char-

  ($.test.prop/data-quotable :convex/char))



($.test.prop/deftest double-

  ($.test.prop/data-quotable :convex/double))



($.test.prop/deftest double-E-notation

  ($.test.prop/check ($.test.schema/E-notation :convex/long)
                     (comp double?
                           $.test.eval/result)))



#_($.test.prop/deftest double-E-notation--fail

  ;; TODO. Must be fixed, see #70.

  ($.test.prop/check ($.test.schema/E-notation :convex/double)
                     $.test.eval/error?))



($.test.prop/deftest keyword-

  ($.test.prop/data-quotable :convex/keyword))



($.test.prop/deftest long-

  ($.test.prop/data-quotable :convex/long))



($.test.prop/deftest string-

  ;; TODO. Suffers from #66.

  ($.test.prop/data :convex/string))



($.test.prop/deftest symbol-

  ($.test.prop/data-quoted :convex/symbol))


;;;;;;;;;; Generative tests - Collections


($.test.prop/deftest ^:recur list-

  ($.test.prop/data-quoted :convex/list))



($.test.prop/deftest ^:recur map-

  ($.test.prop/data-quoted :convex/map))



($.test.prop/deftest ^:recur set-

  ($.test.prop/data-quoted :convex/set))



($.test.prop/deftest vector-

  ($.test.prop/data-quoted :convex/vector))
