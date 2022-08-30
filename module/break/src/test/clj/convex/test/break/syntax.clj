(ns convex.test.break.syntax

  "Testing `Syntax` utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest syntax--

  {:ratio-num 10}

  (let [gen-meta (TC.gen/one-of [$.gen/any-map
                                 $.gen/nothing])]
    (TC.prop/for-all [sym    $.gen/symbol
                      x      (TC.gen/such-that (comp not
                                                     $.std/syntax?)
                                               $.gen/any
                                               100)
                      meta-1 gen-meta
                      meta-2 gen-meta]
      (let [ctx ($.eval/ctx $.break/ctx
                            ($.cell/* (do
                                        (def meta-1
                                             (quote ~meta-1))
                                        (def meta-2
                                             (quote ~meta-2))
                                        (def sym
                                             (quote ~sym))
                                        (def x
                                             (quote ~x))
                                        (def ~sym
                                             x))))]
        (mprop/and (mprop/check

                     "Without meta"

                     (mprop/mult

                       "`meta` returns empty map"

                       ($.eval/true? ctx
                                     ($.cell/* (= {}
                                                  (meta (syntax x)))))


                       "No double wrapping"

                       ($.eval/true? ctx
                                     ($.cell/* (let [~sym (syntax x)]
                                                 (= ~sym
                                                    (syntax ~sym)))))


                       "Rewrapping with meta"

                       ($.eval/true? ctx
                                     ($.cell/* (let [~sym (syntax x)]
                                                 (= (syntax x
                                                            meta-1)
                                                    (syntax ~sym
                                                            meta-1)))))


                       "`syntax?`"

                       ($.eval/true? ctx
                                     ($.cell/* (syntax? (syntax x))))


                       "`unsyntax`"

                       ($.eval/true? ctx
                                     ($.cell/* (= x
                                                  (unsyntax (syntax x)))))))

                   (mprop/check

                     "With meta"
                     (mprop/mult

                       "`meta` returns the given metadata"

                       ($.eval/true? ctx
                                     ($.cell/* (= (if (nil? meta-1)
                                                    {}
                                                    meta-1)
                                                  (meta (syntax x
                                                                meta-1)))))


                       "No double wrapping"

                       ($.eval/true? ctx
                                     ($.cell/* (let [~sym (syntax x
                                                                  meta-1)]
                                                 (= ~sym
                                                    (syntax ~sym
                                                            meta-1)))))


                       "Rewrapping with new metadata merges all metadata"

                       ($.eval/true? ctx
                                     ($.cell/* (= (merge meta-1
                                                         meta-2)
                                                  (meta (syntax (syntax x
                                                                        meta-1)
                                                                meta-2)))))

                       "`syntax?"

                       ($.eval/true? ctx
                                     ($.cell/* (syntax? (syntax x
                                                                meta-1))))


                       "`unsyntax`"

                       ($.eval/true? ctx
                                     ($.cell/* (= x
                                                  (unsyntax (syntax x
                                                                    meta-2))))))))))))
