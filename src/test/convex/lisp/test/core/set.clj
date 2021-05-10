(ns convex.lisp.test.core.set

  "Testing set operations."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur mono

  ($.test.prop/check :convex/set
                     (fn [x]
                       (let [ctx ($.test.eval/form->ctx ($.form/templ {'?s x}
                                                                      '(def s
                                                                            '?s)))]
                         ($.test.prop/mult*

                           "There is no difference between a set and itself"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (difference s
                                                             s)))

                           "The intersection of a set with itself is the set itself"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (intersection s
                                                               s)))

                           "A set is a subset of itself"
                           ($.test.eval/form ctx
                                             '(subset? s
                                                       s))

                           "The union of a set with itself is the set itself"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (union s
                                                        s)))

                           "`empty`"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (empty s)))

                           "Rebuilding set using `into` on empty set"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (into (empty s)
                                                       s)))

                           "Rebuilding set using `into` on the set itself"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (into s
                                                       s)))

                           "Rebuilding set from list using `into`"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (into (empty s)
                                                       (into (list)
                                                             s))))

                           "Rebuilding set by applying it to `conj`"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (apply conj
                                                        #{}
                                                        (vec s))))

                           "Adding all values of set into that same set does not change anything"
                           ($.test.eval/form ctx
                                             '(= s
                                                 (reduce conj
                                                         s
                                                         s)))

                           "A set contains each of its values"
                           ($.test.eval/form ctx
                                             '($/every? (fn [v]
                                                          (contains-key? s
                                                                         v))
                                                        s))

                           "`disj` consistent with `contains-key?`"
                           ($.test.eval/form ctx
                                             '($/every? (fn [v]
                                                          (not (contains-key? (disj s
                                                                                    v)
                                                                              v)))
                                                        s))

                           "`disj` all values returns an empty set"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (reduce disj
                                                         s
                                                         s))))))))



($.test.prop/deftest ^:recur poly

  ($.test.prop/check [:vector
                      {:min 2}
                      [:or
                       :convex/nil
                       :convex/set]]
                     (fn [x]
                       (let [ctx ($.test.eval/form->ctx ($.form/templ {'?s+ x}
                                                                      '(do
                                                                         (def s+
                                                                              '?s+)
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
                         ($.test.prop/mult*

                           "Difference is a set"
                           ($.test.eval/form ctx
                                             '(set? -difference))

                           "Intersection is a set"
                           ($.test.eval/form ctx
                                             '(set? -intersection))

                           "Union is a set"
                           ($.test.eval/form ctx
                                             '(set? -union))

                           "Difference cannot be bigger than first set"
                           ($.test.eval/form ctx
                                             '(<= n-difference
                                                  (first n-s+)))

                           "Difference cannot be bigger than union"
                           ($.test.eval/form ctx
                                             '(<= n-difference
                                                  n-union))

                           "Intersection cannot be bigger th first set"
                           ($.test.eval/form ctx
                                             '(<= n-intersection
                                                  (first n-s+)))

                           "Intersection cannot be bigger than union"
                           ($.test.eval/form ctx
                                             '(<= n-intersection
                                                  n-union))

                           "All sets are <= than union"
                           ($.test.eval/form ctx
                                             '($/every? (fn [s]
                                                          (<= (count s)
                                                              n-union))
                                                        s+))

                           "Difference is a subset of first set"
                           ($.test.eval/form ctx
                                             '(let [s (first s+)]
                                                (and (subset? -difference
                                                              s)
                                                     ($/every? (fn [v]
                                                                 (contains-key? s
                                                                                v))
                                                               -difference))))

                           "Non-empty difference is not a subset of sets other than first one"
                           ($.test.eval/form ctx
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
                           ($.test.eval/form ctx
                                             '($/every? (fn [s]
                                                          (and (subset? -intersection
                                                                        s)
                                                               ($/every? (fn [v]
                                                                           (contains-key? s
                                                                                          v))
                                                                         -intersection)))
                                                        s+))

                           "All sets are subsets of union"
                           ($.test.eval/form ctx
                                             '($/every? (fn [s]
                                                          (and (subset? s
                                                                        -union)
                                                               ($/every? (fn [v]
                                                                           (contains-key? -union
                                                                                          v))
                                                                         s)))
                                                        s+))

                           "Emulating union with `into`"
                           ($.test.eval/form ctx
                                             '(= -union
                                                 (reduce into
                                                         #{}
                                                         s+)))

                           "Removing difference items from first set removes the difference"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (apply difference
                                                        (cons (reduce disj
                                                                      (first s+)
                                                                      -difference)
                                                              (next s+)))))

                           "Removing intersection items from first set removes the intersection"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (apply intersection
                                                        (cons (reduce disj
                                                                      (first s+)
                                                                      -intersection)
                                                              (next s+)))))

                           "Difference and intersection have nothing in common"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (intersection -difference
                                                               -intersection)
                                                 (intersection -intersection
                                                               -difference)))

                           "Difference between difference and intersection is difference"
                           ($.test.eval/form ctx
                                             '(= (difference -difference
                                                             -intersection)
                                                 -difference))

                           "Difference between intersection and difference is intersection"
                           ($.test.eval/form ctx
                                             '(= (difference -intersection
                                                             -difference)
                                                 -intersection))

                           "No difference between difference and union"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (difference -difference
                                                             -union)))

                           "No difference between intersection and union"
                           ($.test.eval/form ctx
                                             '(= #{}
                                                 (difference -intersection
                                                             -union)))

                           "Intersection between difference and intersection is a subset of first set"
                           ($.test.eval/form ctx
                                             '(subset? (intersection -difference
                                                                     -intersection)
                                                       (first s+)))

                           "Intersection between difference and union is difference"
                           ($.test.eval/form ctx
                                             '(= -difference
                                                 (intersection -difference
                                                               -union)))

                           "Intersection between intersection and union is intersection"
                           ($.test.eval/form ctx
                                             '(= -intersection
                                                 (intersection -intersection
                                                               -union)))

                           "Union between difference and intersection is a subset of first set"
                           ($.test.eval/form ctx
                                             '(subset? (union -difference
                                                              -intersection)
                                                       (first s+)))

                           "Union between difference and union is union"
                           ($.test.eval/form ctx
                                             '(= -union
                                                 (union -difference
                                                        -union)))

                           "Union between intersection and union is union"
                           ($.test.eval/form ctx
                                             '(= -union
                                                 (union -intersection
                                                        -union)))

                           "Order of arguments does not matter in `union`"
                           ($.test.eval/form ctx
                                             '(= -union
                                                 (apply union
                                                        (into (list)
                                                              s+)))))))))
