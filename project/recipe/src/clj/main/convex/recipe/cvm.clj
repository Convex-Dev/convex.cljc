(ns convex.recipe.cvm

  ""

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]))


;;;;;;;;;;


(def ctx

  ""

  ($.cvm/ctx))



(def code

  ""

  ($.cell/* (conj [:a]
                  :b)))


;;;;;;;;;;


(comment


  (-> ($.cvm/eval ctx
                  code)
      $.cvm/result
      str)


  (def expanded
       (-> ($.cvm/expand ctx
                         ($.cell/* (if (< 2 3)
                                     :yes
                                     :no)))
           $.cvm/result))

  (str expanded)


  (def compiled
       (-> ($.cvm/compile ctx
                          expanded)
           $.cvm/result))

  (str compiled)


  (-> ($.cvm/exec ctx
                  compiled)
      $.cvm/result
      str)


  (-> ($.cvm/expand-compile ctx
                            code)
      $.cvm/result)


  ($.cvm/juice-refill ctx)


  )
