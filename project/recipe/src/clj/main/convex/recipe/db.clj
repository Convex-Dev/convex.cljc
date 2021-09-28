(ns convex.recipe.db

  ""

  {:author "Adam Helinski"}

  (:require [convex.cell   :as $.cell]
            [convex.cvm.db :as $.cvm.db]
            [convex.db     :as $.db]))


;;;;;;;;;;


(def db

  "An instance is simply a file.

   Memory-mapped IO makes this database super fast."

  ($.db/open "private/recipe/db/my-instance.etch"))


;;;;;;;;;;


(comment


  ($.cvm.db/global-set db)


  (= db
     ($.cvm.db/local))


  ($.cvm.db/local-set db)



  (def my-cell
       ($.cell/* [:a :b 42]))


  ($.db/write my-cell)


  (= my-cell
     ($.db/read ($.cell/hash my-cell)))


  ($.db/write-root my-cell)


  ($.db/read-root)


  ($.db/read-ref ($.cell/hash my-cell))


  ($.db/close db)


  )
