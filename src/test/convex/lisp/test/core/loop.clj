(ns convex.lisp.test.core.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;; Helpers


(defn- -recur

  "Used by [[recur--]] for generating nested `loop` and `fn` forms which ensures that, in `recur`:
  
   - A counter is properly incremented, looping does occur
   - A fixed set of bindings do not mutate
   - Possibly nested points of recurion are respected

   Sometimes, loops are wrapped in a no-arg function just for messing with the recursion point.
  
   Otherwise, execution stops by calling `fail`."

  [[{:keys [fixed+
            fn-wrap?
            n
            recur-point
            sym]}
    & looping+]]

  (let [fixed-sym+ (mapv first
                         fixed+)
        fixed-x+   (mapv (comp $.form/quoted
                               second)
                         fixed+)
        body       ($.form/templ {'?fixed-sym+  fixed-sym+
                                  '?fixed-x+    fixed-x+
                                  '?n           n
                                  '?recur-case  (let [recur-form (list* 'recur
                                                                        (conj fixed-sym+
                                                                              (list 'inc
                                                                                    sym)))]
                                                  (if looping+
                                                    ($.form/templ {'?n-inner       (-> looping+
                                                                                       first
                                                                                       :n)
                                                                   '?looping-inner (-recur looping+)
                                                                   '?recur-form    recur-form}
                                                                  '(if (= ?n-inner
                                                                          ?looping-inner)
                                                                     ?recur-form
                                                                     (fail :BAD-ITER
                                                                           "Iteration count of inner loop is wrong")))
                                                    recur-form))
                                  '?sym         sym}
                                 '(if (= ?sym
                                         ?n)
                                    (if (= ?fixed-sym+
                                           ?fixed-x+)
                                      ?n
                                      (fail :NOT-FIXED
                                            "Fixed bindings were wrongfully modified"))
                                    ?recur-case))
        looping   (case recur-point
                    :fn   (list* (list 'fn
                                       (conj fixed-sym+
                                             sym)
                                       body)
                                 (conj fixed-x+
                                       0))
                    :loop (list 'loop
                                (conj (reduce (fn [acc [sym x]]
                                                (conj acc
                                                      sym
                                                      ($.form/quoted x)))
                                              []
                                              fixed+)
                                      sym
                                      0)
                                body))]
    (if fn-wrap?
      (list (list 'fn
                  []
                  looping))
      looping)))


;;;;;;;;;; Tests


;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/106#issuecomment-841406937
;;
#_($.test.prop/deftest dotimes--

  ($.test.prop/check [:and
                      [:tuple
                       :convex/symbol
                       :convex/symbol
                       :convex/number]
                      [:fn (fn [[bind counter n]]
                             (and (not= bind
                                        counter)
                                  (>= n
                                      0)))]]
                     (fn [[bind counter n]]
                       (println :form 
                       ($.test.eval/log ($.form/templ {'?bind    bind
                                                          '?counter counter
                                                          '?n       n}
                                                         '(let [?counter 0]
                                                            (dotimes [?bind ?n]
                                                              (set! ?counter
                                                                    (+ ?counter
                                                                       1)))
                                                            (log :got ?counter (floor ?n))
                                                            (== ?counter
                                                                (floor ?n)))))))))



($.test.prop/deftest ^:recur recur--

  ($.test.prop/check [:vector
                      {:max 5
                       :min 1}
                      [:map
                       [:fixed+      ($.test.schema/binding+ 0)]
                       [:fn-wrap?    :boolean]
                       [:n           [:int
                                      {:max 5
                                       :min 0}]]
                       [:recur-point [:enum
                                      :fn
                                      :loop]]
                       [:sym         [:and
                                      :convex/symbol
                                      [:not [:enum
                                             '+
                                             '=
                                             'fail
                                             'inc
                                             'recur]]]]]]
                     (fn [looping+]
                       (= (-> looping+
                              first
                              :n)
                          ($.test.eval/result (-recur looping+))))))



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
