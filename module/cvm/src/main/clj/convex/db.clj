(ns convex.db

  "Etch is a fast, immutable, embedded database tailored for cells.

   It can be understood as a data store where keys are hashes of the cells they point to.
   Hence, the API is pretty simple. [[read]] takes the hash of a cell and returns the cell
   (if present). [[write]] takes a cell and returns a new version of that cell with some
   internals updated.
   
   Most of the time, usage is made even simpler by using [[root-write]] and [[root-read]] to persist
   the state of a whole application at once (only new values are effectively written).

   Data is retrieved semi-lazily. For instance, in the case of a large vector, only the 
   the top structure of that vector is fetched. Elements are read from disk when actually accessed
   then cached using a clever system of soft references under the hood. This explains why data
   larger than memory can be retrieved and handled since the JVM can garbage-collect those soft
   references ; their value will be read from disk again if required. Nonetheless, users only deal
   with cells and all this process is completely transparent.

   Attention, although this namespace is straightforward, one rule must be followed at all time:
   cells read from an instance can only be written back to that instance. In other words, one
   must never mix cells read from different instances with the intent of writing them anywhere.
   This will result in some of the data not being written. Everything, from cells to Etch, has
   been heavily optimized for Convex peers that only ever handle 1 instance at a time. It is
   fine using several stores in the same process as long as operations never cross-over.

   Convex tooling, whenever an instance is needed, will always look for the instance associated 
   with the current thread (if any). The typical workflow is to call [[current-set]] after [[open]].
   If no instance is bound to the current thread explicitely, a temporary one is created whenever needed.
   See [[global-set]] for improving the workflow when an instance is needed in more than one thread.
  
   When using a [[convex.cvm/ctx]], its state is initially hold in memory. After opening an Etch
   instance and setting it as thread-local, this state can be retrieved at any point using [[convex.cvm/state]]
   and persisted to disk since it is a cell. This renders that state garbage-collecteable as exposed
   above. Of course, it is important not to close the instance before stopping all operations on that
   context and its state."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Ref
                             Hash)
           (convex.core.store Stores)
           (java.io File)
           (etch EtchStore))
  (:refer-clojure :exclude [flush
                            read]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn current

  "Returns the thread-local instance (or nil).
   See [[current-set]]."

  ^EtchStore

  []

  (Stores/current))



(defn current-set

  "Binds the given `instance` to the current thread.
   Returns the `instance`.
   See [[current]]."

  ^EtchStore

  [instance]

  (Stores/setCurrent instance)
  instance)



(defn global-set

  "When an instance is used in more than one thread, it is a good idea using this function.
   Convex tooling will then use the given `instance` in all thread automatically where no store
   has been initialized yet.
  
   Setting a store global will **not** impact threads which already started handling an instance.
   Hence, this function is best used when one needs only one store throught the lifetime of the
   process, preferably setting it at the beginning."

  ^EtchStore

  [instance]

  (Stores/setGlobalStore instance)
  instance)


;;;;;;;;;; Lifecycle


(defn close

  "Flushes and closes the thread-local instance. Also unbinds it from the current thread.
   
   Note that all instances are also cleanly closed on JVM shutdown but it is
   more predictable doing it manually."

  []

  (.close (current))
  (current-set nil)
  nil)



(defn flush

  "Flushes the thread-local instance, ensuring all changes are persisted to disk."

  ^EtchStore

  []

  (.flush (current))
  nil)



(defn open

  "Opens an instance at the given `path`.
   File is created if needed."

  ^EtchStore

  [^String path]

  (let [^File file (File. path)]
    (-> file
        (.getParentFile)
        (.mkdirs))
    (EtchStore/create file)))



(defn open-tmp

  "Opens an instance under a temporary file.

   A prefix string may be provided for the filename."


  (^EtchStore []

   (EtchStore/createTemp))


  (^EtchStore [prefix]

   (EtchStore/createTemp prefix)))



(defn path

  "Returns the path of the thread-local instance."

  []

  (.getFileName (current)))



(defn size

  "Returns the size in bytes of the thread-local instance.
  
   Etch always reserves some extra space in its instance file.
   The returned value is the size of the actual data, without any extra space."

  []

  (.getDataLength (.getEtch (current))))


;;;;;;;;;; R\W


(defn read

  "Reads from the thread-local instance and returns the cell for the given `hash` (or nil
   if not found)."
  
  ^ACell
  
  [^Hash hash]

  (some-> (.refForHash (current)
                       hash)
          (.getValue)))



(defn write

  "Writes the given `cell` to the thread-local instance and returns a new \"version\" of that cell.

   If the cell is needed for more work, the old version should be discarded in favor of that new
   version. This allows for transparent garbage-collection (see namespace description) while acting
   as an optimization during subsequent writes (e.g. an already persisted cell is put in a collection
   that is in turn persisted).

   Very basic cell types are not persisted because that would be inefficient and hardly ever happens.
   They are typically embedded in collections. Hence, this function will return `nil` for:

     - Address
     - Empty collections
     - Primitives (boolean, byte, double, long)
     - Symbolic (keywords and symbols)"

  ^ACell

  [^ACell cell]

  (let [^Ref r (ACell/createPersisted (.getValue (.getRef cell)))]
    (when (.cachedHash r)
      (.getValue r))))


;;;


(defn root-read

  "Returns the cell stored at the root of the thread-local instance.

   The root is a place in the instance that can be read without providing a hash. It is commonly
   used for storing the whole state of an application or at least some sort of index containing
   hashes of other data in the instance. This makes Etch self-sufficient as no hash must be stored
   externally.

   See [[write-root]]."

  ^ACell

  []

  (read (.getRootHash (current))))



(defn root-write

  "Exactly like [[write]] but the `cell` is written to the root of the thread-local instance.

   See [[read-root]]."

  ^ACell

  [^ACell cell]

  (let [^Ref r (ACell/createPersisted (.getValue (.getRef cell)))
             h (.cachedHash r)]
    (when h
      (.setRootHash (.getEtch (current))
                    h)
      (.getValue r))))
