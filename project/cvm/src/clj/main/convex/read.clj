(ns convex.read

  "Reading, parsing various kind of sources into CVX cells without any evaluation.

   Binary must be big-endian and text must be UTF-8.

   Attention, currently, function that read only one cell fail if the input contains more than one.
   In the future, behavior should be improved. For instance, consuming cells one by one from a stream.

   Also see [[convex.write]] for the opposite idea."

  {:author "Adam Helinski"}

  (:require [convex.data :as $.data])
  (:import (convex.core.data ACell
                             AList
                             Blob
                             Format)
           (convex.core.lang.reader AntlrReader)
           (java.io BufferedReader
                    FileInputStream
                    InputStream
                    Reader)
           (java.nio ByteBuffer)
           (org.antlr.v4.runtime CharStreams)))


(set! *warn-on-reflection*
      true)


(declare stream-bin
         stream-bin+
         string+)


;;;;;;;;;;


(defn blob

  "Reads one binary cell from the given CVX blob."

  ^ACell

  [^Blob blob]

  (Format/read blob))



(defn byte-buffer

  "Reads one binary cell from the given `java.nio.ByteBuffer`."

  ^ACell

  [^ByteBuffer bb]

  (Format/read bb))



(defn byte-buffer+

  "Like [[byte-buffer]] but reads all available cells and returns them in a CVX list."

  ^AList

  [^ByteBuffer bb]

  (loop [acc []]
    (if (.hasRemaining bb)
      (recur (conj acc
                   (byte-buffer bb)))
      ($.data/list acc))))



(defn file-bin

  "Reads one binary cell from the given `filename`."

  ^ACell

  [^String filename]

  (stream-bin (FileInputStream. filename)))



(defn file-bin+

  "Like [[file-bin]] but reads all available binary cells and returns them in a CVX list."

  ^AList

  [^String filename]

  (stream-bin+ (FileInputStream. filename)))



(defn file-txt

  "Reads one text cell from the given `filename`."

  ^ACell

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn file-txt+

  "Like [[file-txt]] but reads all available cells and returns them in a CVX list."

  ^AList

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn hex-string

  "Reads one binary cell from the given hex string."

  ^ACell

  [^String string]

  (Format/read string))



(defn line

  "Reads a line from the given `java.io.BufferedReader` and parses the result as a CVX list of cells."

  [^BufferedReader buffered-reader]

  (some-> (.readLine buffered-reader)
          string+))



(defn stream-bin

  "Reads one binary cell from the given `java.io.InputStream` (parent class of bin streams)..

   Binary data for the cell is prefixed with a byte size in VLC (Variable Length Encoding)."

  ;; Assumes input stream is perfect. For instance, does not check that there is enough data.
  ;;
  ;; Akin to [[byte-buffer]] throwing an underflow exception when data is missing.

  ^ACell

  [^InputStream input-stream]

  (let [b-first (.read input-stream)]
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
            (recur (.read input-stream)
                   (inc i))
            (byte-buffer (ByteBuffer/wrap (.readNBytes input-stream
                                                       (Format/readVLCLong ba
                                                                           0))))))))))



(defn stream-bin+

  "Like [[stream-bin]] but reads all available binary cells and returns them in a CVX list."

  ^AList

  [input-stream]

  (loop [acc []]
    (if-some [cell (stream-bin input-stream)]
      (recur (conj acc
                   cell))
      ($.data/list acc))))



(defn stream-txt

  "Reads one text cell from the given `java.io.Reader` (parent class of text streams)."

  ^ACell

  [^Reader reader]

  (AntlrReader/read (CharStreams/fromReader reader)))



(defn stream-txt+

  "Like [[stream-txt]] but reads all available text cells and returns them in a CVX list."

  ^AList

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  "Reads one cell text from the given `string`."

  ^ACell

  [^String string]

  (AntlrReader/read string))



(defn string+

  "Like [[string]] but reads all available text cells and returns them in a CVX list."

  ^AList

  [^String string]

  (AntlrReader/readAll string))
