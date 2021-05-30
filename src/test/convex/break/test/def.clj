(ns convex.break.test.def

  "Testing utilities around `def`."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.prop             :as $.break.prop]
            [convex.break.util             :as $.break.util]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]))


;;;;;;;;;; 


($.break.prop/deftest def--

  (TC.prop/for-all [sym $.lisp.gen/symbol
                    x   $.lisp.gen/any]
    (let [ctx ($.break.eval/ctx* (do
                                   (def -defined?
                                        (defined? ~sym))
                                   (def sym
                                        (quote ~sym))
                                   (def x
                                        ~x)
                                   (def ~sym
                                        x)))]
      ($.break.prop/mult*

        "`defined?` on input symbol returns true"
        ($.break.eval/result* ctx
                              (defined? ~sym))

        "Interned value is the input value"
        ($.break.eval/result* ctx
                              (= ~x
                                 ~sym))

        "Value figures in environment"
        ($.break.eval/result ctx
                             '(= x
                                 (unsyntax (get ($/env)
                                                sym))))

        "`undef`"
        (let [ctx-2 ($.break.eval/ctx ctx
                                      (list 'undef
                                            sym))]
          ($.break.prop/mult*

            "`defined?` on input symbol returns false (unless it was a core function defined before)"
            ($.break.eval/result* ctx-2
                                  (if -defined?
                                    true
                                    (not (defined? ~sym))))

            "Environment does not contain symbol anymore"
            ($.break.eval/result ctx-2
                                 '(not (contains-key? ($/env)
                                                      sym)))

            "Environment produced by `undef*` is the same as produced by `undef`"
            ($.break.util/eq ($.break.eval/result ctx-2
                                                  '($/env))
                              ($.break.eval/result ctx
                                                  '(do
                                                     (undef* sym)
                                                     ($/env))))

            "Undefined symbol must result in an error when used"
            (if ($.break.eval/result ctx-2
                                     '(not (defined? sym)))
              (identical? :UNDECLARED
                          (-> ($.break.eval/exception? ctx-2
                                                       sym)
                              :convex.error/code))
              true)))))))



;; TODO. `defined?` on any symbol (besides core symbols), similarly ensure UNDECLARED errors when relevant
