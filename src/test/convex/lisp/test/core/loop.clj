(ns convex.lisp.test.core.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Helpers


(defn- -recur

  "Used by [[recur--]] for generating nested `loop` forms which ensures that, in `recur`:
  
   - A counter is properly incremented
   - A fixed set of bindings do not mutate

   Points of recursions must be respected. Sometimes, a loop is wrapped in an additional `fn`
   for messing with those.
  
   Otherwise, execution stops by calling `fail`."

  [[[sym limit fixed fn-wrap?] & limit+]]

  (let [ret ($.form/templ {'?binding+    (conj (reduce (fn [acc [sym x]]
                                                         (conj acc
                                                               sym
                                                               ($.form/quoted x)))
                                                       []
                                                       fixed)
                                               sym
                                               0)
                           '?fixed-sym+  (mapv first
                                               fixed)
                           '?fixed-x+    (mapv (comp $.form/quoted
                                                     second)
                                               fixed)
                           '?limit       limit
                           '?recur-case  (let [recur-form (list* 'recur
                                                                 (conj (mapv first
                                                                             fixed)
                                                                       (list 'inc
                                                                             sym)))]
                                           (if limit+
                                             ($.form/templ {'?limit-inner (-> limit+
                                                                              first
                                                                              second)
                                                            '?loop-inner  (-recur limit+)
                                                            '?recur-form  recur-form}
                                                           '(if (= ?limit-inner
                                                                   ?loop-inner)
                                                              ?recur-form
                                                              (fail :BAD-ITER
                                                                    "Iteration count of inner loop is wrong")))
                                             recur-form))
                           '?sym         sym}
                          '(loop ?binding+
                             (if (= ?sym
                                    ?limit)
                               (if (= ?fixed-sym+
                                      ?fixed-x+)
                                 ?limit
                                 (fail :NOT-FIXED
                                       "Fixed bindings were wrongfully modified"))
                               ?recur-case)))]
    (if fn-wrap?
      ($.form/templ {'?loop ret}
                    '((fn [] ?loop)))
      ret)))


;;;;;;;;;; Tests


($.test.prop/deftest ^:recur recur--

  ($.test.prop/check [:vector
                      {:max 5
                       :min 1}
                      [:tuple
                       [:and
                        :convex/symbol
                        [:not [:enum
                               '+
                               '=
                               'fail
                               'inc
                               'recur]]]
                       [:int
                        {:max 5
                         :min 0}]
                       ($.test.schema/binding+ 0)
                       :boolean]]
                     (fn [limit+]
                       (= (-> limit+
                              first
                              second)
                          ($.test.eval/result (-recur limit+))))))



($.test.prop/deftest ^:recur reduce--

  ($.test.prop/check [:and
                      ;; TODO. Should be all collections but fails because of sets: https://github.com/Convex-Dev/convex/issues/109
                      ;:convex/collection
                      :convex/vector
                      [:fn #(pos? (count %))]]
                     (fn [x]
                       ($.test.eval/result ($.form/templ {'?i (rand-int (count x))
                                                          '?x x}
                                                         '(let [x '?x
                                                                v (nth x
                                                                       '?i)]
                                                            (= v
                                                               (reduce (fn [acc item]
                                                                         (if (= item
                                                                                v)
                                                                           (reduced item)
                                                                           acc))
                                                                       :convex-sentinel
                                                                       x))))))))
