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
                                                 (reduce (fn [acc v]
                                                           (disj acc
                                                                 v))
                                                         s
                                                         s)))
                           )))))

;;;;;;;;;;


; difference
; disj
; intersection
; subset?
; union
