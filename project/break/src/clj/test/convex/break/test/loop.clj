(ns convex.break.test.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


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
        fixed-x+   (mapv (comp $.clj/quoted
                               second)
                         fixed+)
        ;; Code that will be in the (loop [] ...) or ((fn [] ...)) form
        body       ($.clj/templ* (if (= ~sym
                                         ~n)
                                    (if (= ~fixed-sym+
                                           ~fixed-x+)
                                      ~n
                                      (fail :NOT-FIXED
                                            "Fixed bindings were wrongfully modified"))
                                    ~(let [recur-form ($.clj/templ* (recur ~@fixed-sym+
                                                                           (inc ~sym)))]
                                       (if looping+
                                         ($.clj/templ* (if (= ~(-> looping+
                                                                   first
                                                                   :n)
                                                              ~(-recur looping+))
                                                         ~recur-form
                                                         (fail :BAD-ITER
                                                               "Iteration count of inner loop is wrong")))
                                         recur-form))))
        ;; Wrapping body in a loop or a fn form
        looping   (case recur-point
                    :fn   ($.clj/templ* ((fn ~(conj fixed-sym+
                                                    sym)
                                             ~body)
                                         ~@(conj fixed-x+
                                                 0)))
                    :loop ($.clj/templ* (loop ~(conj (reduce (fn [acc [sym x]]
                                                               (conj acc
                                                                     sym
                                                                     ($.clj/quoted x)))
                                                             []
                                                             fixed+)
                                                     sym
                                                     0)
                                          ~body)))]
    ;; Messing with point of recursion by wrapping in a fn that is immedialy called
    (if fn-wrap?
      ($.clj/templ* ((fn [] ~looping)))
      looping)))


;;;;;;;;;; Tests


(mprop/deftest dotimes--

  {:ratio-num 20}

  (TC.prop/for-all [n             (TC.gen/double* {:infinite? false
                                                   :max       1e3
                                                   :min       0
                                                   :NaN?      false})
                    [sym-bind
                     sym-counter] (TC.gen/vector-distinct $.clj.gen/symbol
                                                          {:num-elements 2})]
    ($.clj.eval/result* (do
                          (def ~sym-counter
                               0)
                          (dotimes [~sym-bind ~n]
                            (def ~sym-counter
                                 (+ ~sym-counter
                                    1)))
                          (== ~sym-counter
                              (floor ~n))))))



(mprop/deftest recur--

  {:ratio-num 5}

  (TC.prop/for-all [looping+ (TC.gen/vector (TC.gen/hash-map :fixed+      ($.clj.gen/binding+ 0
                                                                                              4)
                                                             :fn-wrap?    $.clj.gen/boolean
                                                             :n           (TC.gen/choose 0
                                                                                         5)
                                                             :recur-point (TC.gen/elements [:fn
                                                                                            :loop])
                                                             :sym         (TC.gen/such-that #(not (#{;; TODO. Must also be different from syms in `:fixed+`
                                                                                                     '+
                                                                                                     '=
                                                                                                     'fail
                                                                                                     'inc
                                                                                                     'recur}
                                                                                                    %))
                                                                                            $.clj.gen/symbol))
                                            1
                                            5)]
    (= (-> looping+
           first
           :n)
       ($.clj.eval/result (-recur looping+)))))



(mprop/deftest reduce--

  {:ratio-num 10}

  (TC.prop/for-all [x (TC.gen/such-that #(not-empty (cond->
                                                      ;; `(list ...)` form or a vector
                                                      %
                                                      (seq? %)
                                                      rest))
                                        $.clj.gen/collection)]

    ($.clj.eval/result* (let [x '~x
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
