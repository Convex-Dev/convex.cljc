(ns convex.shell.req.juice

  (:refer-clojure :exclude [set])
  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn set

  [ctx [n-unit]]

  (or (when-not ($.std/long? n-unit)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Setting juice requires a long")))
      (let [n-unit-2 ($.clj/long n-unit)]
        (or (when (neg? n-unit-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Juice units cannot be < 1")))
            ($.cvm/juice-set ctx
                             ($.clj/long n-unit))))))



(defn track

  [ctx [trx]]

  (let [j-1   ($.cvm/juice ctx)   
        ctx-2 (-> ctx
                  ($.cvm/fork)
                  ($.cvm/eval trx))]
    ($.cvm/result-set ctx
                      ($.cell/* [~($.cvm/result ctx-2)
                                 ~($.cell/long (- j-1
                                                  ($.cvm/juice ctx-2)))]))))
