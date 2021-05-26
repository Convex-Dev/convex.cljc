(ns convex.lisp.test.core.set

  "Testing set operations."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest mono

  ;; Using only one set.

  (TC.prop/for-all [s $.gen/set]
    (let [ctx ($.test.eval/ctx* (def s
                                     ~s))]
      ($.test.prop/mult*

        "There is no difference between a set and itself"
        ($.test.eval/result ctx
                            '(= #{}
                                (difference s
                                            s)))

        "The intersection of a set with itself is the set itself"
        ($.test.eval/result ctx
                            '(= s
                                (intersection s
                                              s)))

        "A set is a subset of itself"
        ($.test.eval/result ctx
                            '(subset? s
                                      s))

        "The union of a set with itself is the set itself"
        ($.test.eval/result ctx
                            '(= s
                                (union s
                                       s)))

        "`empty`"
        ($.test.eval/result ctx
                            '(= #{}
                                (empty s)))

        "Rebuilding set using `into` on empty set"
        ($.test.eval/result ctx
                            '(= s
                                (into (empty s)
                                      s)))

        "Rebuilding set using `into` on the set itself"
        ($.test.eval/result ctx
                            '(= s
                                (into s
                                      s)))

        "Rebuilding set from list using `into`"
        ($.test.eval/result ctx
                            '(= s
                                (into (empty s)
                                      (into (list)
                                            s))))

        "Rebuilding set by applying it to `conj`"
        ($.test.eval/result ctx
                            '(= s
                                (apply conj
                                       #{}
                                       (vec s))))

        "Adding all values of set into that same set does not change anything"
        ($.test.eval/result ctx
                            '(= s
                                (reduce conj
                                        s
                                        s)))

        "A set contains each of its values"
        ($.test.eval/result ctx
                            '($/every? (fn [v]
                                         (contains-key? s
                                                        v))
                                       s))

        "`disj` consistent with `contains-key?`"
        ($.test.eval/result ctx
                            '($/every? (fn [v]
                                         (not (contains-key? (disj s
                                                                   v)
                                                             v)))
                                       s))

        "`disj` all values returns an empty set"
        ($.test.eval/result ctx
                            '(= #{}
                                (reduce disj
                                        s
                                        s)))))))



#_($.test.prop/deftest poly

  ;; Using at least 2 sets.

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/155

  (TC.prop/for-all [s+ (TC.gen/vector $.test.gen/maybe-set
                                      2
                                      8)]
    (let [ctx ($.test.eval/ctx* (do
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
                                       (count -union))))]
      ($.test.prop/mult*

        "Difference is a set"
        ($.test.eval/result ctx
                            '(set? -difference))

        "Intersection is a set"
        ($.test.eval/result ctx
                            '(set? -intersection))

        "Union is a set"
        ($.test.eval/result ctx
                            '(set? -union))

        "Difference cannot be bigger than first set"
        ($.test.eval/result ctx
                            '(<= n-difference
                                 (first n-s+)))

        "Difference cannot be bigger than union"
        ($.test.eval/result ctx
                            '(<= n-difference
                                 n-union))

        "Intersection cannot be bigger th first set"
        ($.test.eval/result ctx
                            '(<= n-intersection
                                 (first n-s+)))

        "Intersection cannot be bigger than union"
        ($.test.eval/result ctx
                            '(<= n-intersection
                                 n-union))

        "All sets are <= than union"
        ($.test.eval/result ctx
                            '($/every? (fn [s]
                                         (<= (count s)
                                             n-union))
                                       s+))

        "Difference is a subset of first set"
        ($.test.eval/result ctx
                            '(let [s (first s+)]
                               (and (subset? -difference
                                             s)
                                    ($/every? (fn [v]
                                                (contains-key? s
                                                               v))
                                              -difference))))

        "Non-empty difference is not a subset of sets other than first one"
        ($.test.eval/result ctx
                            '(if (empty? -difference)
                               true
                               ($/every? (fn [s]
                                           (and (not (subset? -difference
                                                              s))
                                                ($/every? (fn [v]
                                                            (not (contains-key? s
                                                                                v)))
                                                          -difference)))
                                         (next s+))))

        "Intersection is a subset of all sets"
        ($.test.eval/result ctx
                            '($/every? (fn [s]
                                         (and (subset? -intersection
                                                       s)
                                              ($/every? (fn [v]
                                                          (contains-key? s
                                                                         v))
                                                        -intersection)))
                                       s+))

        "All sets are subsets of union"
        ($.test.eval/result ctx
                            '($/every? (fn [s]
                                         (and (subset? s
                                                       -union)
                                              ($/every? (fn [v]
                                                          (contains-key? -union
                                                                         v))
                                                        s)))
                                       s+))

        "Emulating union with `into`"
        ($.test.eval/result ctx
                            '(= -union
                                (reduce into
                                        #{}
                                        s+)))

        "Removing difference items from first set removes the difference"
        ($.test.eval/result ctx
                            '(= #{}
                                (apply difference
                                       (cons (reduce disj
                                                     (first s+)
                                                     -difference)
                                             (next s+)))))

        "Removing intersection items from first set removes the intersection"
        ($.test.eval/result ctx
                          '(= #{}
                              (apply intersection
                                     (cons (reduce disj
                                                   (first s+)
                                                   -intersection)
                                           (next s+)))))

        "Difference and intersection have nothing in common"
        ($.test.eval/result ctx
                          '(= #{}
                              (intersection -difference
                                            -intersection)
                              (intersection -intersection
                                            -difference)))

        "Difference between difference and intersection is difference"
        ($.test.eval/result ctx
                            '(= (difference -difference
                                            -intersection)
                                -difference))

        "Difference between intersection and difference is intersection"
        ($.test.eval/result ctx
                            '(= (difference -intersection
                                            -difference)
                                -intersection))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/155
        ;;
        ;; "No difference between difference and union"
        ;; ($.test.eval/result ctx
        ;;                     '(= #{}
        ;;                         (difference -difference
        ;;                                     -union)))

        "No difference between intersection and union"
        ($.test.eval/result ctx
                            '(= #{}
                                (difference -intersection
                                            -union)))

        "Intersection between difference and intersection is a subset of first set"
        ($.test.eval/result ctx
                            '(subset? (intersection -difference
                                                    -intersection)
                                      (first s+)))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/155
        ;;
        ;; "Intersection between difference and union is difference"
        ;; ($.test.eval/result ctx
        ;;                     '(= -difference
        ;;                         (intersection -difference
        ;;                                       -union)))

        "Intersection between intersection and union is intersection"
        ($.test.eval/result ctx
                            '(= -intersection
                                (intersection -intersection
                                              -union)))

        "Union between difference and intersection is a subset of first set"
        ($.test.eval/result ctx
                            '(subset? (union -difference
                                             -intersection)
                                      (first s+)))

        ;; "Union between difference and union is union"
        ;; ($.test.eval/result ctx
        ;;                     '(= -union
        ;;                         (union -difference
        ;;                                -union)))

        "Union between intersection and union is union"
        ($.test.eval/result ctx
                            '(= -union
                                (union -intersection
                                       -union)))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/153 
        ;;
        ;; "Order of arguments does not matter in `union`"
        ;; ($.test.eval/result ctx
        ;;                     '(= -union
        ;;                         (apply union
        ;;                                (into (list)
        ;;                                      s+))))
        ))))
