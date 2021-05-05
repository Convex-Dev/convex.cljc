(ns convex.lisp.test.prop

  "Building common `test.check` properties."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as tc.prop]
            [convex.lisp                   :as $]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.util         :as $.test.util]))


(declare like-clojure)


;;;;;;;;;;


(defn arithmetic

  "Applies a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/long])]
                    (fn [x]
                      ($.test.util/prop+

                        "Numerical computation of longs must result in a long"
                        (int? ($.test.eval/form (list* form
                                                       x)))

                        "Numerical computation with at least one double must result in a double"
                        (double? ($.test.eval/form (list* form
                                                          (update x
                                                                  (rand-int (dec (count x)))
                                                                  double))))))))



(defn coerce

  "Coerce a value generated from `schema` by applying it to `form-cast`.
  
   Tests at least 2 properties:
  
   - Is the result consistent with Clojure by applying that value to `clojure-pred`?
   - Does the CVM confirm the result is of the right type by applying it to `form-pred`?
  
   If `clojure-cast is provided, 1 additional property is checked:
  
   - Does casting in Clojure provide the exact same result?"


  ([form-cast form-pred clojure-pred schema]

   (coerce form-cast
           form-pred
           nil
           clojure-pred
           schema))


  ([form-cast form-pred clojure-cast clojure-pred schema]

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
                                cast?] ($.test.eval/form ($/templ {'?sym-cast form-cast
                                                                   '?sym-pred form-pred
                                                                   '?x        x}
                                                                  '(let [x-2 (?sym-cast (quote ?x))]
                                                                     [x-2
                                                                      (?sym-pred x-2)])))]
                           (suite-2 x
                                    x-2
                                    cast?)))))))



(defn comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f-clojure]

  (like-clojure form
                f-clojure
                [:vector
                 {:min 1}
                 :convex/number]))



(defn like-clojure

  "Checks if calling `form` on the CVM with the generated value(s) produces the exact same result
   as in Clojure by using `f-clojure`."

  [form f-clojure schema]

  (tc.prop/for-all* [($.test.util/generator schema)]
                    (fn [x]
                      ($.test.util/eq (apply f-clojure
                                             x)
                                      ($.test.eval/form (list* form
                                                               x))))))



(defn pred-data-false

  "Like [[pred-data-true]] but tests for negative results.
  
   Provided schema is a set of data types meant to be removed from `:convex/data`."


  ([form schema-without]

   (pred-data-false form
                    nil
                    schema-without))


  ([form f-clojure schema-without]

   (tc.prop/for-all* [($.test.util/generator-data-without schema-without)]
                     (if f-clojure
                       (fn [x]
                         (let [x-2 ($.test.eval/apply-one form
                                                          x)]
                           (println :x-2 x-2)
                           ($.test.util/prop+

                             "Always returns false"
                             (not x-2)

                             "Consistent with Clojure"
                             (= x-2
                                (f-clojure x)))))
                       (fn [x]
                         (not ($.test.eval/apply-one form
                                                     x)))))))



(defn pred-data-true

  "Tests if a value generated by ´schema` passes a data predicate on the CVM.
  
   If `f-clojure` is given, also ensures that the very same value produces the exact same result
   in Clojure."


  ([form schema]

   (pred-data-true form
                   nil
                   schema))


  ([form f-clojure schema]

   (tc.prop/for-all* [($.test.util/generator schema)]
                     (if f-clojure
                       (fn [x]
                         (let [x-2 ($.test.eval/apply-one form
                                                          x)]
                           ($.test.util/prop+

                             "Always returns true"
                             x-2

                             "Consistent with Clojure"
                             (= x-2
                                (f-clojure x)))))
                       (fn [x]
                         ($.test.eval/apply-one form
                                                x))))))
