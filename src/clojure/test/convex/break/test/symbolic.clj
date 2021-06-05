(ns convex.break.test.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest name--

  (TC.prop/for-all [x (TC.gen/one-of [$.lisp.gen/keyword
                                      $.lisp.gen/symbol-quoted
                                      $.lisp.gen/symbol-ns-quoted])]
    ($.break.eval/like-clojure?* (name ~x))))
