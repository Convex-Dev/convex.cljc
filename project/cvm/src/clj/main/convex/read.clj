(ns convex.read

  "Reading CVX, parsing source into CVX forms without any evaluation.

   Forms can either be UTF-8 text or binary data (see [[convex.write]])."

  {:author "Adam Helinski"}

  (:require [convex.data :as $.data])
  (:import (convex.core.data ACell
                             AList
                             Blob
                             Format)
           (convex.core.lang.reader AntlrReader)
           (java.io FileInputStream
                    InputStream
                    Reader)
           (java.nio ByteBuffer)
           (org.antlr.v4.runtime CharStreams)))


(set! *warn-on-reflection*
      true)


(declare byte-buffer
         stream-bin
         stream-bin+)


;;;;;;;;;; ANTLR Reader


(defn file-bin

  "Reads one binary form from the given `filename`."

  ^ACell

  [^String filename]

  (stream-bin (FileInputStream. filename)))



(defn file-bin+

  "Like [[file-bin]] but reads all available binary forms and returns them in a CVX list."

  ^AList

  [^String filename]

  (stream-bin+ (FileInputStream. filename)))



(defn file-txt

  "Reads one text form from the given `filename`."

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn file-txt+

  "Like [[file-txt]] but reads all available forms and returns them in a CVX list."

  ^AList

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn stream-bin

  "Reads one binary form from the given `java.io.InputStream` (parent class of bin streams)..

   Binary data for the form is prefixed with a byte size in VLC (Variable Length Encoding)."

  ;; Assumes input stream is perfect. For instance, does not check that there is enough data.
  ;;
  ;; Akin to [[byte-buffer]] throwing an underflow exception when data is missing.

  ^ACell

  [^InputStream is]

  (let [b-first (.read is)]
    (when-not (= b-first
                 -1)
      (let [ba (byte-array Format/MAX_VLC_LONG_LENGTH)]
        (loop [b b-first
               i 0]
          (aset-byte ba
                     i
                     b)
          (if (bit-test b
                        8)
            (recur (.read is)
                   (inc i))
            (byte-buffer (ByteBuffer/wrap (.readNBytes is
                                                       (Format/readVLCLong ba
                                                                           0))))))))))



(defn stream-bin+

  "Like [[stream-bin]] but reads all available binary forms and returns them in a CVX list."

  ^AList

  [is]

  (loop [acc []]
    (if-some [form (stream-bin is)]
      (recur (conj acc
                   form))
      ($.data/list acc))))



(defn stream-txt

  "Reads one text form from the given `java.io.Reader` (parent class of char streams)."

  ^ACell

  [^Reader reader]

  (AntlrReader/read (CharStreams/fromReader reader)))



(defn stream-txt+

  "Like [[stream-txt]] but reads all available text forms and returns them in a CVX list."

  ^AList

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  "Reads one form text from the given `string`."

  ^ACell

  [^String string]

  (AntlrReader/read string))



(defn string+

  "Like [[string]] but reads all available text forms and returns them in a CVX list."

  ^AList

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

  ^AList

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
