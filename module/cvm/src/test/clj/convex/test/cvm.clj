(ns convex.test.cvm

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.cvm   :as $.cvm]))


;;;;;;;;;; From expansion to execution


(T/deftest execution

  (let [form ($.cell/* (if true 42 0))]
    (T/is (= ($.cell/* 42)
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  $.cvm/result)
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  $.cvm/result)
             (->> form
                  ($.cvm/expand ($.cvm/ctx))
                  $.cvm/compile
                  $.cvm/exec
                  $.cvm/result)
             (->> form
                  ($.cvm/expand-compile ($.cvm/ctx))
                  $.cvm/exec
                  $.cvm/result)))))
