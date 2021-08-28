(ns convex.db

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Hash
                             Ref)
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



(defn create-temp

  ""


  (^EtchStore []

   (EtchStore/createTemp))


  (^EtchStore [prefix]

   (EtchStore/createTemp prefix)))
  

;;;;;;;;;; Lifecycle and miscellaneous


(defn close

  ""

  [^EtchStore db]

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

  ^ACell

  [db hash]

  (when-some [^Ref r (read-ref db
                               hash)]
    (.getValue r)))



(defn read-ref

  ""

  ^Ref

  [^EtchStore db ^Hash hash]

  (.refForHash db
               hash))



(defn read-root

  ""

  ^ACell

  [db]

  (read db
       (read-root-hash db)))



(defn read-root-hash

  ""

  ^Hash

  [^EtchStore db]

  (.getRootHash db))



(defn read-root-ref

  ""

  ^Ref

  [db]

  (read-ref db
            (read-root-hash db)))


;;;;;;;;;; Writing


(defn write

  ""

  ^Ref

  [db ^ACell x]

  (write-ref db
             (.getRef x)))



(defn write-ref

  ""

  ^Ref

  [^EtchStore db x]

  (.storeTopRef db
                x
                Ref/PERSISTED
                nil))



(defn write-root

  ""

  ^Ref

  [db ^ACell x]

  (write-root-ref db
                  (.getRef x)))



(defn write-root-hash

  ""

  ^EtchStore

  [^EtchStore db hash]

  (.setRootHash db
                hash)
  hash)

  

(defn write-root-ref

  ""

  ^Ref

  [db x]

  (let [^Ref r (write-ref db
                          x)]
    (write-root-hash db
                     (.cachedHash r))
    r))
