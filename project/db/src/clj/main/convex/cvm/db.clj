(ns convex.cvm.db

  "When a CVM instance is used, it relies on a thread-local database which can be manually retrieved using [[local]].
   
   The thread-local database can be set usint [[local-set]]. Originally, at thread initialization, it corresponds to
   the [[global]] database which is common to all threads. Its value can also be altered using [[global-set]].

   Ultimately, the [[global]] database itself returns [[default]] unless user has set its value to another database.

   Default [[database]] is an Etch instance. See [[convex.db]]."

  {:author "Adam Helinski"}

  (:import (convex.core.store AStore
                              Stores)))


;;;;;;;;;;


(defn default

  "Returns the default database, an Etch instance."

  ^AStore

  []

  (Stores/getDefaultStore))



(defn global

  "Returns the global database."

  ^AStore

  []

  (Stores/getGlobalStore))



(defn global-set

  "Sets the global database returned by [[global]].
  
   A nil value means [[global]] will rely on [[default]]."

  ^AStore

  [db]

  (Stores/setGlobalStore db)
  db)



(defn local

  "Returns the thread-local database used by any CVM instance operating in the same thread."

  ^AStore

  []

  (Stores/current))



(defn local-set

  "Sets the thread-local databased returned by [[local]]."

  ^AStore

  [db]

  (Stores/setCurrent db)
  db)
