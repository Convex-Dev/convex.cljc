(ns convex.break.test.code

  "Testing code related utilities, such as expansion, evaluation, ..."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.prop             :as $.test.prop]
            [convex.lisp.gen               :as $.lisp.gen]
            ))


;;;;;;;;;;


($.test.prop/deftest eval--

  (TC.prop/for-all [x $.lisp.gen/any]
    (let [ctx ($.break.eval/ctx* (do
                                   (def x
                                        ~x)
                                   (def form
                                        (quasiquote (fn []
                                                      (quote (unquote x)))))
                                   (def eval-
                                        (eval form))))]
      ($.test.prop/mult*

        "Data evaluates to itself"
        ($.break.eval/result ctx
                             '(= x
                                 (eval 'x)))

        "Call evaluated function"
        ($.break.eval/result ctx
                             '(= x
                                 (eval-)))

        "Expanding form prior to `eval` has no impact"
        ($.break.eval/result ctx
                             '(= eval-
                                 (eval (expand form))))

        "Compiling form prior to `eval` has no impact"
        ($.break.eval/result ctx
                             '(= eval-
                                 (eval (compile form))))

        "Expanding and compiling form prior to `eval` has no impact"
        ($.break.eval/result ctx
                             '(= eval-
                                 (eval (compile (expand form)))))))))




($.test.prop/deftest eval-as--

  (TC.prop/for-all [sym $.lisp.gen/symbol
                    x   $.lisp.gen/any]
    ($.break.eval/result* (let [addr (deploy '(set-controller *caller*))]
                            (eval-as addr
                                     '(def ~sym
                                           ~x))
                            (= ~x
                               (lookup addr
                                       (quote ~sym)))))))


;; TODO. When expanders are stabilized.
;;
;; ($.test.prop/deftest expand--)


;;;;;;;;;;


; TODO. Do can we actually do with `compile` and `expand`? Results are not something that can be used in the sandbox.

; *initial-expander*
; compile
; defexpander
; defmacro
; eval
; eval-as
; expand
; expander
; macro
; quote
; unquote
; unquote-splicing
