(ns convex.recipe.db

  "This example showcases how to use Etch, the fast database used on tbe Convex network to store cells.

   Etch is a key-value store were:

   - Values are cells of any kind, even Convex functions
   - Keys are the hashes of the encodings of the cells they point to

   It is efficient, specially built for Convex cells, and has structural sharing just like Clojure data structures.

   Connaisseurs will recognize that Etch is a Merkle DAG.

   Also see `convex.recipe.cells`."

  {:author "Adam Helinski"}

  (:require [convex.cell   :as $.cell]
            [convex.cvm.db :as $.cvm.db]
            [convex.db     :as $.db]
            [convex.ref    :as $.ref]))


;;;;;;;;;;


(def db

  "A database instance is simply a file.

   Memory-mapped IO makes this database super fast."

  ($.db/open "private/recipe/db/my-instance.etch"))


;;;;;;;;;;


(comment


  ;; Many utilities, such as the client shown in `client.recipe.client`, need Etch.
  ;;
  ;; Those utilities look for an instance in a thread-local value, a value that can be
  ;; different for every thread.
  ;;
  ;; Hence, when using an instance directly, it must be set on the current thread to avoid
  ;; any problem.
  ;;
  ;; Not doing so will often result in `MissingDataException`s.
  ;;
  ($.cvm.db/local-set db)


  ;; However, instances are perfectly fine for multithreading!
  ;;
  ;; It is okay setting a default instance for threads instead of setting it explicitly for each
  ;; and every thread.
  ;;
  ($.cvm.db/global-set db)


  ;;
  ;; By default, a temporary file is created.
  ;;
  ;; Utilities use that temporary file unless `local-set` or `global-set` has been used.
  ;;


  ;; Cell to store. Could be any Convex cell at all.
  ;;
  (def my-cell
       ($.cell/* [:a :b 42]))


  ;; Writing this cell to the thread-local instance.
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
  ;; There is only one such slot. We keep at the root what we need to retrieve easily. It is typically
  ;; used as a kind of index.
  ;;
  ;; For instance, peer keep at the root all the transactions they have seen and
  ;; all states they have processed.
  ;;
  ($.db/write-root my-cell)


  ;; Now we can retrieve the root directly.
  ;;
  (= my-cell
     ($.db/read-root))


  ;; In reality, Etch deals with 'references'.
  ;;
  ;; Many types are implemented with soft references.
  ;;
  ;; A soft reference keeps a hash and caches the cell corresponding to this hash. Cell can be released during garbage collection.
  ;; When needed, it can always be retrieved from the database using its hash.
  ;; 
  ;; This is important: it means that we can use collections that are far greater than available memory!
  ;; Another interesting aspect about Convex types.
  ;;
  ($.db/read-ref ($.cell/hash my-cell))


  ;; Resolving a ref.
  ;;
  ;; This is what higher level functions like `$.db/read` do.
  ;;
  (= my-cell
     ($.ref/resolve *1))


  ;; When done, we can close the instance file.
  ;;
  ($.db/close db)


  )
