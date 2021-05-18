(ns convex.lisp.test.core.syntax

  "Testing `Syntax` utilities."

  {:author "Adam Helinski"}

  (:require [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]))


;;;;;;;;;;


($.test.prop/deftest ^:recur syntax--
  

  ($.test.prop/check [:tuple
                      :convex/symbol
                      :convex/data
                      :convex/meta
                      :convex/meta]
                     (fn [[sym x meta-1 meta-2]]
                       (let [ctx ($.test.eval/ctx ($.form/templ*  (def meta-1
                                                                       '~meta-1)
                                                                  (def meta-2
                                                                       '~meta-2)
                                                                  (def sym
                                                                       '~sym)
                                                                  (def x
                                                                       '~x)
                                                                  (def ~sym
                                                                       x)))]
                         ($.test.prop/and* ($.test.prop/checkpoint*

                                             "`lookup-syntax`"
                                             ($.test.prop/mult*

                                               "`meta` returns empty map"
                                               ($.test.eval/result ctx
                                                                   '(= {}
                                                                       (meta (lookup-syntax sym))))

                                               "`syntax?`"
                                               ($.test.eval/result ctx
                                                                   '(syntax? (lookup-syntax sym)))

                                               "`unsyntax`"
                                               ($.test.eval/result ctx
                                                                   '(= x
                                                                       (unsyntax (lookup-syntax sym))))))

                                           ($.test.prop/checkpoint*

                                             "Without meta"
                                             ($.test.prop/mult*

                                               "`meta` returns empty map"
                                               ($.test.eval/result ctx
                                                                   '(= {}
                                                                       (meta (syntax x))))

                                               "No double wrapping"
                                               ($.test.eval/result ctx
                                                                   ($.form/templ {'?sym sym}
                                                                                 '(let [?sym (syntax x)]
                                                                                    (= ?sym
                                                                                       (syntax ?sym)))))

                                               "Rewrapping with meta"
                                               ($.test.eval/result ctx
                                                                   ($.form/templ {'?sym sym}
                                                                                 '(let [?sym (syntax x)]
                                                                                    (= (syntax x
                                                                                               meta-1)
                                                                                       (syntax ?sym
                                                                                               meta-1)))))

                                               "`syntax?`"
                                               ($.test.eval/result ctx
                                                                   '(syntax? (syntax x)))

                                               "`unsyntax`"
                                               ($.test.eval/result ctx
                                                                   '(= x
                                                                       (unsyntax (syntax x))))))

                                           ($.test.prop/checkpoint*

                                             "With meta"
                                             ($.test.prop/mult*

                                               "`meta` returns the given metadata"
                                               ($.test.eval/result ctx
                                                                   '(= (if (nil? meta-1)
                                                                         {}
                                                                         meta-1)
                                                                       (meta (syntax x
                                                                                     meta-1))))

                                               "No double wrapping"
                                               ($.test.eval/result ctx
                                                                   ($.form/templ {'?sym sym}
                                                                                 '(let [?sym (syntax x
                                                                                                     meta-1)]
                                                                                    (= ?sym
                                                                                       (syntax ?sym
                                                                                               meta-1)))))

                                               "Rewrapping with new metadata merges all metadata"
                                               ($.test.eval/result ctx
                                                                   '(= (merge meta-1
                                                                              meta-2)
                                                                       (meta (syntax (syntax x
                                                                                             meta-1)
                                                                                     meta-2))))

                                               "`syntax?"
                                               ($.test.eval/result ctx
                                                                   '(syntax? (syntax x
                                                                                     meta-1)))

                                               "`unsyntax`"
                                               ($.test.eval/result ctx
                                                                   '(= x
                                                                       (unsyntax (syntax x
                                                                                         meta-2)))))))))))
