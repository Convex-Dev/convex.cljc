(ns convex.lisp.test.core.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest name--

  (TC.prop/for-all [x (TC.gen/one-of [$.gen/keyword
                                      $.gen/symbol-quoted
                                      $.gen/symbol-ns-quoted])]
    ($.test.eval/like-clojure?* (name ~x))))
