(ns convex.test.break.set

  "Testing set operations."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest mono

  ;; Using only one set.

  {:ratio-num 10}

  (TC.prop/for-all [s ($.gen/quoted $.gen/any-set)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (def s
                                         ~s)))]
      (mprop/mult

        "There is no difference between a set and itself"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (difference s
                                               s))))


        "The intersection of a set with itself is the set itself"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (intersection s
                                                 s))))


        "A set is a subset of itself"

        ($.eval/true? ctx
                      ($.cell/* (subset? s
                                         s)))


        "The union of a set with itself is the set itself"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (union s
                                          s))))


        "`empty`"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (empty s))))


        "Rebuilding set using `into` on empty set"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (into (empty s)
                                         s))))


        "Rebuilding set using `into` on the set itself"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (into s
                                         s))))


        "Rebuilding set from list using `into`"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (into (empty s)
                                         (into (list)
                                                 s)))))


        "Rebuilding set by applying it to `conj`"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (apply conj
                                          #{}
                                            (vec s)))))


        "Adding all values of set into that same set does not change anything"

        ($.eval/true? ctx
                      ($.cell/* (= s
                                   (reduce conj
                                           s
                                           s))))


        "A set contains each of its values"

        ($.eval/true? ctx
                      ($.cell/* ($/every? (fn [v]
                                            (contains-key? s
                                                           v))
                                          s)))


        "`disj` consistent with `contains-key?`"

        ($.eval/true? ctx
                      ($.cell/* ($/every? (fn [v]
                                            (not (contains-key? (disj s
                                                                      v)
                                                                v)))
                                          s)))


        "`disj` all values returns an empty set"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (reduce disj
                                           s
                                           s))))))))



(mprop/deftest poly

  ;; Using at least 2 sets.

  {:ratio-num 10}

  (TC.prop/for-all [s+ ($.gen/vector (TC.gen/one-of [$.gen/nothing
                                                     ($.gen/quoted $.gen/any-set)])
                                     2
                                     8)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def s+
                                           ~s+)
                                      (def -difference
                                           (apply difference
                                                  s+))
                                      (def -intersection
                                           (apply intersection
                                                  s+))
                                      (def -union
                                           (apply union
                                                  s+))
                                      (def n-difference
                                           (count -difference))
                                      (def n-intersection
                                           (count -intersection))
                                      (def n-s+
                                           (mapv count
                                                 s+))
                                      (def n-union
                                           (count -union)))))]
      (mprop/mult

        "Difference is a set"

        ($.eval/true? ctx
                      ($.cell/* (set? -difference)))


        "Intersection is a set"
        ($.eval/true? ctx
                      ($.cell/* (set? -intersection)))


        "Union is a set"

        ($.eval/true? ctx
                      ($.cell/* (set? -union)))


        "Difference cannot be bigger than first set"

        ($.eval/true? ctx
                      ($.cell/* (<= n-difference
                                    (first n-s+))))


        "Difference cannot be bigger than union"

        ($.eval/true? ctx
                      ($.cell/* (<= n-difference
                                    n-union)))


        "Intersection cannot be bigger th first set"

        ($.eval/true? ctx
                      ($.cell/* (<= n-intersection
                                    (first n-s+))))


        "Intersection cannot be bigger than union"

        ($.eval/true? ctx
                      ($.cell/* (<= n-intersection
                                    n-union)))


        "All sets are <= than union"

        ($.eval/true? ctx
                      ($.cell/* ($/every? (fn [s]
                                            (<= (count s)
                                                n-union))
                                          s+)))


        "Difference is a subset of first set"

        ($.eval/true? ctx
                      ($.cell/* (let [s (first s+)]
                                  (and (subset? -difference
                                                s)
                                       ($/every? (fn [v]
                                                   (contains-key? s
                                                                  v))
                                                 -difference)))))


        "Non-empty difference is not a subset of sets other than first one"

        ($.eval/true? ctx
                      ($.cell/* (if (empty? -difference)
                                  true
                                  ($/every? (fn [s]
                                              (and (not (subset? -difference
                                                                 s))
                                                   ($/every? (fn [v]
                                                               (not (contains-key? s
                                                                                   v)))
                                                             -difference)))
                                            (next s+)))))


        "Intersection is a subset of all sets"

        ($.eval/true? ctx
                      ($.cell/* ($/every? (fn [s]
                                            (and (subset? -intersection
                                                          s)
                                                 ($/every? (fn [v]
                                                             (contains-key? s
                                                                            v))
                                                           -intersection)))
                                          s+)))


        "All sets are subsets of union"

        ($.eval/true? ctx
                      ($.cell/* ($/every? (fn [s]
                                            (and (subset? s
                                                          -union)
                                                 ($/every? (fn [v]
                                                             (contains-key? -union
                                                                            v))
                                                           s)))
                                          s+)))


        "Emulating union with `into`"

        ($.eval/true? ctx
                      ($.cell/* (= -union
                                   (reduce into
                                           #{}
                                           s+))))


        "Removing difference items from first set removes the difference"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (apply difference
                                          (cons (reduce disj
                                                        (first s+)
                                                        -difference)
                                                (next s+))))))


        "Removing intersection items from first set removes the intersection"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (apply intersection
                                          (cons (reduce disj
                                                        (first s+)
                                                        -intersection)
                                                (next s+))))))


        "Difference and intersection have nothing in common"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (intersection -difference
                                                 -intersection)
                                   (intersection -intersection
                                                 -difference))))


        "Difference between difference and intersection is difference"

        ($.eval/true? ctx
                      ($.cell/* (= (difference -difference
                                               -intersection)
                                   -difference)))


        "Difference between intersection and difference is intersection"

        ($.eval/true? ctx
                      ($.cell/* (= (difference -intersection
                                               -difference)
                                   -intersection)))


        "No difference between difference and union"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (difference -difference
                                               -union))))


        "No difference between intersection and union"

        ($.eval/true? ctx
                      ($.cell/* (= #{}
                                   (difference -intersection
                                               -union))))


        "Intersection between difference and intersection is a subset of first set"

        ($.eval/true? ctx
                      ($.cell/* (subset? (intersection -difference
                                                       -intersection)
                                         (first s+))))

        "Intersection between difference and union is difference"

        ($.eval/true? ctx
                      ($.cell/* (= -difference
                                   (intersection -difference
                                                 -union))))


        "Intersection between intersection and union is intersection"

        ($.eval/true? ctx
                      ($.cell/* (= -intersection
                                   (intersection -intersection
                                                 -union))))


        "Union between difference and intersection is a subset of first set"

        ($.eval/true? ctx
                      ($.cell/* (subset? (union -difference
                                                -intersection)
                                         (first s+))))


        "Union between difference and union is union"

        ($.eval/true? ctx
                      ($.cell/* (= -union
                                   (union -difference
                                          -union))))


        "Union between intersection and union is union"

        ($.eval/true? ctx
                      ($.cell/* (= -union
                                   (union -intersection
                                          -union))))


        "Order of arguments does not matter in `union`"

        ($.eval/true? ctx
                      ($.cell/* (= -union
                                   (apply union
                                          (into (list)
                                                s+)))))))))
