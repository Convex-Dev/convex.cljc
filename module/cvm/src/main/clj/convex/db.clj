(ns convex.db

  "Etch is a fast, immutable, append-only database specially tailored for cells.

   This namespace provides an API for creating an instance by pointing to a single file. This file
   hosts an arbitrarily large map of `hash of the encoding of a cell` -> `cell`. Hence, reading require
   hashes (see [[convex.cell/hash]]) and writes primarilty return refs or nil
   when not found (see [[convex.ref]]).

   Lastly, a root hash can be stored and retrieved. Cell stored at root is typically used to maintain
   some sort of global state or table tracking what is needed.

   **Attention.** By default, R/W functions use the current thread-local database (see [[convex.cvm.db]]).
   Providing an instance explicitly is tricky because when reading, not all data might be retrieved at once.
   This is what allows large data, even larger than memory, to be queried: large structures are split into refs
   and not all refs are necessarily resolved right away. However, any unresolved ref will be resolved against
   the current thread-local database when needed, not the one that was explicitly provided when reading.
  
   In other words, when handling a custom instance, it is best to work on a dedicated thread and call
   [[convex.cvm.db/local-set]].
  
   That being said, instances support multithreading. Being immutable, no thread has to worry that some data might
   be removed or updated in place."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Hash
                             Ref)
           (convex.core.store AStore
                              MemoryStore)
           (java.io File)
           (etch EtchStore))
  (:refer-clojure :exclude [flush
                            read])
  (:require [convex.cvm.db :as $.cvm.db]
            [convex.ref    :as $.ref]))


(set! *warn-on-reflection*
      true)


(declare read-ref
         read-root-hash
         write-ref
         write-root-ref)


;;;;;;;;;; Creating new DBs


(defn open

  "Opens a db at the given `path`:
  
   ```clojure
   (open \"some_dir/my-db\")
   ```
  
   File is created if needed."

  ^EtchStore

  [^String path]

  (let [^File file (File. path)]
    (-> file
        .getParentFile
        .mkdirs)
    (EtchStore/create file)))



(defn open-in-memory

  "Alternatively, an in-memory db can be used for some use cases.
  
   However, [[open]] is typically recommended."

  ^MemoryStore

  []

  (MemoryStore.))

  


(defn open-temp

  "Like [[open]] but works with a temporariy file.
  
   A prefix string may be provided."


  (^EtchStore []

   (EtchStore/createTemp))


  (^EtchStore [prefix]

   (EtchStore/createTemp prefix)))
  

;;;;;;;;;; Lifecycle and miscellaneous


(defn close

  "Closes the given `db`."


  ([^AStore db]

   (.close db)
   nil))



(defn flush

  "Flushes the given `db`, ensuring all changes are persisted to disk.
  
   Does not work with in-memory instances."


  (^EtchStore []

   (flush ($.cvm.db/local)))


  (^EtchStore [^EtchStore db]

   (.flush db)
   db))



(defn path

  "Returns the path of the given `db`.
  
   Does not work with in-memory instances."


  ([]

   (path ($.cvm.db/local)))


  ([^EtchStore db]

   (.getFileName db)))


;;;;;;;;;; Reading


(defn read

  "Like [[read-ref]] but ref is directly resolved to its cell.
  
   Convenient, but returns nil both when not found or when the stored cell is nil."

  
  (^ACell [hash]

   (read ($.cvm.db/local)
         hash))


  (^ACell [db hash]

   (some-> ^Ref (read-ref db
                          hash)
           .getValue)))



(defn read-ref

  "Given a `hash`, reads a ref to a cell stored in the `db`, or nil if not found."


  (^Ref [hash]

   (read-ref ($.cvm.db/local)
             hash))


  (^Ref [^AStore db ^Hash hash]

   (.refForHash db
                hash)))



(defn read-root

  "Like [[read-root-ref]] but ref is directly resolved to its cell, akin to [[read]]."


  (^ACell []

   (read-root ($.cvm.db/local)))


  (^ACell [db]

   (read db
         (read-root-hash db))))



(defn read-root-hash

  "Reads the hash stored as root."


  (^Hash []

   (read-root-hash ($.cvm.db/local)))


  (^Hash [^AStore db]

   (.getRootHash db)))



(defn read-root-ref

  "Like [[read-ref]] but uses the hash from [[read-root-hash]]."


  (^Ref []

   (read-root-ref ($.cvm.db/local)))


  (^Ref [db]

   (read-ref db
             (read-root-hash db))))


;;;;;;;;;; Writing


(defn write

  "Wraps `cell` in a ref and calls [[write-ref]]."


  (^Ref [^ACell cell]

    ;; TODO. Cannot user `ACell.createPersisted()` because of https://github.com/Convex-Dev/convex/issues/298
    ;;
    (write ($.cvm.db/local)
           cell))


  (^Ref [db ^ACell cell]

   (write-ref db
              ($.ref/create-direct cell))))



(defn write-ref

  "Given a ref, writes its associated cell."


  (^Ref [ref]

   (write-ref ($.cvm.db/local)
              ref))


  (^Ref [^AStore db ^Ref ref]

   (.storeTopRef db
                 ref
                 Ref/PERSISTED
                 nil)))



(defn write-root

  "Wraps `cell` in a ref and calls [[write-root-ref]]."


  (^Ref [cell]

   (write-root ($.cvm.db/local)
               cell))


  (^Ref [db ^ACell cell]

   (write-root-ref db
                   ($.ref/create-direct cell))))



(defn write-root-hash

  "Writes the given `hash` as root."


  (^Hash [hash]

   (write-root-hash ($.cvm.db/local)
                    hash))


  (^Hash [^AStore db hash]

   (.setRootHash db
                 hash)
   hash))

  

(defn write-root-ref

  "Writes `ref` and then sets its hash as root."


  (^Ref [ref]

   (write-root-ref ($.cvm.db/local)
                   ref))


  (^Ref [db ref]

   (let [^Ref ref-2 (write-ref db
                               ref)]
     (write-root-hash db
                      (.cachedHash ref-2))
     ref-2)))
