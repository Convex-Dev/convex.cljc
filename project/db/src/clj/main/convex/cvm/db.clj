(ns convex.cvm.db

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.store AStore
                              Stores)))


;;;;;;;;;;


(defn default

  ""

  ^AStore

  []

  (Stores/getDefaultStore))



(defn global

  ""

  ^AStore

  []

  (Stores/getGlobalStore))



(defn global-set

  ""

  ^AStore

  [db]

  (Stores/setGlobalStore db)
  db)



(defn local

  ""

  ^AStore

  []

  (Stores/current))



(defn local-set

  ""

  ^AStore

  [db]

  (Stores/setCurrent db)
  db)
