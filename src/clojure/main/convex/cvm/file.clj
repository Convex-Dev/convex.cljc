(ns convex.cvm.file

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            read])
  (:require [convex.cvm     :as $.cvm]
            [convex.cvm.raw :as $.cvm.raw]))


;;;;;;;;;;


(defn read

  ""

  [path]

  (-> path
      slurp
      $.cvm/read))



(defn run

  ""


  ([ctx path]

   (run ctx
        path
        identity))


  ([ctx path wrap-read]

   (let [juice- ($.cvm/juice ctx)]
     (.withJuice ($.cvm/eval ($.cvm/fork ctx)
                             (-> path
                                 read
                                 wrap-read))
                 juice-))))


;;;;;;;;;;


(defn deploy

  ""


  ([ctx sym path]

   (deploy ctx
           sym
           path
           identity))


  ([ctx sym path wrap-read]

  (let [sym-2 ($.cvm.raw/symbol sym)]
    (run ctx
         path
         (fn [parsed]
           ($.cvm.raw/do ($.cvm.raw/def sym-2
                                        ($.cvm.raw/deploy (wrap-read parsed)))
                         ($.cvm.raw/import ($.cvm.raw/list [($.cvm.raw/symbol 'address)
                                                            sym-2])
                                           sym-2)))))))



(defn intern

  ""


  ([ctx sym path]

   (intern ctx
           sym
           path
           identity))
          

  ([ctx sym path wrap-read]

   (run ctx
        path
        (fn [parsed]
          ($.cvm.raw/def ($.cvm.raw/symbol sym)
                         (wrap-read parsed))))))
