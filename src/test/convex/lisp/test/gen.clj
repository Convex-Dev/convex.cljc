(ns convex.lisp.test.gen

  "Generators for test purposes."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]))


;;;;;;;;;;


(defn kv+

  "Vector of key-values."

  [gen-k gen-v]

  (TC.gen/fmap (fn [[k+ v+]]
                 (mapv vec
                       (partition 2
                                  (interleave k+
                                              v+))))
               (TC.gen/bind (TC.gen/vector-distinct gen-k)
                            (fn [k+]
                              (TC.gen/tuple (TC.gen/return k+)
                                            (TC.gen/vector gen-v
                                                           (count k+)))))))


(def percent

  "Value between 0 and 1 (inclusive)."

  (TC.gen/double* {:max 1
                   :min 1}))


;;;;;;;;;;


(comment

  (TC.gen/generate (kv+ TC.gen/large-integer
                        TC.gen/boolean))
  )
