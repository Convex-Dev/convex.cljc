(ns convex.test.break.def

  "Testing utilities around `def`."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; 


(mprop/deftest def--

  {:ratio-num 10}

  (TC.prop/for-all [sym $.gen/symbol
                    x   $.gen/any]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def -defined?
                                           (defined? ~sym))
                                      (def sym
                                           (quote ~sym))
                                      (def x
                                           (quote ~x))
                                      (def ~sym
                                           x))))]
      (mprop/mult

        "`defined?` on input symbol returns true"

        ($.eval/true? ctx
                      ($.cell/* (defined? ~sym)))


        "Interned value is the input value"

        ($.eval/true? ctx
                      ($.cell/* (= (quote ~x)
                                   ~sym)))


        "Value is present in environment"

        ($.eval/true? ctx
                      ($.cell/* (= x
                                   (get ($/env)
                                        sym))))


        "`undef`"

        (let [ctx-2 ($.eval/ctx ctx
                                ($.cell/* (undef ~sym)))]
          (mprop/mult

            "`defined?` on input symbol returns false (unless it was a core function defined before)"

            ($.eval/true? ctx-2
                          ($.cell/* (if -defined?
                                      true
                                      (not (defined? ~sym)))))


            "Environment does not contain symbol anymore"

            ($.eval/true? ctx-2
                          ($.cell/* (not (contains-key? ($/env)
                                                        sym))))


            "Environment produced by `undef*` is the same as produced by `undef`"

            (= ($.eval/result ctx-2
                              ($.cell/* ($/env)))
               ($.eval/result ctx
                              ($.cell/* (do
                                          (undef* sym)
                                          ($/env)))))


            "Undefined symbol must result in an error when used"

            (if ($.eval/true? ctx-2
                              ($.cell/* (not (defined? sym))))
              (= ($.cell/code-std* :UNDECLARED)
                 ($.eval/exception-code ctx-2
                                        sym))
              true)))))))
