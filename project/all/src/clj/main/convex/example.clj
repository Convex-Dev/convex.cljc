(ns convex.example
  
  "Walking through key concepts, building an intuitive understanding of the Convex stack."

  {:author "Adam Helinksi"}

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.db   :as $.db]
            [convex.read :as $.read]
            [convex.sign :as $.sign]
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




  )
