(ns convex.read

  "Reading CVX, parsing source into CVX forms without any evaluation.

   Forms can either be UTF-8 text or binary data (see [[convex.write]])."

  {:author "Adam Helinski"}

  (:require [convex.data :as $.data])
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


(declare byte-buffer)


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



(defn is-bin

  "Reads one binary form from the given `java.io.InputStream`.
  
   Binary data for the form is prefixed with a byte size in VLC (Variable Length Encoding)."

  ;; Assumes input stream is perfect. For instance, does not check that there is enough data.
  ;;
  ;; Akin to [[byte-buffer]] throwing an underflow exception when data is missing.

  ^ACell

  [^InputStream is]

  (let [ba (byte-array Format/MAX_VLC_LONG_LENGTH)]
    (loop [i 0]
      (let [b (.read is)]
        (aset-byte ba
                   i
                   b)
        (if (bit-test b
                      8)
          (recur (inc i))
          (byte-buffer (ByteBuffer/wrap (.readNBytes is
                                                     (Format/readVLCLong ba
                                                                         0)))))))))


(defn is-txt

  "Reads one text form from the given `java.io.InputStream`."

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



(defn byte-buffer+

  "Like [[byte-buffer]] but reads all available forms and returns them in a CVX list."

  ^ACell

  [^ByteBuffer bb]

  (loop [acc []]
    (if (.hasRemaining bb)
      (recur (conj acc
                   (byte-buffer bb)))
      ($.data/list acc))))



(defn hex-string

  "Reads one binary form from the given hex string."

  ^ACell

  [^String string]

  (Format/read string))
