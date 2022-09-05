(ns convex.write

  "Writing, encoding CVX cells various kind of sources.

   Binary is big-endian and text is UTF-8.

   Also see [[convex.read]] for the opposite idea."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             Strings)
           (java.io Writer)))


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

  "Prints the given `cell` as a string cell.
  
   While standard `str` is sufficient for other type of cells, this function ensures that CVX strings are escaped
   so that reading produces a CVX string as well.
  
   For instance, CVX string \"foo\" produces the following:

   | Function                  | Cell after reading | Type |
   |---------------------------|--------------------|------|
   | Clojure `str`             | `\"foo\"`          | JVM  |
   | This namespace's `string` | `\"\"(+ 1 2)\"\"`  | Cell |"

  ^String

  [^ACell cell]

  ;; Cannot use `$.cell/string` because of a circular dependency.
  ;
  (if cell
    (.print cell)
    (Strings/create "nil")))
