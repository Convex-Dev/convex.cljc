(ns convex.recipe.cell

  ""

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.read :as $.read]))


;;;;;;;;;;


(comment


  ($.cell/long 42)

  ($.cell/keyword "foo")

  ($.cell/vector [($.cell/long 42)
                  ($.cell/keyword "foo")])

  (str *1)

  ($.cell/address 42)

  ($.cell/blob (byte-array [1 2 3]))

  (def my-cell
       ($.cell/* (+ 2 2)))

  ($.cell/encoding my-cell)

  ($.cell/hash my-cell)

  (.cons my-cell
         ($.cell/* :head))
  
  (str *1)

  (first my-cell)


  ($.read/string "(+ 2 2)")

  ($.read/file "project/recipe/src/cvx/main/simple_contract.cvx")

  )
