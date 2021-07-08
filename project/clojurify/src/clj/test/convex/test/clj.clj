(ns convex.test.clj

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.clj   :as $.clj]))


;;;;;;;;;;


(T/deftest foo

  (T/is (= 4
           (+ 2
              2))))
