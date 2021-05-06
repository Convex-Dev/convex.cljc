(ns convex.lisp.test.core.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.mult           :as $.test.mult]
            [convex.lisp.test.prop           :as $.test.prop]))


;;;;;;;;;; Default values


(def max-size-coll

  ""

  5)


;;;;;;;;;;


(tc.ct/defspec deploy--

  {:max-size max-size-coll}

  ($.test.prop/check :convex/data
                     (fn [x]
                       ($.test.prop/mult ($.test.mult/new-account []
                                                                  ($.test.eval/form->context ($/templ {'?data x}
                                                                                                      '(def addr
                                                                                                            (deploy (quote '?data)))))
                                                                  true?)))))


;;;;;;;;;; TODO

;; `log`, about logging
