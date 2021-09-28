(ns convex.read

  "Reading, parsing various kind of sources into CVX cells without any evaluation.

   Attention, currently, functions that read only one cell fail when the input contains more than one.
   In the future, behavior should be improved. For instance, consuming cells one by one from a stream.

   Also see the [[convex.write]] namespace for the opposite idea."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell
                             AList)
           (convex.core.lang.reader AntlrReader)
           (java.io BufferedReader
                    InputStreamReader
                    Reader)
           (org.antlr.v4.runtime CharStreams))
  (:require [clojure.java.io]))


(set! *warn-on-reflection*
      true)


(declare stream
         stream+
         string+)


;;;;;;;;;;


(defn file

  "Reads one cell from the given `filename`."

  ^ACell

  [^String filename]

  (AntlrReader/read (CharStreams/fromFileName filename)))



(defn file+

  "Like [[file]] but reads all available cells and returns them in a CVX list."

  ^AList

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn line+

  "Reads a line from the given `java.io.BufferedReader` and parses the result as a CVX list of cells."

  [^BufferedReader buffered-reader]

  (some-> (.readLine buffered-reader)
          string+))



(defn- -resource-reader

  ;; Turns resource path into a `Reader`.

  ^Reader

  [path]

  (-> path
      clojure.java.io/resource
      .openStream
      InputStreamReader.))



(defn resource

  "Reads one cell from resource located under `path` on the classpath."

  ^AList

  [path]

  (-> path
      -resource-reader
      stream))



(defn resource+

  "Like [[resource]] but reads all available cells and returns them in a CVX list."

  ^AList

  [path]

  (-> path
      -resource-reader
      stream+))



(defn stream

  "Reads one cell from the given `java.io.Reader` (parent class of text streams).
  
   Rest of the stream is discarded."

  ^ACell

  [^Reader reader]

  (AntlrReader/read (CharStreams/fromReader reader)))



(defn stream+

  "Like [[stream]] but reads all available text cells and returns them in a CVX list."

  ^AList

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  "Reads one cell from the given `string`."

  ^ACell

  [^String string]

  (AntlrReader/read string))



(defn string+

  "Like [[string]] but reads all available text cells and returns them in a CVX list."

  ^AList

  [^String string]

  (AntlrReader/readAll string))


;;;;;;;;;;
;;
;; Support for binary stuff removed.
;; Current utilities do not pack/unpack references since they are meant for incremental updates over the wire,
;;
;; which is not suited for any kind of local IO.
;
; 
; (defn blob
; 
;   "Reads one binary cell from the given CVX blob."
; 
;   ^ACell
; 
;   [^Blob blob]
; 
;   (Format/read blob))
; 
; 
; 
; (defn byte-buffer
; 
;   "Reads one binary cell from the given `java.nio.ByteBuffer`."
; 
;   ^ACell
; 
;   [^ByteBuffer bb]
; 
;   (Format/read bb))
; 
; 
; 
; (defn byte-buffer+
; 
;   "Like [[byte-buffer]] but reads all available cells and returns them in a CVX list."
; 
;   ^AList
; 
;   [^ByteBuffer bb]
; 
;   (loop [acc []]
;     (if (.hasRemaining bb)
;       (recur (conj acc
;                    (byte-buffer bb)))
;       ($.cell/list acc))))
; 
; 
; 
; (defn file-bin
; 
;   "Reads one binary cell from the given `filename`."
; 
;   ^ACell
; 
;   [^String filename]
; 
;   (stream-bin (FileInputStream. filename)))
; 
; 
; 
; (defn file-bin+
; 
;   "Like [[file-bin]] but reads all available binary cells and returns them in a CVX list."
; 
;   ^AList
; 
;   [^String filename]
; 
;   (stream-bin+ (FileInputStream. filename)))
; 
;
;
; (defn hex-string
; 
;   "Reads one binary cell from the given hex string."
; 
;   ^ACell
; 
;   [^String string]
; 
;   (Format/read string))
; 
;
; 
; (defn stream-bin
; 
;   "Reads one binary cell from the given `java.io.InputStream` (parent class of bin streams)..
; 
;    Binary data for the cell is prefixed with a byte size in VLC (Variable Length Encoding)."
; 
;   ;; Assumes input stream is perfect. For instance, does not check that there is enough data.
;   ;;
;   ;; Akin to [[byte-buffer]] throwing an underflow exception when data is missing.
; 
;   ^ACell
; 
;   [^InputStream input-stream]
; 
;   (let [b-first (.read input-stream)]
;     (when-not (= b-first
;                  -1)
;       (let [ba (byte-array Format/MAX_VLC_LONG_LENGTH)]
;         (loop [b b-first
;                i 0]
;           (aset-byte ba
;                      i
;                      b)
;           (if (bit-test b
;                         8)
;             (recur (.read input-stream)
;                    (inc i))
;             (byte-buffer (ByteBuffer/wrap (.readNBytes input-stream
;                                                        (Format/readVLCLong ba
;                                                                            0))))))))))
; 
; 
; 
; (defn stream-bin+
; 
;   "Like [[stream-bin]] but reads all available binary cells and returns them in a CVX list."
; 
;   ^AList
; 
;   [input-stream]
; 
;   (loop [acc []]
;     (if-some [cell (stream-bin input-stream)]
;       (recur (conj acc
;                    cell))
;       ($.cell/list acc))))
