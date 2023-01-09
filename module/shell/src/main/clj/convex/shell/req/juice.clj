(ns convex.shell.req.juice

  (:refer-clojure :exclude [get
                            set])
  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn get

  [ctx _arg+]

  ($.cvm/result-set ctx
                    (-> ctx
                        ($.cvm/juice)
                        ($.cell/long))))



(defn set

  [ctx [n-unit]]

  (or (when-not ($.std/long? n-unit)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Setting juice requires a long")))
      ($.cvm/juice-set ctx
                       ($.clj/long n-unit))))



(defn track-trx

  [ctx [trx]]

  (let [j-1   ($.cvm/juice ctx)   
        ctx-2 (-> ctx
                  ($.cvm/fork)
                  ($.cvm/eval trx))]
    ($.cvm/result-set ctx
                      ($.cell/* [~($.cell/long (- j-1
                                                  ($.cvm/juice ctx-2)))
                                 ~($.cvm/result ctx-2)]))))
