(ns convex.lisp.test.prop

  "Utilities for building property based tests."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.clojure-test :as tc.ct]
            [clojure.test.check.results      :as tc.result]))


(declare fail)


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
                   ~(merge (let [-meta (meta sym)]
                             (when-some [x (cond
                                             (:fuzz -meta)  2
                                             (:recur -meta) 5)]
                               {:max-size x}))
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

  "Returns a `test.check` error with `checkpoint` conjed to `:convex.test/error`
   of the `test.check` error data (see `clojure.test.check.results` namespace)."


  ([checkpoint]

   (reify tc.result/Result

     (pass? [_]
       false)

     (result-data [_]
       {:convex.test/error [checkpoint]})))


  ([failure checkpoint]

   (let [result (update (tc.result/result-data failure)
                        :convex.test/error
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
