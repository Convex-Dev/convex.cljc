(ns convex.lisp

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.lisp  :as $]))


;;;;;;;;;;


(t/deftest main

  (t/is (true? true)))
