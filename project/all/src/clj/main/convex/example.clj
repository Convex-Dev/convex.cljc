(ns convex.example
  
  "Walking through key concepts, building an intuitive understanding of the Convex stack."

  {:author "Adam Helinksi"}

  (:require [convex.cell   :as $.cell]
            [convex.cvm    :as $.cvm]
            [convex.cvm.db :as $.cvm.db]
            [convex.db     :as $.db]
            [convex.read   :as $.read]
            [convex.sign   :as $.sign]
            ))


;;;;;;;;;;


(comment


  ;; Forms should be evaluated one by one in the hope of providing a gradual understanding of the
  ;; fundamental ideas behing Convex.



  ;; DATA MODEL
  ;;
  ;; Many of the Convex data types stem from Clojure.

  ($.cell/long 42)

  ($.cell/keyword "foo")

  ($.cell/vector [($.cell/symbol "bar")
                  ($.cell/double 42.24)])

  ($.cell/map {($.cell/keyword "foo") ($.cell/long 42)
               ($.cell/keyword "bar") ($.cell/boolean true)})

  ;; Stringifying them makes it more obvious.

  (str *1)


  ;; A few other types are specific to Convex.
  
  ;; Such as addresses, denoting accounts...
  ;;
  (str ($.cell/address 42))

  ;; Or blobs, binary large objects...
  ;;
  (str ($.cell/blob (byte-array [1 2 3])))


  ;; In reality, the word "cell" is more appropriate than "data".
  ;;
  ;; In Convex, anything can be transmitted over the wire or persisted in the database, even functions.
  ;; Hence, the word "cell" in Convex has a broader scope than the word "data" in Clojure.

  
  ;; The reader is useful for dev or tests.
  ;;
  (def x
       ($.read/string "{:foo 42
                        :bar [1 'b :c]}"))


  ;; A cell can be efficiently encoded in a binary format suited for efficient incremental updates
  ;; over the network or for storing in a database.
  ;;
  ;; An encoding is returned as a blob, akin to a byte array. It is a unique representaton that can
  ;; be decoded.
  ;;
  (str ($.cell/encoding x))


  ;; A cell can also be computed down to a 32-byte hash.
  ;;
  ;; More precisely, the hash of its encoding. It cannot be decoded but its fixed width will be very important
  ;; in a moment.
  ;;
  (str ($.cell/hash x))



  ;; ETCH, A FAST IMMUTABLE DATABASE FOR CELLS
  ;;
  ;; Imagine a possibly large key-value store where:
  ;;
  ;;   - Values are cells of any kind
  ;;   - Keys are hashes of the encoding of the cells they point to

  ;; Let us store our Convex map defined under `x`
  ;;
  ($.db/write x)

  ;; It can be read back by providing its hash.
  ;;
  (= x
     ($.db/read ($.cell/hash x)))

  ;; Okay, but how do you know which hash to query? Where do you keep hashes of the things you want to to retrieve?
  ;;
  ;; In practice, some sort of state or "table of content" is maintained. That is the purpose of a "root".
  ;;
  ;; For instance, a Convex peer maintains its data in the root of the database. Things like a vector of all
  ;; the states of the network it has encountered.
  ;;
  ;; This is efficient because even when stored, cells rely on structural sharing.
  
  ;; Writing to root, could be any kind of cell, small or huge.
  ;;
  ($.db/write-root x)

  ;; And reading it back, without providing any hash or anything.
  ;;
  (= x
     ($.db/read-root))

  
  ;; In those previous examples, we were using a temporary database created by default.
  ;;
  ;; We might want to create a stable one. It's really just one file.
  ;;
  (def db
       ($.db/open "private/my-db.etch"))

  ;; To use it, it is important to set it as "thread-local".
  ;;
  ($.cvm.db/local-set db)

  ;; 







  )
