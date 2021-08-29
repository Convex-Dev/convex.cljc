(ns convex.form

  "Building common forms out of cells."

  {:author "Adam Helinski"}

  (:import (convex.core.lang Symbols))
  (:refer-clojure :exclude [def
                            do
                            import
                            quote])
  (:require [convex.cell :as $.cell]))


(declare def
         do
         import
         quote)


;;;;;;;;;; Private


(defn- -sym

  ;; Casts `sym` to a CVX symbol if it is a CLJ one.

  [sym]

  (if (symbol? sym)
    ($.cell/symbol (name sym))
    sym))


;;;;;;;;;; Common form


(defn def

  "Creates a `def` form which interns `x` under `sym`."

  [sym x]

  ($.cell/list [Symbols/DEF
                (-sym sym)
                x]))



(defn deploy

  "Creates a `deploy` form which deploys `code`.
  
   If `sym` is provided, the deploy form is embedded in a [[def]]."


  ([code]

   ($.cell/list [Symbols/DEPLOY
                 (convex.form/quote code)]))


  ([sym code]

   (let [sym-2 (-sym sym)]
     (convex.form/do [(convex.form/def sym-2
                                       (deploy code))
                      (convex.form/import ($.cell/list [(symbol "address")
                                                        sym-2])
                                          sym-2)]))))



(defn do

  "Creates a `do` form embedded the given cells."
  
  [cell+]

  ($.cell/list (cons Symbols/DO
                     cell+)))



(defn import

  "Creates an `import` form which imports `x` as `as`."

  [x as]

  ($.cell/list [(symbol "import")
                x
                (keyword "as")
                as]))



(defn quote

  "Creates form which quotes `x`."

  [x]

  ($.cell/list [Symbols/QUOTE
                x]))



(defn undef

  "Opposite of [[def]]."

  [sym]

  ($.cell/list [Symbols/UNDEF
                sym]))
