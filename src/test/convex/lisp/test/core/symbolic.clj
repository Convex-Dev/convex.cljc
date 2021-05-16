(ns convex.lisp.test.core.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest name--

  ($.test.prop/check [:or
                      :convex/keyword
                      :convex/symbol]
                     (fn [x]
                       ($.test.eval/like-clojure? (list 'name
                                                        ($.form/quoted x))))))
