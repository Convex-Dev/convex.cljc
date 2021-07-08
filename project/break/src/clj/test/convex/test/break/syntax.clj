(ns convex.test.break.syntax

  "Testing `Syntax` utilities."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;;


(mprop/deftest syntax--

  {:ratio-num 10}

  (let [gen-meta (TC.gen/one-of [$.clj.gen/map
                                 $.clj.gen/nothing])]
    (TC.prop/for-all [sym    $.clj.gen/symbol
                      x      $.clj.gen/any
                      meta-1 gen-meta
                      meta-2 gen-meta]
      (let [ctx ($.clj.eval/ctx* (do
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
        (mprop/and (mprop/check

                     "Without meta"

                     (mprop/mult

                       "`meta` returns empty map"

                       ($.clj.eval/result ctx
                                          '(= {}
                                              (meta (syntax x))))


                       "No double wrapping"

                       ($.clj.eval/result* ctx
                                           (let [~sym (syntax x)]
                                             (= ~sym
                                                (syntax ~sym))))


                       "Rewrapping with meta"

                       ($.clj.eval/result* ctx
                                           (let [~sym (syntax x)]
                                             (= (syntax x
                                                        meta-1)
                                                (syntax ~sym
                                                        meta-1))))


                       "`syntax?`"

                       ($.clj.eval/result ctx
                                          '(syntax? (syntax x)))


                       "`unsyntax`"

                       ($.clj.eval/result ctx
                                          '(= x
                                              (unsyntax (syntax x))))))

                   (mprop/check

                     "With meta"
                     (mprop/mult

                       "`meta` returns the given metadata"

                       ($.clj.eval/result ctx
                                          '(= (if (nil? meta-1)
                                                {}
                                                meta-1)
                                              (meta (syntax x
                                                            meta-1))))


                       "No double wrapping"

                       ($.clj.eval/result* ctx
                                           (let [~sym (syntax x
                                                              meta-1)]
                                             (= ~sym
                                                (syntax ~sym
                                                        meta-1))))


                       "Rewrapping with new metadata merges all metadata"

                       ($.clj.eval/result ctx
                                          '(= (merge meta-1
                                                     meta-2)
                                              (meta (syntax (syntax x
                                                                    meta-1)
                                                            meta-2))))

                       "`syntax?"

                       ($.clj.eval/result ctx
                                          '(syntax? (syntax x
                                                            meta-1)))


                       "`unsyntax`"

                       ($.clj.eval/result ctx
                                          '(= x
                                              (unsyntax (syntax x
                                                                meta-2)))))))))))
