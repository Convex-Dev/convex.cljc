(ns convex.break.test.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest name--

  {:ratio-num 10}

  (TC.prop/for-all [x (TC.gen/one-of [$.clj.gen/keyword
                                      $.clj.gen/symbol-quoted
                                      $.clj.gen/symbol-ns-quoted])]
    ($.clj.eval/like-clojure?* (name ~x))))
