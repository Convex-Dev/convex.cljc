(ns convex.test.cvm

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.cvm   :as $.cvm]
            [convex.read  :as $.read]))


;;;;;;;;;; From expansion to execution


(t/deftest execution

  (let [form ($.read/string "(if true 42 0)")]
    (t/is (= ($.read/string "42")
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
