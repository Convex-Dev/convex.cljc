(ns convex.cvm.db

  "When an Etch instance is needed by the CVM, it is retrieved from a thread-local value (see [[local]]). 

   This value can be altered using [[local-set]]. Originally, at thread initialization, it corresponds to
   the [[global]] instance common to all threads. This globale value can also be altered using [[global-set]]."

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
