(ns convex.write

  "Writing CVX cells as UTF-8 text.

   Also see [[convex.read]] for the opposite idea."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AString
                             Strings)
           (java.io Writer)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn stream

  "Writes the given `cell` to the given `java.io.Writer` (parent class of text streams).

   By default, standard `str` is used for stringifying `cell`. See [[string]] for implications."


  (^Writer [writer cell]

   (stream writer
           str
           cell))


  (^Writer [^Writer writer stringify ^ACell cell]

   (.write writer
           (if (nil? cell)
             "nil"
             ^String (stringify cell)))
   writer))



(defn string

  "Prints the given `cell` as a string cell which can be read back with the [[convex.read]] namespace.

   A default limit of 10000 bytes is applied relative to the output, beyond which \"<<Print limit exceeded>>\"
   is appended. Pass `Long/MAX_VALUE` for the maximum limit."


  (^AString [cell]

   (string nil
           cell))
  

  (^AString [limit ^ACell cell]

   (if (nil? cell)
     (Strings/create "nil")
     (.print cell
             (or limit
                 10000)))))
