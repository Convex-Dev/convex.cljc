(ns convex.read

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell)
           (convex.core.lang.reader AntlrReader)
           (java.io InputStream
                    Reader)
           (org.antlr.v4.runtime CharStreams)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn file

  ""

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn input-stream

  ""

  ^ACell

  [^InputStream is]

  (AntlrReader/readAll (CharStreams/fromStream is)))



(defn reader

  ""

  ^ACell

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  ""

  ^ACell

  [^String string]
           
  (AntlrReader/readAll string))
