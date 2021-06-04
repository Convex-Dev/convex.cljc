(ns convex.break.prop

  "Utilities for building property based tests."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.clojure-test :as TC.ct]
            [clojure.test.check.results      :as TC.result]))


(declare fail)


;;;;;;;;;; Defining generative tests


(let [get-env (fn [k default]
                (if-some [x (not-empty (System/getenv k))]
                  (try
                    (Long/parseLong x)
                    (catch Throwable e
                      (throw (ex-info (str "While parsing env variable: "
                                           k)
                                      {::env-var k}
                                      e))))
                  default))]

  (def max-size

    "Maximum size used by [[deftest]]. Can be set using the \"BREAK_MAX_SIZE\" env variable."

    (get-env "BREAK_MAX_SIZE"
             200))



  (def n-test
  
    "Number of tests used by [[deftest]]. Can be set using the \"BREAK_N_TEST\" env variable."

    (get-env "BREAK_N_TEST"
             100)))




(defmacro deftest

  "Like `clojure.test.check.clojure-test/defspec`.

   Difference is that the number of tests and maximum size can be easily configured
   at the level of the whole test suite and also by providing metadata:

   For altering those default values, see [[max-size]] and [[n-test]].

   


   ```clojure
   (deftest some-test

     {:ratio-n
     ...)
   ```"

  ([sym prop]

   `(deftest ~sym
             nil
             ~prop))


  ([sym option+ prop]

   `(TC.ct/defspec ~sym
                  ~(-> option+
                       (update :max-size
                               #(or %
                                    (* max-size
                                       (or (:ratio-size option+)
                                           1))))
                       (update :num-tests
                               #(or %
                                    (* n-test
                                       (or (:ratio-count option+)
                                           1)))))
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
         (satisfies? TC.result/Result
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

  "Returns a `test.check` error with `checkpoint` conjed to `:convex.test/error`
   of the `test.check` error data (see `clojure.test.check.results` namespace)."


  ([checkpoint]

   (reify TC.result/Result

     (pass? [_]
       false)

     (result-data [_]
       {:convex.test/error [checkpoint]})))


  ([failure checkpoint]

   (let [result (update (TC.result/result-data failure)
                        :convex.test/error
                        (partial into
                                 [checkpoint]))]
     (reify TC.result/Result

       (pass? [_]
         false)

       (result-data [_]
         result)))))



(defn mult

  "Meant to be used inside a `test.check` property in order to multiplex it while keeping
   track of which \"sub-property\" failed.
  
   Tests each pair of checkpoint and function. Fails with [[faill]] and the message when
   a predicate returns false.

   A checkpoint could be anything. Most commonly a human readable string. See [[checkpoint]].

   Composes with itself. A function could be another call to [[mult]] and in case of failure,
   all checkpoints leading to it figure under `:convex.test/error` (see [[fail]]).

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
