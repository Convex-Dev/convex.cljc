(ns convex.lisp.test.prop

  "Building common `test.check` properties."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.clojure-test :as tc.ct]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.results      :as tc.result]
            [convex.lisp.form                :as $.form]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.schema         :as $.test.schema]
            [convex.lisp.test.util           :as $.test.util]))


(declare fail
         like-clojure)


;;;;;;;;;; Defining generative tests


(defmacro deftest

  "Like `clojure.test.check.clojure-test/defspec`.

   Difference is that the number of tests and maximum size can be easily configured
   at the level of the whole test suite and also by providing metadata:

   | Key | Effect |
   |---|---|
   | `:recur` | When involving recursive collections, reduces max size to 5 |

   ```clojure
   (defcheck some-test

     (check :double
            (fn [x]
              (double? x))))
   ```"

  ([sym prop]

   `(deftest ~sym
             nil
             ~prop))


  ([sym option+ prop]

   `(tc.ct/defspec ~sym
                   ~(merge (when (get (meta sym)
                                      :recur)
                             {:max-size 5})
                           {:num-tests 100}
                           option+)
                   ~prop)))


;;;;;;;;;; Helpers for writing properties


(defn- -and*

  ;; Helper for [[and*]]

  [form+]

  (let [[form
         & form-2+] form+]
    (if form-2+
      `(let [x# ~form]
         (if (true? x#)
           ~(-and* form-2+)
           x#))
      form)))



(defmacro and*

  "Useful for testing different [[checkpoint]]s and failing fast.
  
   Behaves like Clojure's `and` but only `true` is a truthy value.
  
   ```clojure
   (and* (checkpoint* \"Property a\"
                      (prop-a ...))
         (checkpoint* \"Property b\"
                      (prop-b ...)))
   ```"

  [& form+]

  (if (seq form+)
    (-and* form+)
    true))



(defn check

  "Returns a property generating `schema` against `f`."

  [schema f]

  (tc.prop/for-all* [($.test.schema/generator schema)]
                    f))



(defn checkpoint

  "Meant to be used inside a property.

   When `f` does not return `true`, this function use [[fail]] with the
   given beacon.

   If `f` returns a failure, using [[fail]] allows beacons to be composed, allowing
   to track exactly what failed in case of nested checkpoints.

   Also catches and document exceptions using [[fail]].

   Any result other than `true`, `false`, or a failure akin to what [[fail]] returns will
   result in an exception.

   ```clojure
   (checkpoint \"Should be greather than threshold\"
               (fn [] (> x threshold)))
   ```

   See macro variant [[checkpoint*]]."


  ([[beacon f]]

   (checkpoint beacon
               f))


  ([beacon f]

   (try
     (let [x (f)]
       (cond
         (true? x)                    true
         (false? x)                   (fail beacon)
         (satisfies? tc.result/Result
                     x)               (fail x
                                            beacon)
         (instance? Throwable
                    x)                x
         :else                        (throw (ex-info "Property multiplexing does not understand returned value"
                                                      {::result x}))))
     (catch Throwable e
       (fail [beacon
              e])))))



(defmacro checkpoint*

  "See [[checkpoint]]
  
   ```clojure
   (checkpoint*

      \"Testing something\"

      (something :a :b :c))
   ```"

  [beacon form]

  `(checkpoint ~beacon
               (fn [] ~form)))



(defn fail

  "Returns a `test.check` error with `checkpoint` conjed to `:convex.lisp/error`
   of the `test.check` error data (see `clojure.test.check.results` namespace)."


  ([checkpoint]

   (reify tc.result/Result

     (pass? [_]
       false)

     (result-data [_]
       {:convex.lisp/error [checkpoint]})))


  ([failure checkpoint]

   (let [result (update (tc.result/result-data failure)
                        :convex.lisp/error
                        (partial into
                                 [checkpoint]))]
     (reify tc.result/Result

       (pass? [_]
         false)

       (result-data [_]
         result)))))



(defn mult

  "Meant to be used inside a `test.check` property in order to multiplex it while keeping
   track of which \"sub-property\" failed.
  
   Tests each pair of checkpoint and function. Fails with [[faill]] and the message when
   a predicate returns false.

   A checkpoint could be anything. Most commonly a human readable string.

   Composes with itself. A function could be another call to [[mult]] and in case of failure,
   all checkpoints leading to it figure under `:convex.lisp/error` (see [[fail]]).

   ```clojure
   (mult [[\"3 must be greater than 4\"
           #(< 3 4)]

          [\"Result must be a double\"
           #(double? ...)])
   ```"

  [prop-pair+]

  (reduce (fn [_acc prop-pair]
            (let [x (checkpoint prop-pair)]
              (or (true? x)
                  (reduced x))))
          true
          prop-pair+))



(defmacro mult*

  "Macro akin to [[mult]] for a ligher syntax.
  
   ```clojure
   (mult*
  
     \"3 must be greater than 4\"
     (< 3 4)

     \"Result must be a double\"
     (double? ...))
   ```"

  [& prop-pair+]

  `(mult ~(mapv (fn [[checkpoint form]]
                  [checkpoint
                   `(fn [] ~form)])
                (partition 2
                           prop-pair+))))



(defn mult-result

  "Working with collection of results obtained from evaling Convex Lisp code, returns a [[fail]] with the
   corresponding checkpoint (position-wise) when a false result is encountered.

   ```clojure
   (mult-result [true
                 false] ;; Vector of results
                [\"3 must be greater than 4\"
                 \"Result must be a double\"])
   ```"

  [result+ checkpoint+]

  (assert (= (count checkpoint+)
             (count result+)))
  (or (some (fn [[result checkpoint+]]
              (when-not result
                (fail checkpoint+)))
            (partition 2
                       (interleave result+
                                   checkpoint+)))
      true))


;;;;;;;;;; Building properties


(defn arithmetic

  "Checks applies a vector of numbers to `form` on the CVM.
  
   Tests 2 properties:
  
   - Using only longs results in a long
   - Using at least one double results in a double"

  [form]

  (check [:vector
          {:min 1}
          :convex/long]
         (fn [x]
           (mult* "Numerical computation of longs must result in a long"
                  (int? ($.test.eval/result (list* form
                                                   x)))

                  "Numerical computation with at least one double must result in a double"
                  (double? ($.test.eval/result (list* form
                                                      (update x
                                                              (rand-int (dec (count x)))
                                                              double))))))))



(defn coerce

  "Checks coercing a value generated from `schema` by applying it to `form-cast`.
  
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

   (check schema
          (let [suite   (fn [_x x-2 cast?]
                          [["Consistent with Clojure"
                            #(clojure-pred x-2)]

                           ["Properly cast"
                            #(identity cast?)]])
                suite-2 (if clojure-cast
                          (fn [x x-2 cast?]
                            (conj (suite x
                                         x-2
                                         cast?)
                                  ["Comparing cast with Clojure's"
                                   #(= x-2
                                       (clojure-cast x))]))
                          suite)]
            (fn [x]
              (let [[x-2
                     cast?] ($.test.eval/result ($.form/templ {'?sym-cast form-cast
                                                               '?sym-pred form-pred
                                                               '?x        x}
                                                              '(let [x-2 (?sym-cast (quote ?x))]
                                                                 [x-2
                                                                  (?sym-pred x-2)])))]
                (mult (suite-2 x
                               x-2
                               cast?))))))))



(defn comparison

  "Checks if applying numbers to `form` on the CVM produces the exact same result (a boolean)
   as in Clojure."

  [form f-clojure]

  (like-clojure form
                f-clojure
                [:vector
                 {:min 1}
                 :convex/number]))



(defn create-data

  "Returns a property checking that creating data using `form` (a variadic CVM function) produces
   the same result as in Clojure using `f-clojure`."

  [form f-clojure]

  (check [:vector
          :convex/data]
         (fn [x]
           ($.test.util/eq (apply f-clojure
           	                	  x)
                           ($.test.eval/result (list* form
                                                      (map $.form/quoted
                                                           x)))))))



(defn data

  "Checks generating a value from `schema` and evaling it. Result must be equal to initial value.

   `f` can be provided for mapping a generated value prior to evaling."

  
  ([schema]

   (data schema
         identity))


  ([schema f]

   (check schema
          (fn [x]
            ($.test.util/eq x
                            (-> x
                                f
                                $.test.eval/result))))))



(defn data-quotable

  "Like [[data]] but ensures that quoting the generated value does not change anything in the result."

  [schema]

  (check schema
         (fn [x]
           ($.test.util/eq x
                           ($.test.eval/result x)
                           ($.test.eval/result ($.form/quoted x))))))



(defn data-quoted

  "Like [[data]] but quotes the generated values.
  
   Useful for preventing any symbol from being evaled."

  [schema]

  (data schema
        $.form/quoted))



(defn like-clojure

  "Checks if calling `form` on the CVM with the generated value(s) produces the exact same result
   as in Clojure by using `f-clojure`."

  [form f-clojure schema]

  (check schema
         (fn [x]
           ($.test.util/eq (apply f-clojure
                                  x)
                           ($.test.eval/result (list* form
                                                      x))))))



(defn pred-data

  "Used by [[pred-data-false]] and [[pred-data-true]]."


  ([form result? schema]

   (pred-data form
              result?
              nil
              schema))


  ([form result? f-clojure schema]

   (check schema
          (let [suite   (fn [_x x-2]
                          [["Always returns false"
                            #(result? x-2)]])
                suite-2 (if f-clojure
                          (fn [x x-2]
                            (conj (suite x
                                         x-2)
                                  ["Consistent with Clojure"
                                   #(= x-2
                                       (f-clojure x))]))
                          suite)]

            (fn [x]
              (mult (suite-2 x
                             ($.test.eval/result (list form
                                                       ($.form/quoted x))))))))))



(defn pred-data-false

  "Like [[pred-data-true]] but tests for negative results.
  
   Provided schema is a set of data types meant to be removed from `:convex/data`."


  ([form schema-without]

   (pred-data-false form
                    nil
                    schema-without))


  ([form f-clojure schema-without]

   (pred-data form
              false?
              f-clojure
              ($.test.schema/data-without schema-without))))



(defn pred-data-true

  "Tests if a value generated by ´schema` passes a data predicate on the CVM.
  
   If `f-clojure` is given, also ensures that the very same value produces the exact same result
   in Clojure."


  ([form schema]

   (pred-data-true form
                   nil
                   schema))


  ([form f-clojure schema]

   (pred-data form
              true?
              f-clojure
              schema)))
