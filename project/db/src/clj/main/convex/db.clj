(ns convex.db

  ""

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

   (read ($.cvm.db/local)
         hash))


  (^ACell [db hash]

   (some-> ^Ref (read-ref db
                          hash)
           .getValue)))



(defn read-ref

  ""


  (^Ref [hash]

   (read-ref ($.cvm.db/local)
             hash))


  (^Ref [^AStore db ^Hash hash]

   (.refForHash db
                hash)))



(defn read-root

  ""


  (^ACell []

   (read-root ($.cvm.db/local)))


  (^ACell [db]

   (read db
         (read-root-hash db))))



(defn read-root-hash

  ""


  (^Hash []

   (read-root-hash ($.cvm.db/local)))


  (^Hash [^AStore db]

   (.getRootHash db)))



(defn read-root-ref

  ""


  (^Ref []

   (read-root-ref ($.cvm.db/local)))


  (^Ref [db]

   (read-ref db
             (read-root-hash db))))


;;;;;;;;;; Writing


(defn write

  ""


  (^Ref [^ACell cell]

    ;; TODO. Cannot user `ACell.createPersisted()` because of https://github.com/Convex-Dev/convex/issues/298
    ;;
    (write ($.cvm.db/local)
           cell))


  (^Ref [db ^ACell cell]

   (write-ref db
              ($.ref/create-direct cell))))



(defn write-ref

  ""


  (^Ref [ref]

   (write-ref ($.cvm.db/local)
              ref))


  (^Ref [^AStore db ^Ref ref]

   (.storeTopRef db
                 ref
                 Ref/PERSISTED
                 nil)))



(defn write-root

  ""


  (^Ref [cell]

   (write-root ($.cvm.db/local)
               cell))


  (^Ref [db ^ACell cell]

   (write-root-ref db
                   ($.ref/create-direct cell))))



(defn write-root-hash

  ""


  (^Hash [hash]

   (write-root-hash ($.cvm.db/local)
                    hash))


  (^Hash [^AStore db hash]

   (.setRootHash db
                 hash)
   hash))

  

(defn write-root-ref

  ""


  (^Ref [ref]

   (write-root-ref ($.cvm.db/local)
                   ref))


  (^Ref [db ref]

   (let [^Ref ref-2 (write-ref db
                               ref)]
     (write-root-hash db
                      (.cachedHash ref-2))
     ref-2)))
