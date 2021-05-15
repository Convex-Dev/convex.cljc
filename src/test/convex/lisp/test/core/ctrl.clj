(ns convex.lisp.test.core.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]
            [convex.lisp.test.util :as $.test.util]))


;;;;;;;;;; 


($.test.prop/deftest ^:recur and-or

  ($.test.prop/check [:vector
                      :convex/data]
                     (fn [x]
                       (let [x-quoted (map $.form/quoted
                                           x)
                             assertion (fn [sym]
                                         (let [form (list* sym
                                                           x-quoted)]
                                           ($.test.util/eq (eval form)
                                                           ($.test.eval/result form))))]
                         ($.test.prop/mult*

                           "`and` consistent with Clojure"
                           (assertion 'and)

                           "`or` consistent with Clojure"
                           (assertion 'or))))))


;;;;;;;;;;


; assert
; cond
; fail
; halt
; if
; return
; rollback
