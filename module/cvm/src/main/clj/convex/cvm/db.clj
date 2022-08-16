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


(defn global

  "Returns the global database."

  ^AStore

  []

  (Stores/getGlobalStore))



(defn global-set

  "Sets the global database returned by [[global]]."

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

  "Sets the thread-local database returned by [[local]]."

  ^AStore

  [db]

  (Stores/setCurrent db)
  db)



(defmacro local-with

  "Macro using `local-set` momentarily to execute given forms before restoring the previous thread-local database."

  ^AStore

  [db & form+]

  `(let [db#     ~db
         cached# (local)]
     (local-set db#)
     (try
       ~@form+
       (finally
         (local-set cached#)))))
