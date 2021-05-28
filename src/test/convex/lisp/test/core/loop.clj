(ns convex.lisp.test.core.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


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
        fixed-x+   (mapv (comp $.lisp/quoted
                               second)
                         fixed+)
        ;; Code that will be in the (loop [] ...) or ((fn [] ...)) form
        body       ($.lisp/templ* (if (= ~sym
                                         ~n)
                                    (if (= ~fixed-sym+
                                           ~fixed-x+)
                                      ~n
                                      (fail :NOT-FIXED
                                            "Fixed bindings were wrongfully modified"))
                                    ~(let [recur-form ($.lisp/templ* (recur ~@fixed-sym+
                                                                            (inc ~sym)))]
                                       (if looping+
                                         ($.lisp/templ* (if (= ~(-> looping+
                                                                    first
                                                                    :n)
                                                               ~(-recur looping+))
                                                          ~recur-form
                                                          (fail :BAD-ITER
                                                                "Iteration count of inner loop is wrong")))
                                         recur-form))))
        ;; Wrapping body in a loop or a fn form
        looping   (case recur-point
                    :fn   ($.lisp/templ* ((fn ~(conj fixed-sym+
                                                     sym)
                                              ~body)
                                          ~@(conj fixed-x+
                                                  0)))
                    :loop ($.lisp/templ* (loop ~(conj (reduce (fn [acc [sym x]]
                                                                (conj acc
                                                                      sym
                                                                      ($.lisp/quoted x)))
                                                              []
                                                              fixed+)
                                                      sym
                                                      0)
                                           ~body)))]
    ;; Messing with point of recursion by wrapping in a fn that is immedialy called
    (if fn-wrap?
      ($.lisp/templ* ((fn [] ~looping)))
      looping)))


;;;;;;;;;; Tests


($.test.prop/deftest dotimes--

  (TC.prop/for-all [n             (TC.gen/double* {:infinite? false
                                                   :max       1e3
                                                   :min       0
                                                   :NaN?      false})
                    [sym-bind
                     sym-counter] (TC.gen/vector-distinct $.gen/symbol
                                                          {:num-elements 2})]
    ($.test.eval/result* (do
                           (def ~sym-counter
                                0)
                           (dotimes [~sym-bind ~n]
                             (def ~sym-counter
                                  (+ ~sym-counter
                                     1)))
                           (== ~sym-counter
                               (floor ~n))))))



($.test.prop/deftest recur--

  (TC.prop/for-all [looping+ (TC.gen/vector (TC.gen/hash-map :fixed+      ($.gen/binding+ 0
                                                                                          4)
                                                             :fn-wrap?    $.gen/boolean
                                                             :n           (TC.gen/choose 0
                                                                                         5)
                                                             :recur-point (TC.gen/elements [:fn
                                                                                            :loop])
                                                             :sym         (TC.gen/such-that #(not (#{'+
                                                                                                     '=
                                                                                                     'fail
                                                                                                     'inc
                                                                                                     'recur}
                                                                                                    %))
                                                                                            $.gen/symbol))
                                            1
                                            5)]
    (= (-> looping+
           first
           :n)
       ($.test.eval/result (-recur looping+)))))



($.test.prop/deftest reduce--

  (TC.prop/for-all [x (TC.gen/such-that #(not-empty (cond->
                                                      %
                                                      (seq? %)
                                                      rest))
                                        $.gen/collection)]

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
                                      x))))))
