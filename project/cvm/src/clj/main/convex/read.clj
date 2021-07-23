(ns convex.read

  "Reading CVX, parsing source into CVX forms without any evaluation.

   Forms can either be UTF-8 text or binary data (see [[convex.encode]])."

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


(defn file-txt

  "Reads one text form from the given `filename`."

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn file-txt+

  "Like [[file-txt]] but reads all available forms and returns them in a CVX list."

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn is-txt

  "Reads one form from the given `java.io.InputStream`."

  ^ACell

  [^InputStream is]

  (AntlrReader/read (CharStreams/fromStream is)))



(defn is-txt+

  "Like [[is-txt]] but reads all available forms and returns them in a CVX list."

  ^ACell

  [^InputStream is]

  (AntlrReader/readAll (CharStreams/fromStream is)))



(defn reader

  "Reads one text form from the given `java.io.Reader`."

  ^ACell

  [^Reader reader]

  (AntlrReader/read (CharStreams/fromReader reader)))



(defn reader+

  "Like [[is-txt]] but reads all available forms and returns them in a CVX list."

  ^ACell

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  "Reads one form from the given `string`."

  ^ACell

  [^String string]

  (AntlrReader/read string))



(defn string+

  "Like [[string]] but reads all available forms and returns them in a CVX list."

  ^ACell

  [^String string]

  (AntlrReader/readAll string))


;;;;;;;;; Decoding


(defn blob

  "Reads one binary form from the given CVX blob."

  ^ACell

  [^Blob blob]

  (Format/read blob))



(defn byte-buffer

  "Reads one binary form from the given `java.nio.ByteBuffer`."

  ^ACell

  [^ByteBuffer bb]

  (Format/read bb))


;(defn byte-buffer
;
;  ""
;
;  ^ACell
;
;  [^ByteBuffer bb]
;
;  (loop 



(defn hex-string

  "Reads one binary form from the given hex string."

  ^ACell

  [^String string]

  (Format/read string))
