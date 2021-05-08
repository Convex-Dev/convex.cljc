(ns convex.lisp.test.ctx

  "Testing core namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test    :as t]
            [convex.lisp     :as $]
            [convex.lisp.ctx :as $.ctx]))


;;;;;;;;;;


(t/deftest execution

  (let [form ($/read "(if true 42 0)")]
    (t/is (= ($/read "42")
             (->> form
                  ($.ctx/eval ($.ctx/create-fake))
                  $.ctx/result)
             (->> form
                  ($.ctx/eval ($.ctx/create-fake))
                  $.ctx/result)
             (->> form
                  ($.ctx/expand ($.ctx/create-fake))
                  $.ctx/compile
                  $.ctx/run
                  $.ctx/result)
             (->> form
                  ($.ctx/expand-compile ($.ctx/create-fake))
                  $.ctx/run
                  $.ctx/result)
             (->> form
                  ($.ctx/expand-compile ($.ctx/create-fake))
                  $.ctx/query
                  $.ctx/result)))))
