(ns convex.break.test.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest name--

  {:ratio-num 10}

  (TC.prop/for-all [x (TC.gen/one-of [$.lisp.gen/keyword
                                      $.lisp.gen/symbol-quoted
                                      $.lisp.gen/symbol-ns-quoted])]
    ($.cvm.eval/like-clojure?* (name ~x))))
