(ns convex.lisp.test.core.actor

  "Testing actor utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.mult :as $.test.mult]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur deploy--

  ($.test.prop/check :convex/data
                     (fn [x]
                       ($.test.prop/mult
                         ($.test.mult/new-account []
                                                  ($.test.eval/ctx ($.form/templ {'?data x}
                                                                                 '(def addr
                                                                                       (deploy (quote '?data)))))
                                                  true?)))))
