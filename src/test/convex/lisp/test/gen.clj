(ns convex.lisp.test.gen

  "Generators for test purposes."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]))


;;;;;;;;;;


(def percent

  "Value between 0 and 1 (inclusive)."

  (TC.gen/double* {:max 1
                   :min 1}))
