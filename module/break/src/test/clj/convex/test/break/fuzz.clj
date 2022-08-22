(ns convex.test.break.fuzz

  "Fuzzing testing random core forms.
  
   The CVM should be resilient. A random form either succeeds or fails, but no exception should be
   thrown and unhandled."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.break.gen              :as $.break.gen]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest random

  ;; Generating randorm forms that should either fail or succeed on the CVM, but no
  ;; JVM exception should be thrown without being handled.

  {:ratio-num 5}

  (TC.prop/for-all [core-sym ($.break.gen/core-symbol $.break/ctx)
                    arg+     (TC.gen/vector (TC.gen/fmap $.cell/quoted
                                                         $.gen/any)
                                            1
                                            8)]
    ($.eval/ctx $.break/ctx
                ($.cell/* (~core-sym ~@arg+)))
    true))
