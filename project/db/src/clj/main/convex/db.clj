(ns convex.db

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Hash
                             Ref)
           (convex.core.store AStore
                              MemoryStore
                              Stores)
           (java.io File)
           (etch EtchStore))
  (:refer-clojure :exclude [flush
                            read]))


(set! *warn-on-reflection*
      true)


(declare read-ref
         read-root-hash
         write-ref
         write-root-ref)


;;;;;;;;;; Creating new DBs


(defn create

  ""

  ^EtchStore

  [^String path]

  (EtchStore/create (File. path)))



(defn create-in-memory

  ""

  ^MemoryStore

  []

  (MemoryStore.))

  


(defn create-temp

  ""


  (^EtchStore []

   (EtchStore/createTemp))


  (^EtchStore [prefix]

   (EtchStore/createTemp prefix)))
  

;;;;;;;;;; Lifecycle and miscellaneous


(defn close

  ""

  [^AStore db]

  (.close db)
  nil)



(defn flush

  ""

  ^EtchStore

  [^EtchStore db]

  (.flush db)
  db)



(defn path

  ""

  [^EtchStore db]

  (.getFileName db))


;;;;;;;;;; Reading


(defn read

  ""

  
  (^ACell [hash]

   (read (Stores/current)
         hash))


  (^ACell [db hash]

   (some-> ^Ref (read-ref db
                          hash)
           .getValue)))



(defn read-ref

  ""


  (^Ref [hash]

   (read-ref (Stores/current)
             hash))


  (^Ref [^AStore db ^Hash hash]

   (.refForHash db
                hash)))



(defn read-root

  ""


  (^ACell []

   (read-root (Stores/current)))


  (^ACell [db]

   (read db
         (read-root-hash db))))



(defn read-root-hash

  ""


  (^Hash []

   (read-root-hash (Stores/current)))


  (^Hash [^AStore db]

   (.getRootHash db)))



(defn read-root-ref

  ""


  (^Ref []

   (read-root-ref (Stores/current)))


  (^Ref [db]

   (read-ref db
             (read-root-hash db))))


;;;;;;;;;; Writing


(defn write

  ""


  (^Ref [^ACell cell]

   (ACell/createPersisted cell))


  (^Ref [db ^ACell cell]

   (write-ref db
              (Ref/get cell))))



(defn write-ref

  ""


  (^Ref [^Ref ref]

   (.persist ref))


  (^Ref [^AStore db ref]

   (.storeTopRef db
                 ref
                 Ref/PERSISTED
                 nil)))



(defn write-root

  ""


  (^Ref [cell]

   (write-root (Stores/current)
               cell))


  (^Ref [db ^ACell cell]

   (write-root-ref db
                   (Ref/get cell))))



(defn write-root-hash

  ""


  (^Hash [hash]

   (write-root-hash (Stores/current)
                    hash))


  (^Hash [^AStore db hash]

   (.setRootHash db
                 hash)
   hash))

  

(defn write-root-ref

  ""


  (^Ref [ref]

   (write-root-ref (Stores/current)
                   ref))


  (^Ref [db ref]

   (let [^Ref ref-2 (write-ref db
                               ref)]
     (write-root-hash db
                      (.cachedHash ref-2))
     ref-2)))
