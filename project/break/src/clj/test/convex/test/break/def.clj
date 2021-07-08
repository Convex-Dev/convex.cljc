(ns convex.test.break.def

  "Testing utilities around `def`."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; 


(mprop/deftest def--

  {:ratio-num 10}

  (TC.prop/for-all [sym $.clj.gen/symbol
                    x   $.clj.gen/any]
    (let [ctx ($.clj.eval/ctx* (do
                                   (def -defined?
                                        (defined? ~sym))
                                   (def sym
                                        (quote ~sym))
                                   (def x
                                        ~x)
                                   (def ~sym
                                        x)))]
      (mprop/mult

        "`defined?` on input symbol returns true"

        ($.clj.eval/result* ctx
                            (defined? ~sym))


        "Interned value is the input value"

        ($.clj.eval/result* ctx
                            (= ~x
                               ~sym))


        "Value figures in environment"

        ($.clj.eval/result ctx
                           '(= x
                               (unsyntax (get ($/env)
                                              sym))))


        "`undef`"

        (let [ctx-2 ($.clj.eval/ctx ctx
                                    (list 'undef
                                          sym))]
          (mprop/mult

            "`defined?` on input symbol returns false (unless it was a core function defined before)"

            ($.clj.eval/result* ctx-2
                                (if -defined?
                                  true
                                  (not (defined? ~sym))))


            "Environment does not contain symbol anymore"

            ($.clj.eval/result ctx-2
                               '(not (contains-key? ($/env)
                                                    sym)))


            "Environment produced by `undef*` is the same as produced by `undef`"

            ($.clj/= ($.clj.eval/result ctx-2
                                         '($/env))
                      ($.clj.eval/result ctx
                                         '(do
                                            (undef* sym)
                                            ($/env))))

            "Undefined symbol must result in an error when used"

            (if ($.clj.eval/result ctx-2
                                   '(not (defined? sym)))
              ($.clj.eval/code? :UNDECLARED
                                ctx-2
                                sym)
              true)))))))



;; TODO. `defined?` on any symbol (besides core symbols), similarly ensure UNDECLARED errors when relevant
