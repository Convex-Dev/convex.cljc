# `:project/db`

Peers use the Etch database, an immutable database specially crafted for efficiently storing cells. While a database can sometimes
be provided in options (eg. when creating a peer server in [`:project/net`](../net)), such as database is also useful for other
endeavours. It is fast by leveraging memory-mapped IO.

Cells are immutable and akin to the kind of ideas found in Clojure (eg. immutable persistent data structures). More complex cells
are always backed by an Etch database which is created by default. This is why a large structure, like a very large map, can be larger
than available memory: it uses a system of soft references released when memory becomes scarce ; if needed, a reference can always
be read back from disk but the process is mostly transparent.

An Etch database can be understood as a map of `hash of the encoding of cell` -> `cell`. Hence, it is [content addressable](https://en.wikipedia.org/wiki/Content-addressable_storage).
It also stores a root hash that can be easily retrieved. For instance, peers store at root some information about the network, notably a 
vector of all the network states it has encountered, allowing for time travel.


## Using a database

Namespace `convex.db` offers an API for creating and managing databases.

Creating one:

```clojure
;; By pointing to a file.
;;
(def db
     ($.db/open "path/to/file.etch"))


;; By creating a temp file, for dev and test:
;;
(def db
     ($.db/open-temp))
```

This value can be provided as option wherever needed.

The rest of the API offers utilities for using the database directly. The system of soft references described above works
on a thread-per-thread basis. Hence, to work smoothly, it has to be set using the `convex.cvm.db` namespace:

```clojure
;; Sets database for the current thread
;;
($.cvm.db/local-set db)


;; Sets database globally as default
;;
($.cvm.db/global-set db)
```

Writing and reading a cell (created using namespace `convex.cell` from [`:project/cvm`](../cvm)). Thread-local database is
used by default:

```clojure
(def my-vector
     ($.cell/vector [($.cell/long 42)]))

($.db/write my-vector)

($.db/read ($.cell/hash my-vector))

;; -> [42] as CVM cell
```

Writing and reading a root value is just as easy:

```clojure
($.db/write-root my-vector)

($.db/read-root my-vector)

;; -> [42] as CVM cell
```

Once in a while it is a good idea to flush the database to ensure that changes are fully persisted:

```clojure
($.db/flush)
```

The following macro helps when working with a database only temporarily by setting it as thread-local and then restoring
the original:

```clojure
($.cvm.db/local-with db
  ($.db/write-root my-vector)
  ($.db/read-root))
```

The API provides more fine-graind utilities, for instance working directly with references.


## Understanding references

Namespace `convex.ref` exposes refs in greater details.

Understanding and working with those is more advanced but it helps in understanding how the CVM and Etch work together, explaining
the very good performance of the Convex network. If [pointers](https://en.wikipedia.org/wiki/Pointer_(computer_programming)) are
addresses pointing to an area of memory containing data, a **soft ref** is a hash pointing to a cell in an Etch database. it caches
the cell it points to, but it memory becomes scarce, cell is released. When dereferenced, a soft ref retrieves its cell from the
current thread-local database if necessary.
