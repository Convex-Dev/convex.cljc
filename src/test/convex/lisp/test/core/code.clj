(ns convex.lisp.test.core.code

  "Testing code related utilities, such as expansion, evaluation, ..."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest eval--

  (TC.prop/for-all [x $.gen/any]
    (let [ctx ($.test.eval/ctx* (do
                                  (def x
                                       ~x)
                                  (def form
                                       (quasiquote (fn []
                                                     (quote (unquote x)))))
                                  (def eval-
                                       (eval form))))]
      ($.test.prop/mult*

        "Data evaluates to itself"
        ($.test.eval/result ctx
                            '(= x
                                (eval 'x)))

        "Call evaluated function"
        ($.test.eval/result ctx
                            '(= x
                                (eval-)))

        "Expanding form prior to `eval` has no impact"
        ($.test.eval/result ctx
                            '(= eval-
                                (eval (expand form))))

        "Compiling form prior to `eval` has no impact"
        ($.test.eval/result ctx
                            '(= eval-
                                (eval (compile form))))

        "Expanding and compiling form prior to `eval` has no impact"
        ($.test.eval/result ctx
                            '(= eval-
                                (eval (compile (expand form)))))))))




($.test.prop/deftest eval-as--

  (TC.prop/for-all [sym $.gen/symbol
                    x   $.gen/any]
    ($.test.eval/result* (let [addr (deploy '(set-controller *caller*))]
                           (eval-as addr
                                    '(def ~sym
                                          ~x))
                           (= ~x
                              (lookup addr
                                      (quote ~sym)))))))



($.test.prop/deftest expand--

  (TC.prop/for-all [x $.gen/any]
    (let [ctx ($.test.eval/ctx* (def x ~x))]
      ($.test.prop/and* ($.test.prop/checkpoint*

                          "Expanding data"
                          ($.test.prop/mult*

                            "Expands to syntax"
                            ($.test.eval/result ctx
                                                '(syntax? (expand x)))

                            "No metadata is created during expansion"
                            ($.test.eval/result ctx
                                                '(= {}
                                                    (meta (expand x))))))

                        ($.test.prop/checkpoint*

                          "Expanding a macro"
                          (let [ctx-2 ($.test.eval/ctx ctx
                                                       '(defmacro macro-twice [x] [x x]))]
                            ($.test.prop/mult*

                              "Expands to syntax"
                              ($.test.eval/result ctx-2
                                                  '(syntax? (expand '(macro-twice x))))

                              "Data is expanded as needed"
                              ($.test.eval/result ctx-2
                                                  '(= [x x]
                                                      (eval (expand '(macro-twice x)))))

                              "No metadata is created during expansion"
                              ($.test.eval/result ctx-2
                                                  '(= {}
                                                      (meta (expand '(macro-write x))))))))))))


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
