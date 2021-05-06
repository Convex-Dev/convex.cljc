(ns convex.lisp.test.util

  "Miscellaneous helpers for tests."

  {:author "Adam Helinski"})


;;;;;;;;;; Working with generative tests


(defn eq

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply =
         (clojure.core/map hash
              			   arg+)))
