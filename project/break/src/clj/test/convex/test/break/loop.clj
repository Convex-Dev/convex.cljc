(ns convex.test.break.loop

  "Testing various ways of looping and doing recursion."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.break.gen              :as $.break.gen]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Helpers


(defn- -recur

  "Used by [[recur--]] for generating nested `loop` and `fn` cells which ensures that, in `recur`:
  
   - A counter is properly incremented, looping does occur
   - A fixed set of bindings do not mutate
   - Possibly nested points of recurion are respected

   Sometimes, loops are wrapped in a no-arg function just for messing with the recursion point (trying to).
  
   Otherwise, execution stops by calling `fail`."
      
  [[{:keys [fixed+
            fn-wrap?
            n
            recur-point
            sym]}
    & looping+]]

  (let [[fixed-sym+
         fixed-x+]  fixed+
        ;; Code that will be in the (loop [] ...) or ((fn [] ...)) form
        body        ($.cell/* (if (= ~sym
                                     ~n)
                                (if (= ~fixed-sym+
                                       ~fixed-x+)
                                  ~n
                                  (fail :NOT-FIXED
                                        "Fixed bindings were wrongfully modified"))
                                ~(let [recur-form ($.cell/* (recur ~@fixed-sym+
                                                                   (inc ~sym)))]
                                   (if looping+
                                     ($.cell/* (if (= ~(-> looping+
                                                           (first)
                                                           (:n))
                                                      ~(-recur looping+))
                                                 ~recur-form
                                                 (fail :BAD-ITER
                                                       "Iteration count of inner loop is wrong")))
                                     recur-form))))
        ;; Wrapping body in a loop or a fn form
        looping   (case recur-point
                    :fn   ($.cell/* ((fn [~@fixed-sym+ ~sym]
                                       ~body)
                                     ~@fixed-x+ 0))
                    :loop ($.cell/* (loop [~@(interleave fixed-sym+
                                                         fixed-x+)
                                           ~sym 0]
                                      ~body)))]
    ;; Messing with point of recursion by wrapping in a fn that is immedialy called
    (if fn-wrap?
      ($.cell/* ((fn [] ~looping)))
      looping)))


;;;;;;;;;; Tests


(mprop/deftest dotimes--

  {:ratio-num 20}

  (TC.prop/for-all [n             (TC.gen/double* {:infinite? false
                                                   :max       1e3
                                                   :min       0
                                                   :NaN?      false})
                    [sym-bind
                     sym-counter] (TC.gen/vector-distinct $.gen/symbol
                                                          {:num-elements 2})]
    (let [n-2 ($.cell/double n)]
      ($.eval/true? $.break/ctx
                    ($.cell/* (do
                                (def ~sym-counter
                                     0)
                                (dotimes [~sym-bind ~n-2]
                                  (def ~sym-counter
                                       (+ ~sym-counter
                                          1)))
                                (== ~sym-counter
                                    (floor ~n-2))))))))



(mprop/deftest recur--

  {:ratio-num 5}

  (TC.prop/for-all [looping+ (TC.gen/vector (TC.gen/hash-map :fixed+      ($.break.gen/binding-raw+ 0
                                                                                                    4)
                                                             :fn-wrap?    TC.gen/boolean
                                                             :n           (TC.gen/fmap $.cell/long
                                                                                       (TC.gen/choose 0
                                                                                                      5))
                                                             :recur-point (TC.gen/elements [:fn
                                                                                            :loop])
                                                             :sym         (TC.gen/such-that #(not ($.std/contains?
                                                                                                    ($.cell/* #{;; TODO. Must also be different from syms in `:fixed+`
                                                                                                                +
                                                                                                                =
                                                                                                                fail
                                                                                                                inc
                                                                                                                recur})
                                                                                                    %))
                                                                                            $.gen/symbol))
                                            1
                                            5)]
    (= (-> looping+
           (first)
           (:n))
       ($.eval/result $.break/ctx
                      (-recur looping+)))))



(mprop/deftest reduce--

  {:ratio-num 10}

  (TC.prop/for-all [x (TC.gen/such-that not-empty
                                        $.gen/any-coll)]
    ($.eval/true? $.break/ctx
                  ($.cell/* (let [x (quote ~x)
                                  v (nth x
                                         ~($.cell/long (rand-int (count x))))]
                              (= v
                                 (reduce (fn [acc item]
                                           (if (= item
                                                  v)
                                             (reduced item)
                                             acc))
                                         :convex-sentinel
                                         x)))))))
