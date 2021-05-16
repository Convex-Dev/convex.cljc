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
            [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]
            [convex.lisp.test.util   :as $.test.util])) 


;;;;;;;;;; Reusing properties


(defn prop-eq

  "Checks generating a value from `schema` and evaling it. Result must be equal to initial value.

   `f` can be provided for mapping a generated value prior to evaling."

  
  ([schema]

   (prop-eq schema
              identity))


  ([schema f]

   ($.test.prop/check schema
                      (fn [x]
                        ($.test.util/eq x
                                        ($.test.eval/result (list 'identity
                                                                  (f x))))))))



(defn prop-quotable

  "Like [[prop-eq]] but ensures that quoting the generated value does not change anything in the result."

  [schema]

  ($.test.prop/check schema
                     (fn [x]
                       ($.test.util/eq x
                                       ($.test.eval/result (list 'identity
                                                                 x))
                                       ($.test.eval/result ($.form/quoted x))))))



(defn prop-quoted

  "Like [[prop-eq]] but quotes the generated values.
  
   Useful for preventing any symbol from being evaled."

  [schema]

  (prop-eq schema
           $.form/quoted))


;;;;;;;;;; Scalar values


(t/deftest nil--

  (t/is (nil? ($.test.eval/result nil))))
 


($.test.prop/deftest address

  (prop-quotable :convex/address))



($.test.prop/deftest blob

  (prop-quotable :convex/blob))



($.test.prop/deftest boolean-

  (prop-quotable :convex/boolean))



($.test.prop/deftest char-

  (prop-quotable :convex/char))



($.test.prop/deftest double-

  (prop-quotable :convex/double))



($.test.prop/deftest double-E-notation

  ($.test.prop/check ($.test.schema/E-notation :convex/long)
                     (comp double?
                           $.test.eval/result)))



#_($.test.prop/deftest double-E-notation--fail

  ;; TODO. Must be fixed, see #70.

  ($.test.prop/check ($.test.schema/E-notation :convex/double)
                     $.test.eval/error?))



($.test.prop/deftest keyword-

  (prop-quotable :convex/keyword))



($.test.prop/deftest long-

  (prop-quotable :convex/long))



($.test.prop/deftest string-

  ;; TODO. Suffers from #66.

  (prop-eq :convex/string))



($.test.prop/deftest symbol-

  (prop-quoted :convex/symbol))


;;;;;;;;;; Generative tests - Collections


($.test.prop/deftest ^:recur list-

  (prop-quoted :convex/list))



($.test.prop/deftest ^:recur map-

  (prop-quoted :convex/map))



($.test.prop/deftest ^:recur set-

  (prop-quoted :convex/set))



($.test.prop/deftest ^:recur vector-

  (prop-quoted :convex/vector))
