(ns convex.form

  "Building common forms out of cells."

  {:author "Adam Helinski"}

  (:import (convex.core.data AList)
           (convex.core.lang Symbols))
  (:refer-clojure :exclude [def
                            do
                            import
                            quote])
  (:require [convex.cell :as $.cell]))


(declare def
         do
         import
         quote)


;;;;;;;;;; Common form


(defn create-account

  "Creates a form `(create-account key)`."

  ^AList

  [key]

  ($.cell/list [Symbols/CREATE_ACCOUNT
                key]))



(defn create-peer

  "Creates a form `(create-peer key stake)`."

  ^AList

  [key stake]

  ($.cell/list [Symbols/CREATE_PEER
                key
                stake]))



(defn def

  "Creates a `def` form which interns `x` under `sym`."

  ^AList

  [sym x]

  ($.cell/list [Symbols/DEF
                sym
                x]))



(defn deploy

  "Creates a `deploy` form which deploys `code`.
  
   If `sym` is provided, the deploy form is embedded in a [[def]]."


  (^AList [code]

   ($.cell/list [Symbols/DEPLOY
                 (convex.form/quote code)]))


  (^AList [sym code]

   (convex.form/def sym
                    (deploy code))))



(defn do

  "Creates a `do` form embedded the given cells."
  
  ^AList

  [cell+]

  ($.cell/list (cons Symbols/DO
                     cell+)))



(defn quote

  "Creates form which quotes `x`."

  ^AList

  [x]

  ($.cell/list [Symbols/QUOTE
                x]))



(defn set-key

  "Creates a form `(set-key key)`."

  ^AList

  [key]

  ($.cell/list [Symbols/SET_KEY
                key]))



(defn undef

  "Opposite of [[def]]."

  ^AList

  [sym]

  ($.cell/list [Symbols/UNDEF
                sym]))
