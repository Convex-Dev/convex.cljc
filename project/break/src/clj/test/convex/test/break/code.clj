(ns convex.test.break.code

  "Testing code related utilities, such as expansion, evaluation, ..."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest eval--

  {:ratio-num 10}

  (TC.prop/for-all [x ($.gen/quoted $.gen/any)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def x
                                           ~x)
                                      (def form
                                           (quasiquote (fn []
                                                         (quote (unquote x)))))
                                      (def eval-
                                           (eval form)))))]
      (mprop/mult

        "Data evaluates to itself"

        ($.eval/true? ctx
                      ($.cell/* (= x
                                   (eval 'x))))


        "Call evaluated function"

        ($.eval/true? ctx
                      ($.cell/* (= x
                                   (eval-))))


        "Expanding form prior to `eval` has no impact"

        ($.eval/true? ctx
                      ($.cell/* (= eval-
                                   (eval (expand form)))))


        "Compiling form prior to `eval` has no impact"

        ($.eval/true? ctx
                      ($.cell/* (= eval-
                                   (eval (compile form)))))


        "Expanding and compiling form prior to `eval` has no impact"

        ($.eval/true? ctx
                      ($.cell/* (= eval-
                                   (eval (compile (expand form))))))))))



(mprop/deftest eval-as--

  {:ratio-num 10}

  (TC.prop/for-all [sym $.gen/symbol
                    x   ($.gen/quoted $.gen/any)]
    ($.eval/true? $.break/ctx
                  ($.cell/* (let [addr (deploy '(set-controller *caller*))]
                              (eval-as addr
                                       '(def ~sym
                                             ~x))
                              (= ~x
                                 (lookup addr
                                         ~sym)))))))



;; TODO. When expanders are stabilized.
;;
;; (mprop/deftest expand--)


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
