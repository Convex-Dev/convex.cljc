(ns convex.test.break.symbolic

  "Testing utilities related to keywords and symbols."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest name--

  {:ratio-num 10}

  (TC.prop/for-all [keyword? TC.gen/boolean
                    string   $.gen/string-symbolic]
    (let [string-jvm (str string)
          assertion  (fn [symbolic]
                       ($.eval/true? $.break/ctx
                                    ($.cell/* (= ~string
                                                 (name ~symbolic)))))]
      (mprop/mult

        "Prebuilt"
        (assertion (if keyword?
                     ($.cell/keyword string-jvm)
                     ($.cell/quoted ($.cell/symbol string-jvm))))

        "Constructing value in CVX Lisp"
        (assertion ($.cell/* (~($.cell/symbol (if keyword?
                                                "keyword"
                                                "symbol"))
                              ~string)))))))
