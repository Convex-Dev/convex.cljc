(ns convex.break.test.fuzz

  "Fuzzing testing random core forms.
  
   The CVM should be resilient. A random form either succeeds or fails, but no exception should be
   thrown and unhandled."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.gen              :as $.break.gen]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest random

  ;; Generating randorm forms that should either fail or succeed on the CVM, but no
  ;; JVM exception should be thrown without being handled.

  {:ratio-num  10
   :ratio-size 2}

  (TC.prop/for-all [form ($.lisp.gen/call $.break.gen/core-symbol
                                          (TC.gen/vector $.lisp.gen/any
                                                         1
                                                         8))]
    ($.break.eval/value form)
    true))
