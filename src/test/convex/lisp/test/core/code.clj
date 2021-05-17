(ns convex.lisp.test.core.code

  ""

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur eval--

  ($.test.prop/check :convex/data
                     (fn [x]
                       (let [ctx ($.test.eval/ctx ($.form/templ {'?x x}
                                                                '(do
                                                                   (def x
                                                                        '?x)
                                                                   (def form
                                                                        '(fn []
                                                                           '(unquote x)))
                                                                   (def eval-
                                                                        (eval form)))))]
                         ($.test.prop/mult*

                           "Data evaluates to itself"
                           ($.test.eval/result ctx
                                               '(= x
                                                   (eval x)))

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
                                                   (eval (compile (expand form))))))))))

                                                                        

($.test.prop/deftest ^:recur expand--

  ($.test.prop/check :convex/data
                     (fn [x]
                       (let [ctx ($.test.eval/ctx ($.form/templ {'?x x}
                                                                '(def x
                                                                      '?x)))]
                         ($.test.prop/and* ($.test.prop/checkpoint*

                                             "Expanding data"
                                             ($.test.prop/mult*

                                               "Expands to syntax"
                                               ($.test.eval/result ctx
                                                                   '(syntax? (expand x)))

                                               ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/109
											   ;;
                                               ;; "Data is unchanged when expanded"
                                               ;; ($.test.eval/result ctx
                                               ;;                     '(= x
                                               ;;                         (unsyntax (expand x))))

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

                                                 ;; TODO. Actually, quoted macros are still expanded. Asked on Discord.
                                                 ;;
                                                 ;; "Data is expanded as needed"
                                                 ;; ($.test.eval/result ctx-2
                                                 ;;                     '(= [x x]
                                                 ;;                         (unsyntax (expand '(macro-twice ~x)))))

                                                 "No metadata is created during expansion"
                                                 ($.test.eval/result ctx-2
                                                                     '(= {}
                                                                         (meta (expand '(macro-write x)))))))))))))


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
; splice-unquote
