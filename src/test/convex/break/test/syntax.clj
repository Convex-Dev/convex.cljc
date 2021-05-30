(ns convex.break.test.syntax

  "Testing `Syntax` utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.prop             :as $.break.prop]
            [convex.lisp.gen               :as $.lisp.gen]))


;;;;;;;;;;


($.break.prop/deftest syntax--

  (let [gen-meta (TC.gen/one-of [$.lisp.gen/map
                                 $.lisp.gen/nothing])]
    (TC.prop/for-all [sym    $.lisp.gen/symbol
                      x      $.lisp.gen/any
                      meta-1 gen-meta
                      meta-2 gen-meta]
      (let [ctx ($.break.eval/ctx* (do
                                     (def meta-1
                                          ~meta-1)
                                     (def meta-2
                                          ~meta-2)
                                     (def sym
                                          (quote ~sym))
                                     (def x
                                          ~x)
                                     (def ~sym
                                          x)))]
        ($.break.prop/and* ($.break.prop/checkpoint*

                            "`lookup-syntax`"
                            ($.break.prop/mult*

                              "`meta` returns empty map"
                              ($.break.eval/result ctx
                                                   '(= {}
                                                       (meta (lookup-syntax sym))))

                              "`syntax?`"
                              ($.break.eval/result ctx
                                                   '(syntax? (lookup-syntax sym)))

                              "`unsyntax`"
                              ($.break.eval/result ctx
                                                   '(= x
                                                       (unsyntax (lookup-syntax sym))))))

                          ($.break.prop/checkpoint*

                            "Without meta"
                            ($.break.prop/mult*

                              "`meta` returns empty map"
                              ($.break.eval/result ctx
                                                   '(= {}
                                                       (meta (syntax x))))

                              "No double wrapping"
                              ($.break.eval/result* ctx
                                                    (let [~sym (syntax x)]
                                                      (= ~sym
                                                         (syntax ~sym))))

                              "Rewrapping with meta"
                              ($.break.eval/result* ctx
                                                    (let [~sym (syntax x)]
                                                      (= (syntax x
                                                                 meta-1)
                                                         (syntax ~sym
                                                                 meta-1))))

                              "`syntax?`"
                              ($.break.eval/result ctx
                                                   '(syntax? (syntax x)))

                              "`unsyntax`"
                              ($.break.eval/result ctx
                                                   '(= x
                                                       (unsyntax (syntax x))))))

                          ($.break.prop/checkpoint*

                            "With meta"
                            ($.break.prop/mult*

                              "`meta` returns the given metadata"
                              ($.break.eval/result ctx
                                                   '(= (if (nil? meta-1)
                                                         {}
                                                         meta-1)
                                                       (meta (syntax x
                                                                     meta-1))))

                              "No double wrapping"
                              ($.break.eval/result* ctx
                                                    (let [~sym (syntax x
                                                                       meta-1)]
                                                      (= ~sym
                                                         (syntax ~sym
                                                                 meta-1))))

                              "Rewrapping with new metadata merges all metadata"
                              ($.break.eval/result ctx
                                                   '(= (merge meta-1
                                                              meta-2)
                                                       (meta (syntax (syntax x
                                                                             meta-1)
                                                                     meta-2))))

                              "`syntax?"
                              ($.break.eval/result ctx
                                                   '(syntax? (syntax x
                                                                     meta-1)))

                              "`unsyntax`"
                              ($.break.eval/result ctx
                                                   '(= x
                                                       (unsyntax (syntax x
                                                                         meta-2)))))))))))
