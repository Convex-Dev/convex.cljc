(ns convex.break.util

  "Miscellaneous helpers."

  {:author "Adam Helinski"})


;;;;;;;;;; Working with generative tests


(defn eq

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply =
         (clojure.core/map hash
              			   arg+)))
