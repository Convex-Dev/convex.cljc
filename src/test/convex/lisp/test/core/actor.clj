(ns convex.lisp.test.core.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form              :as $.form]
            [convex.lisp.test.core.account :as $.test.core.account]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur deploy--

  ($.test.prop/check :convex/data
                     (fn [x]
                       ($.test.core.account/suite-new ($.test.eval/ctx ($.form/templ {'?data x}
                                                                                     '(def addr
                                                                                           (deploy (quote '?data)))))
                                                      true?))))
