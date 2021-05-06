(ns convex.lisp.test.core.fn

  ""

  {:author "Adam Helinski"}

  (:require [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp.form                :as $.form]
            [convex.lisp.test.mult           :as $.test.mult]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.schema         :as $.test.schema]))


(def max-size-coll 5)


;;;;;;;;;;


(tc.ct/defspec fn--arg-0

  ;; Calling no-arg functions.

  {:max-size max-size-coll}

  ($.test.prop/check :convex/data
                     (fn [x]
                       ($.test.prop/mult (let [x-2     ($.form/quoted x)
                                               fn-form (list 'fn
                                                             []
                                                             x-2)]
                                           (-> []
                                               ($.test.mult/fn?- fn-form)
                                               ($.test.mult/fn-call fn-form
                                                                    nil
                                                                    x-2)))))))
                                               




(tc.ct/defspec fn--arg-fixed

  ;; Calling functions with a fixed number of arguments.

  {:max-size max-size-coll}

  ($.test.prop/check ($.test.schema/binding+ 1)
                     (fn [x]
                       ($.test.prop/mult (let [arg+     (mapv (comp $.form/quoted
                                                                    second)
                                                              x)
                                               binding+ (mapv first
                                                              x)
                                               fn-form  (list 'fn
                                                              binding+
                                                              binding+)]
                                           (-> []
                                               ($.test.mult/fn?- fn-form)
                                               ($.test.mult/fn-call fn-form
                                                                    arg+
                                                                    arg+)))))))



(tc.ct/defspec fn--variadic

  ;; Calling functions with a variadic number of arguments.

  {:max-size max-size-coll}

  ($.test.prop/check ($.test.schema/binding+ 1)
                     (fn [x]
                       (let [arg+       (mapv (comp $.form/quoted
                                                    second)
                                              x)
                             binding+   (mapv first
                                              x)
                             pos-amper  (rand-int (count binding+))
                             binding-2+ (vec (concat (take pos-amper
                                                           binding+)
                                                     ['&]
                                                     (drop pos-amper
                                                           binding+)))
                             fn-form    (list 'fn
                                              binding-2+
                                              binding+)]
                         ($.test.prop/mult*
                           
                           "Right number of arguments"
                           ($.test.prop/mult (-> []
                                                 ($.test.mult/fn?- fn-form)
                                                 ($.test.mult/fn-call fn-form
                                                                      arg+
                                                                      (update arg+
                                                                              pos-amper
                                                                              vector))))

                           "1 argument less"
                           ($.test.prop/mult ($.test.mult/fn-call []
                                                                  fn-form
                                                                  (vec (concat (take pos-amper
                                                                                     arg+)
                                                                               (drop (inc pos-amper)
                                                                                     arg+)))
                                                                  (assoc arg+
                                                                         pos-amper
                                                                         [])))
                            
                           "Extra argument"
                           ($.test.prop/mult ($.test.mult/fn-call []
                                                                  fn-form
                                                                  (vec (concat (take pos-amper
                                                                                     arg+)
                                                                               [42]
                                                                               (drop pos-amper
                                                                                     arg+)))
                                                                  (update arg+
                                                                          pos-amper 
                                                                          #(vector 42
                                                                                   %)))))))))
