(ns convex.test.break.fn

  "Testing creating functions and calling them."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites


(defn suite-fn?

  "Tests is `form` evaluates to a function."

  [form]

  (mprop/check

    "`fn?`"

    ($.clj.eval/result* (fn? ~form))))



(defn suite-fn-call

  "Tests different ways of calling `form` as a function with `arg+` and ensuring it always
   returns `ret`."

  [form arg+ ret]

  (let [ctx ($.clj.eval/ctx* (def ret
                                  ~ret))]
    (mprop/check

      "Calling a function"

      (mprop/mult

        "Direct call"

        ($.clj.eval/result* ctx
                            (= ret
                               (~form ~@arg+)))


        "After def"

        (let [ctx-2 ($.clj.eval/ctx* ctx
                                     (def f
                                          ~form))]
          (mprop/mult

            "`fn?`"

            ($.clj.eval/result ctx-2
                               '(fn? f))


            "Calling"

            ($.clj.eval/result* ctx-2
                                (= ret
                                   (f ~@arg+)))))


        "From `let`, `fn?`"

        ($.clj.eval/result* ctx
                            (let [f ~form]
                              (fn? f)))


        "From `let`, calling"

        ($.clj.eval/result* ctx
                            (let [f ~form]
                              (= ret
                                 (f ~@arg+))))))))


;;;;;;;;;; Tests


(mprop/deftest fn--arg-0

  ;; Calling no-arg functions.

  {:ratio-num 10}

  (TC.prop/for-all [x $.clj.gen/any]
    (let [fn-form ($.clj/templ* (fn [] ~x))]
      (mprop/and (suite-fn? fn-form)
                 (suite-fn-call fn-form
                                nil
                                x)))))



(mprop/deftest fn--arg-fixed

  ;; Calling functions with a fixed number of arguments.

  {:ratio-num 3}

  (TC.prop/for-all [binding+ ($.clj.gen/binding+ 1
                                                 16)]
    (let [arg+    (mapv second
                        binding+)
          sym+    (mapv first
                        binding+)
          fn-form ($.clj/templ* (fn ~sym+
                                    ~sym+))]
      (mprop/and (suite-fn? fn-form)
                 (suite-fn-call fn-form
                                arg+
                                arg+)))))



(mprop/deftest fn--variadic

  ;; Calling functions with a variadic number of arguments.

  {:ratio-num 3}

  (TC.prop/for-all [binding+ ($.clj.gen/binding+ 1
                                                 16)]

    (let [arg+       (mapv second
                           binding+)
          binding+   (mapv first
                           binding+)
          pos-amper  (rand-int (count binding+))
          binding-2+ (vec (concat (take pos-amper
                                        binding+)
                                  ['&]
                                  (drop pos-amper
                                        binding+)))
          fn-form    ($.clj/templ* (fn ~binding-2+
                                       ~binding+))]
      (mprop/mult
        
        "Right number of arguments"

        (mprop/and (suite-fn? fn-form)
                           (suite-fn-call fn-form
                                          arg+
                                          (update arg+
                                                  pos-amper
                                                  vector)))


        "1 argument less"

        (suite-fn-call fn-form
                       (vec (concat (take pos-amper
                                          arg+)
                                    (drop (inc pos-amper)
                                          arg+)))
                       (assoc arg+
                              pos-amper
                              []))

         
        "Extra argument"

        (suite-fn-call fn-form
                       (vec (concat (take pos-amper
                                          arg+)
                                    [42]
                                    (drop pos-amper
                                          arg+)))
                       (update arg+
                               pos-amper 
                               #(vector 42
                                        %)))))))
