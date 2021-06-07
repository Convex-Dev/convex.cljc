(ns convex.break.test.code

  "Testing code related utilities, such as expansion, evaluation, ..."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest eval--

  {:ratio-num 10}

  (TC.prop/for-all [x $.lisp.gen/any]
    (let [ctx ($.cvm.eval/ctx* (do
                                 (def x
                                      ~x)
                                 (def form
                                      (quasiquote (fn []
                                                    (quote (unquote x)))))
                                 (def eval-
                                      (eval form))))]
      (mprop/mult

        "Data evaluates to itself"

        ($.cvm.eval/result ctx
                           '(= x
                               (eval 'x)))


        "Call evaluated function"

        ($.cvm.eval/result ctx
                           '(= x
                               (eval-)))


        "Expanding form prior to `eval` has no impact"

        ($.cvm.eval/result ctx
                           '(= eval-
                               (eval (expand form))))


        "Compiling form prior to `eval` has no impact"

        ($.cvm.eval/result ctx
                           '(= eval-
                               (eval (compile form))))


        "Expanding and compiling form prior to `eval` has no impact"

        ($.cvm.eval/result ctx
                           '(= eval-
                               (eval (compile (expand form)))))))))




(mprop/deftest eval-as--

  {:ratio-num 10}

  (TC.prop/for-all [sym $.lisp.gen/symbol
                    x   $.lisp.gen/any]
    ($.cvm.eval/result* (let [addr (deploy '(set-controller *caller*))]
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
