(ns convex.recipe.cell

  ""

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.read :as $.read]))


;;;;;;;;;;


(comment


  ;;
  ;; CREATING CELLS
  ;;


  ;; 
  ;; Many types have been directly modeled on what we find in Clojure.
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


  ;; Stringifying them to Convex Lisp makes them more recognizable.
  ;;
  (str *1)

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
  ;;
  ($.cell/encoding my-vector)


  ;; A SHA256 hash can be computed over an encoding.
  ;;
  ;; Does not sound very exciting but very important for later!
  ;;
  ($.cell/hash my-vector)


  ;;
  ;; HANDLING CELLS
  ;;
  ;; Some cells offer a Java API similar to common Clojure fonctions.
  ;; In the future, we might offer a namespace for those functions (eg. conj, get, reduce, ...)
  ;; Meanwhile, a little Java interop is fine!
  ;;
  ;; The fact cells are close to Clojure is useful for off-chain computation: handling data outside the network,
  ;; preparing it, modifying it, ...
  ;;

  ;; For instance, `conj`...
  ;;
  (.conj my-vector
         ($.cell/* :c))
  
  (str *1)

  ;; Sometimes, some cells work with some Clojure functions.
  ;;
  (first my-vector)


  ;;
  ;; READER
  ;;
  ;; The Convex Lisp reader takes a string of code as input and outputs a cell.
  ;;

  ;; Reading a small snippet of code.
  ;;
  ($.read/string "(+ 2 2)")
  (str *1)


  ;; Most commonly used when fetching smart contracts written in file.
  ;;
  ;; For instance, this simple smart contract is used in `convex.recipe.client`.
  ;;
  ($.read/file "project/recipe/src/cvx/main/simple_contract.cvx")
  (str *1)


  )
