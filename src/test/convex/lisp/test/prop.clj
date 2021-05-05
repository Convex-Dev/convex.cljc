(ns convex.lisp.test.prop

  "Building common `test.check` properties."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as tc.prop]
            [convex.lisp                   :as $]
            [convex.lisp.test.util         :as $.test.util]))


(declare like-clojure)


;;;;;;;;;;


(defn arithmetic

  "Applies a vector of numbers to `sym-convex` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [sym-convex]

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/long])]
                    (fn [x]
                      ($.test.util/prop+

                        "Numerical computation of longs must result in a long"
                        (int? ($.test.util/eval (list* sym-convex
                                                       x)))

                        "Numerical computation with at least one double must result in a double"
                        (double? ($.test.util/eval (list* sym-convex
                                                          (update x
                                                                  (rand-int (dec (count x)))
                                                                  double))))))))



(defn coerce

  "Coerce a value generated from `schema` by applying it to `sym-convex-cast`.
  
   Tests at least 2 properties:
  
   - Is the result consistent with Clojure by applying that value to `clojure-pred`?
   - Does the CVM confirm the result is of the right type by applying it to `sym-convex-pred`?
  
   If `clojure-cast is provided, 1 additional property is checked:
  
   - Does casting in Clojure provide the exact same result?"


  ([sym-convex-cast sym-convex-pred clojure-pred schema]

   (coerce sym-convex-cast
           sym-convex-pred
           nil
           clojure-pred
           schema))


  ([sym-convex-cast sym-convex-pred clojure-cast clojure-pred schema]

   (tc.prop/for-all* [($.test.util/generator schema)]
                     (let [suite   (fn [_x x-2 cast?]
                                     ($.test.util/prop+

                                       "Consistent with Clojure"
                                       (clojure-pred x-2)

                                       "Properly cast"
                                       cast?))
                           suite-2 (if clojure-cast
                                     (fn [x x-2 cast?]
                                       ($.test.util/prop+

                                         "Basic tests"
                                         (suite x
                                                x-2
                                                cast?)

                                         "Comparing cast with Clojure's"
                                         (= x-2
                                            (clojure-cast x))))
                                     suite)]
                       (fn [x]
                         (let [[x-2
                                cast?] ($.test.util/eval ($/templ {'?sym-cast sym-convex-cast
                                                                   '?sym-pred sym-convex-pred
                                                                   '?x        x}
                                                                  '(let [x-2 (?sym-cast (quote ?x))]
                                                                     [x-2
                                                                      (?sym-pred x-2)])))]
                           (suite-2 x
                                    x-2
                                    cast?)))))))



(defn comparison

  "Checks if applying numbers to `sym-convex` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [sym-convex f-clojure]

  (like-clojure sym-convex
                f-clojure
                [:vector
                 {:min 1}
                 :convex/number]))



(defn like-clojure

  "Checks if calling `sym-convex` on the CVM with the generated value(s) produces the exact same result
   as in Clojure by using `f-clojure`."

  [sym-convex f-clojure schema]

  (tc.prop/for-all* [($.test.util/generator schema)]
                    (fn [x]
                      ($.test.util/eq (apply f-clojure
                                             x)
                                      ($.test.util/eval (list* sym-convex
                                                               x))))))



(defn pred-data-false

  "Like [[pred-data-true]] but tests for negative results.
  
   Provided schema is a set of data types meant to be removed from `:convex/data`."


  ([sym-convex schema-without]

   (pred-data-false sym-convex
                    nil
                    schema-without))


  ([sym-convex f-clojure schema-without]

   (tc.prop/for-all* [($.test.util/generator-data-without schema-without)]
                     (if f-clojure
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred sym-convex
                                                          x)]
                           ($.test.util/prop+

                             "Always returns false"
                             (not x-2)

                             "Consistent with Clojure"
                             (= x-2
                                (f-clojure x)))))
                       (fn [x]
                         (not ($.test.util/eval-pred sym-convex
                                                     x)))))))



(defn pred-data-true

  "Tests if a value generated by ´schema` passes a data predicate on the CVM.
  
   If `f-clojure`is given, also ensures that that same value produces the exact same result
   in Clojure."


  ([sym-convex schema]

   (pred-data-true sym-convex
                   nil
                   schema))


  ([sym-convex f-clojure schema]

   (tc.prop/for-all* [($.test.util/generator schema)]
                     (if f-clojure
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred sym-convex
                                                          x)]
                           ($.test.util/prop+

                             "Always returns true"
                             x-2

                             "Consistent with Clojure"
                             (= x-2
                                (f-clojure x)))))
                       (fn [x]
                         ($.test.util/eval-pred sym-convex
                                                x))))))
