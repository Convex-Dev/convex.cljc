(ns convex.ref

  "A `ref` is a reference to a cell. Most of the time, is it used as an intermediary value between a database
   and the CVM. Unless refs are handled in reference to an explicit database, database used when resolving is always the current
   one bound to the local thread. See [[convex.cvm.db]], [[convex.db]].

   A **direct ref** holds a direct reference to a cell whereas a **soft ref** might release its reference when there is pressure
   on memory. If needed, a **soft ref** will fetch its corresponding cell from a database."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Blob
                             Hash
                             Ref
                             RefDirect
                             RefSoft))
  (:refer-clojure :exclude [hash
                            resolve]))


;;;;;;;;;; Creating refs


(defn create-direct

  "Returs a direct ref to the given `cell`."

  ^RefDirect

  [^ACell cell]

  (RefDirect/create cell))



(defn create-soft

  "Returns a soft ref to the cell which encoding hashes to the given `hash`."

  ^Ref

  [^Hash hash]

  (RefSoft/createForHash hash))


;;;;;;;;;; Predicates


(defn direct?

  "Is the given `ref` a direct ref?"

  [^Ref ref]

  (.isDirect ref))



(defn embedded?

  "Is the given `ref` embedded, meaning its encoding self contained in the encoding of its parent?"

  [^Ref ref]

  (.isEmbedded ref))



(defn missing?

  "Is the cell for this `ref` missing, meaning its neither cached nor present in the thread-local database?"

  [^Ref ref]

  (.isMissing ref))



(defn persisted?

  "Is the given `ref` marked as persisted?"

  [^Ref ref]

  (.isPersisted ref))


;;;;;;;;;; Data


(defn encoding

  "Returns the encoding of the cell assocaited with the given `ref`.

   Reads from the thread-local database if `ref` is soft and lost its cell."

  ^Blob

  [^Ref ref]

  (.getEncoding ref))



(defn hash

  "Returns the hash of the cell associated with the given `ref`."

  ^Hash

  [^Ref ref]

  (.getHash ref))


;;;;;;;;;; Potential reads


(defn direct

  "Returns a direct ref based on the given `ref`, in case it is soft, in which case it will be resolved using
   [[resolve]]."

  ^RefDirect

  [^Ref ref]

  (.toDirect ref))



(defn resolve

  "Returns the `cell` associated with the given `ref`.

   Reads from the thread-local database if `ref` is soft and lost its cell."

  ^ACell

  [^Ref ref]

  (.getValue ref))
