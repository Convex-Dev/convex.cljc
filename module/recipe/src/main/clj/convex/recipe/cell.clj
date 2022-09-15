(ns convex.recipe.cell

  "Cells represents anything that can be handled on the Convex network: data and other types such as functions.
  
   They have been modeled very closely on Clojure and many of those types will be familiar to any Clojurist.

   These examples show how to create cells and handle them. It is the very first step towards understanding Convex
   and how to build dApps."

  {:author "Adam Helinski"}

  (:require [convex.cell  :as $.cell]
            [convex.clj   :as $.clj]
            [convex.read  :as $.read]
            [convex.std   :as $.std]
            [convex.write :as $.write]))


;;;;;;;;;;


(comment


  ;;
  ;; CREATING CELLS
  ;;


  ;; 
  ;; Many types have been directly modeled on Clojure data.
  ;;
  ;; This is because we know that data is king and that data-centric applications
  ;; are key for building robust systems. Thanks Rich!
  ;;

  ;; A long...
  ;;
  ($.cell/long 42)

  ;; A keyword...
  ;;
  ($.cell/keyword "foo")

  ;; A vector...
  ;;
  ($.cell/vector [($.cell/long 42)
                  ($.cell/keyword "foo")])

  ;; Same vector using a conveninent macro...
  ;;
  ($.cell/* [42 :foo])

  
  ;; To avoid confusion, let's talk about Convex data by designating it with "CVX".
  ;;
  ;;  E.g. Keyword Vs. CVX keyword


  ;;
  ;; A few types are specific to Convex.
  ;;

  ;; Like account addresses...
  ;;
  ($.cell/address 42)

  ;; Or binary large objects...
  ;;
  ($.cell/blob (byte-array [1 2 3]))


  ;; Namespace `convex.cell` has functions for creating those cells.
  ;;
  ;; But usually it is easier using the `*` macro which converts Clojure data to Convex.
  ;;
  ($.cell/* (+ 2 2))

  ;; And `~` can be used to insert Convex types.
  ;;
  ($.cell/* (transfer ~($.cell/address 42)
                      500000))


  ;;
  ;; ENCODING
  ;;
  ;; Cells have been designed explicitly for fast storage and efficient sharing over the network.
  ;;


  ;; Let us suppose this vector.
  ;;
  (def my-vector
       ($.cell/* [:a :b]))


  ;; Each cell can be encoded to an efficient, dense binary representation.
  ;; This encoding is meant for incremental updates, making it perfect for handling large data
  ;; structures over the network.
  ;;
  ($.cell/encoding my-vector)


  ;; A SHA256 hash can be computed over an encoding.
  ;;
  ;; It does not sound very exciting but very important for [[convex.recipe.db]].
  ;;
  ($.cell/hash my-vector)


  ;;
  ;; HANDLING CELLS
  ;;


  ;; Almost all core Clojure functions related to sequences work on Convex collections.
  ;;
  (first ($.cell/* [:a :b]))

  (map identity
       ($.cell/* [:a :b :c]))

  (concat ($.cell/* [:a :b])
          ($.cell/* [:c :d]))


  ;; Other classic Clojure functions can be found in the [[convex.std]] namespace.
  ;;
  ($.std/conj ($.cell/* [:a :b])
              ($.cell/* :c))

  ($.std/get ($.cell/* {:a :b})
             ($.cell/* :a))


  ;; Sometimes it is useful converting a cell to a Clojure type via the [[convex.clj]] namespace.
  ;;
  (-> ($.cell/address 42)
      ($.clj/address))


  ;; And in the rare cases where all of this is not enough, there is always Java interop.
  ;;
  ;; https://www.javadoc.io/doc/world.convex/convex-core/latest/convex/core/data/package-summary.html


  ;;
  ;; READER
  ;;
  ;; The Convex Lisp reader takes a string of code as input and outputs a CVX list of cells.
  ;;
  ;; Convex Lisp is the language used for querying data from the network or submitting transactions, such as
  ;; creating smart contracts. It is almost a subset of Clojure with added capabilities.
  ;;
  ;; See [[convex.recipe.cvm]] for examples on how to compile and evaluate cells in order to execute code.
  ;;

  ;; Reading a small snippet of code.
  ;;
  ($.read/string "(+ 2 2)")


  ;; Most commonly used when fetching smart contracts written in file.
  ;;
  ;; For instance, this simple smart contract is used in [[convex.recipe.client]].
  ;;
  ($.read/file "module/recipe/src/main/cvx/simple_contract.cvx")


  ;; Cells can be printed to readable Convex Lisp.
  ;;
  ($.write/string ($.cell/* (+ 2 2)))


  )
