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

  (let [;; Symbols for binding that do not change during recursion
        fixed-sym+ (mapv first
                         fixed+)
        ;; And associated values
        fixed-x+   (mapv (comp $.form/quoted
                               second)
                         fixed+)
        ;; Code that will be in the (loop [] ...) or ((fn [] ...)) form
        body       ($.form/templ* (if (= ~sym
                                         ~n)
                                    (if (= ~fixed-sym+
                                           ~fixed-x+)
                                      ~n
                                      (fail :NOT-FIXED
                                            "Fixed bindings were wrongfully modified"))
                                    ~(let [recur-form ($.form/templ* (recur ~@fixed-sym+
                                                                            (inc ~sym)))]
                                       (if looping+
                                         ($.form/templ* (if (= ~(-> looping+
                                                                    first
                                                                    :n)
                                                               ~(-recur looping+))
                                                          ~recur-form
                                                          (fail :BAD-ITER
                                                                "Iteration count of inner loop is wrong")))
                                         recur-form))))
        ;; Wrapping body in a loop or a fn form
        looping   (case recur-point
                    :fn   ($.form/templ* ((fn ~(conj fixed-sym+
                                                     sym)
                                              ~body)
                                          ~@(conj fixed-x+
                                                  0)))
                    :loop ($.form/templ* (loop ~(conj (reduce (fn [acc [sym x]]
                                                                (conj acc
                                                                      sym
                                                                      ($.form/quoted x)))
                                                              []
                                                              fixed+)
                                                      sym
                                                      0)
                                           ~body)))]
    ;; Messing with point of recursion by wrapping in a fn that is immedialy called
    (if fn-wrap?
      ($.form/templ* ((fn [] ~looping)))
      looping)))


;;;;;;;;;; Tests


;; TODO. Fail, not core symbols
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
                       ($.test.eval/result* (do
                                              (def ~counter
                                                   0)
                                              (dotimes [~bind ~n]
                                                (def ~counter
                                                     (+ ~counter
                                                        1)))
                                              (== ~counter
                                                  (floor ~n)))))))



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
                       ($.test.eval/result* (let [x '~x
                                                  v (nth x
                                                         ~(rand-int (count x)))]
                                              (= v
                                                 (reduce (fn [acc item]
                                                           (if (= item
                                                                  v)
                                                             (reduced item)
                                                             acc))
                                                         :convex-sentinel
                                                         x)))))))
