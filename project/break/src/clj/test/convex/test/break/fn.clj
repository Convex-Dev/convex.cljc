(ns convex.test.break.fn

  "Testing creating functions and calling them."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.break.gen              :as $.break.gen]
            [convex.cell                   :as $.cell]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Suites


(defn suite-fn?

  "Tests is `cell` evaluates to a function."

  [cell]

  (mprop/check

    "`fn?`"

    ($.eval/true? $.break/ctx
                  ($.cell/* (fn? ~cell)))))



(defn suite-fn-call

  "Tests different ways of calling `form` as a function with `arg+` and ensuring it always
   returns `ret`."

  [fn-cell arg+ ret]

  (let [ctx ($.eval/ctx $.break/ctx
                        ($.cell/* (def ret
                                       ~ret)))]
    (mprop/check

      "Calling a function"

      (mprop/mult

        "Direct call"

        ($.eval/true? ctx
                      ($.cell/* (= ret
                                   (~fn-cell ~@arg+))))


        "After defining the function"

        (let [ctx-2 ($.eval/ctx ctx
                                ($.cell/* (def f
                                               ~fn-cell)))]
          (mprop/mult

            "`fn?`"

            ($.eval/true? ctx-2
                          ($.cell/* (fn? f)))


            "Calling"

            ($.eval/true? ctx-2
                          ($.cell/* (= ret
                                       (f ~@arg+))))))


        "From `let`, `fn?`"

        ($.eval/true? ctx
                      ($.cell/* (let [f ~fn-cell]
                                  (fn? f))))

        "From `let`, calling"

        ($.eval/true? ctx
                      ($.cell/* (let [f ~fn-cell]
                                  (= ret
                                     (f ~@arg+)))))))))


;;;;;;;;;; Tests


(mprop/deftest fn--arg-0

  ;; Calling no-arg functions.

  {:ratio-num 10}

  (TC.prop/for-all [ret ($.gen/quoted $.gen/any)]
    (let [fn-cell ($.cell/* (fn [] ~ret))]
      (mprop/and (suite-fn? fn-cell)
                 (suite-fn-call fn-cell
                                nil
                                ret)))))



(mprop/deftest fn--arg-fixed

  ;; Calling functions with a fixed number of arguments.

  {:ratio-num 3}

  (TC.prop/for-all [[sym+
                     x+]  ($.break.gen/binding-raw+ 1
                                                    16)]
    (let [fn-cell ($.cell/* (fn ~sym+
                                ~sym+))]
      (mprop/and (suite-fn? fn-cell)
                 (suite-fn-call fn-cell
                                x+
                                x+)))))



(mprop/deftest fn--variadic

  ;; Calling functions with a variadic number of arguments.

  {:ratio-num 3}

  (TC.prop/for-all [[sym+
                     x+]  ($.break.gen/binding-raw+ 1
                                                    16)]
    (let [i-amper     (rand-int (count sym+))
          i-amper-cvx ($.cell/long i-amper)
          sym-2+      ($.cell/vector (concat (take i-amper
                                                   sym+)
                                             [($.cell/* &)]
                                             (drop i-amper
                                                   sym+)))
          fn-cell     ($.cell/* (fn ~sym-2+
                                    ~sym+))]
      (mprop/mult
        
        "Right number of arguments"

        (mprop/and (suite-fn? fn-cell)
                   (suite-fn-call fn-cell
                                  x+
                                  ($.std/update x+
                                                i-amper-cvx
                                                (fn [x]
                                                  ($.cell/* [~x])))))


        "1 argument less"

        (suite-fn-call fn-cell
                       ($.cell/vector (concat (take i-amper
                                                    x+)
                                              (drop (inc i-amper)
                                                    x+)))
                       ($.std/assoc x+
                                    i-amper-cvx
                                    ($.cell/* [])))

         
        "Extra argument"

        (suite-fn-call fn-cell
                       ($.cell/vector (concat (take i-amper
                                                    x+)
                                              [($.cell/* 42)]
                                              (drop i-amper
                                                    x+)))
                       ($.std/update x+
                                     i-amper-cvx
                                     #($.cell/* [42
                                                 ~%])))))))
