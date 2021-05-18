(ns convex.lisp.test.core.def

  "Testing utilities around `def`."

  {:author "Adam Helinski"}

  (:require [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]
            [convex.lisp.test.util :as $.test.util]))


;;;;;;;;;; 


($.test.prop/deftest ^:recur def--

  ($.test.prop/check [:tuple
                      :convex/symbol
                      :convex/data]
                     (fn [[sym x]]
                       (let [ctx ($.test.eval/ctx* (do
                                                     (def -defined?
                                                          (defined? ~sym))
                                                     (def sym
                                                          (quote ~sym))
                                                     (def x
                                                          (quote ~x))
                                                     (def ~sym
                                                          x)))]
                         ($.test.prop/mult*

                           "`defined?` on input symbol returns true"
                           ($.test.eval/result* ctx
                                                (defined? ~sym))

                           "Interned value is the input value"
                           ($.test.eval/result* ctx
                                                (= (quote ~x)
                                                   x
                                                   ~sym))

                           "Value figures in environment"
                           ($.test.eval/result ctx
                                               '(= x
                                                   (unsyntax (get ($/env)
                                                                  sym))))

                           "`undef`"
                           (let [ctx-2 ($.test.eval/ctx ctx
                                                        (list 'undef
                                                              sym))]
                             ($.test.prop/mult*

                               "`defined?` on input symbol returns false (unless it was a core function defined before)"
                               ($.test.eval/result* ctx-2
                                                    (if -defined?
                                                      true
                                                      (not (defined? ~sym))))

                               "Environment does not contain symbol anymore"
                               ($.test.eval/result ctx-2
                                                   '(not (contains-key? ($/env)
                                                                        sym)))

                               "Environment produced by `undef*` is the same as produced by `undef`"
                               ($.test.util/eq ($.test.eval/result ctx-2
                                                                   '($/env))
                                               ($.test.eval/result ctx
                                                                   '(do
                                                                      (undef* sym)
                                                                      ($/env))))

                               "Undefined symbol must result in an error when used"
                               (if ($.test.eval/result ctx-2
                                                       '(not (defined? sym)))
                                 (identical? :UNDECLARED
                                             (-> ($.test.eval/error? ctx-2
                                                                     sym)
                                                 :convex.error/code))
                                 true))))))))



;; TODO. `defined?` on any symbol (besides core symbols), similarly ensure UNDECLARED errors when relevant
