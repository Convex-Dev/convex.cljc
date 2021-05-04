(ns convex.lisp.test.prop

  "Building common `test.check` properties."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as tc.prop]
            [convex.lisp                   :as $]
            [convex.lisp.test.util         :as $.test.util])
  (:refer-clojure :exclude [cast]))


(declare like-clojure)


;;;;;;;;;;


(defn arithmetic

  ""

  [core-symbol]

  (tc.prop/for-all* [($.test.util/generator [:vector
                                             {:min 1}
                                             :convex/long])]
                    (fn [x]
                      ($.test.util/prop+

                        "Numerical computation of longs must result in a long"
                        (int? ($.test.util/eval (list* core-symbol
                                                       x)))

                        "Numerical computation with at least one double must result in a double"
                        (double? ($.test.util/eval (list* core-symbol
                                                          (update x
                                                                  (rand-int (dec (count x)))
                                                                  double))))))))



(defn cast

  ""


  ([sym-core-cast sym-core-pred clojure-pred schema]

   (cast sym-core-cast
         sym-core-pred
         schema
         nil
         clojure-pred))


  ([sym-core-cast sym-core-pred clojure-cast clojure-pred schema]

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
                                cast?] ($.test.util/eval ($/templ {'?sym-cast sym-core-cast
                                                                   '?sym-pred sym-core-pred
                                                                   '?x        x}
                                                                  '(let [x-2 (?sym-cast (quote ?x))]
                                                                     [x-2
                                                                      (?sym-pred x-2)])))]
                           (suite-2 x
                                    x-2
                                    cast?)))))))



(defn comparison

  ""

  [sym-convex f-clojure]

  (like-clojure sym-convex
                f-clojure
                [:vector
                 {:min 1}
                 :convex/number]))



(defn like-clojure

  ""

  [sym-convex f-clojure schema]

  (tc.prop/for-all* [($.test.util/generator schema)]
                    (fn [x]
                      ($.test.util/eq (apply f-clojure
                                             x)
                                      ($.test.util/eval (list* sym-convex
                                                               x))))))



(defn pred-data-false

  ""


  ([core-symbol schema-without]

   (pred-data-false core-symbol
                    nil
                    schema-without))


  ([core-symbol clojure-pred schema-without]

   (tc.prop/for-all* [($.test.util/generator-data-without schema-without)]
                     (if clojure-pred
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred core-symbol
                                                          x)]
                           ($.test.util/prop+

                             "Always returns false"
                             (not x-2)

                             "Consistent with Clojure"
                             (= x-2
                                (clojure-pred x)))))
                       (fn [x]
                         (not ($.test.util/eval-pred core-symbol
                                                     x)))))))



(defn pred-data-true

  ""


  ([core-symbol schema]

   (pred-data-true core-symbol
                   nil
                   schema))


  ([core-symbol clojure-pred schema]

   (tc.prop/for-all* [($.test.util/generator schema)]
                     (if clojure-pred
                       (fn [x]
                         (let [x-2 ($.test.util/eval-pred core-symbol
                                                          x)]
                           ($.test.util/prop+

                             "Always returns true"
                             x-2

                             "Consistent with Clojure"
                             (= x-2
                                (clojure-pred x)))))
                       (fn [x]
                         ($.test.util/eval-pred core-symbol
                                                x))))))


