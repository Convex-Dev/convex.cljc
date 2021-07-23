(ns convex.read

  "Reading Convex Lisp source and transforming it into Convex forms."

  {:author "Adam Helinski"}

  (:import (convex.core.data Blob
                             ACell
                             Format)
           (convex.core.lang.reader AntlrReader)
           (java.io InputStream
                    Reader)
           (java.nio ByteBuffer)
           (org.antlr.v4.runtime CharStreams)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; ANTLR Reader


(defn file

  "Reads the file under the given `filename`."

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn input-stream

  "Reads the given `java.io.InputStream`."

  ^ACell

  [^InputStream is]

  (AntlrReader/readAll (CharStreams/fromStream is)))



(defn input-stream-one

  "Like [[input-stream]] but reads only one form."

  ^ACell

  [^InputStream is]

  (AntlrReader/read (CharStreams/fromStream is)))



(defn reader

  "Reads the given `java.io.Reader`"

  ^ACell

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn reader-one

  "Like [[reader]] but reads only one form."

  ^ACell

  [^Reader reader]

  (AntlrReader/read (CharStreams/fromReader reader)))



(defn string

  "Reads the given `string`."

  ^ACell

  [^String string]

  (AntlrReader/readAll string))


;;;;;;;;; Decoding


(defn blob

  ""

  ^ACell

  [^Blob blob]

  (Format/read blob))



(defn byte-buffer

  ""

  ^ACell

  [^ByteBuffer bb]

  (Format/read bb))



(defn hex-string

  ""

  ^ACell

  [^String string]

  (Format/read string))
