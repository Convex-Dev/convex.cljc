(ns convex.recipe.db

  "These examples showcase how to use Etch, the fast and immutable database for Convex cells, notably used
   by peers of the Convex network.
   

   Etch is a key-value store were:

   - Values are cells of any kind, even compiled Convex functions
   - Keys are the hashes of the encodings of the cells they point to

   It is efficient, specially built for Convex cells, and has structural sharing just like Clojure data structures.

   Connaisseurs will recognize that Etch is a Merkle DAG.

   See [[convex.recipe.cell]] first for learning about cells."

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.db   :as $.db]))


;;;;;;;;;;


(comment


  (def instance
  
    "A database instance is simply a file.
  
     Memory-mapped IO makes this database super fast."
  
    ($.db/open "private/recipe/db/my-instance.etch"))


  ;; Data from Etch is loaded semi-lazily, meaning that for larger structures, not all data is
  ;; retrieved at once.
  ;;
  ;; This is a desirable property because it allows us to work with data that is even larger than
  ;; available memory. Missing values are transparently queried from the database when they are
  ;; actually used, via a system of soft references.
  ;;
  ;; When a missing value must be read from the database, the instance used is the one currently
  ;; attached to a thread-local value.
  ;;
  ;; Hence it is important to set our instance as "thread-local". Not doing so will often result
  ;; in `MissingDataException`.
  ($.db/current-set instance)

  (= instance
     ($.db/current))


  ;; However, instances are perfectly fine for multithreading!
  ;;
  ;; If only one instance is needed for a whole application, which is typically the case, it is
  ;; often easier setting it as default for all threads.
  ;;
  ($.db/global-set instance)

  
  ;; All functions below work with the thread-local instance they find.
  ;;
  ;; By default, a temporary file is created if no instance is set explicitly.


  ;; Cell to store. Could be any Convex cell at all.
  ;;
  (def my-cell
       ($.cell/* [:a :b 42]))


  ;; Writing this cell to the current thread-local instance.
  ;; Returns the hash of this cell.
  ;;
  ($.db/write my-cell)


  ;; Reading back this cell by providing its hash.
  ;;
  (= my-cell
     ($.db/read ($.cell/hash my-cell)))


  ;; Wait a second, in a real use case, how can get the hash of the cell if we don't have it?!
  ;;
  ;; We can write a value to the 'root' and retrieve it without providing a hash.
  ;;
  ;; There is only one such slot. We keep at the root what we need to retrieve easily. It usually
  ;; contains the state of the whole application.
  ;;
  ;; For instance, peers keep at the root all the transactions they have seen and
  ;; all states they have processed.
  ;;
  ($.db/root-write my-cell)


  ;; Now we can retrieve the root directly.
  ;;
  (= my-cell
     ($.db/root-read))


  ;; While the OS will flush writes in due time, it is a good practice doing it ourselves once in a while.
  ;;
  ($.db/flush)


  ;;
  ;; it is not crucial to understand references but this is how structural sharing is implemented: often, collections do not 
  ;; hold actual values but rather references.
  ;;
  ;; This is also why sharing over the network is fast: send a big structure and if references are missing, recipient can always
  ;; request those later.
  ;;


  ;; While opening an instance registers a shutdown hook for closing it, it is always more predictable doing it explicitly.
  ;; For the time being, an unclean shutdown will probably corrupt the instance.
  ;;
  ($.db/close)


  ;; LAST NOTE. Persisted cells cache the fact they have been persisted. One important implication is that cells from different
  ;; instances should never be mixed together with the intent of writing them back.
  ;;
  ;; For instance:
  ;;
  ;;   - Read cell A from instance A
  ;;   - Write something like `[1 2 cell-A]` to instance B
  ;;   - cell-A will silently not be persisted in instance B because it remembers being alread stored (not there though!) 
  ;;
  ;; This is notably because Etch has been very heavily optimized for peers of the Convex network that only ever deal with
  ;; one instance at a time.


  )
