(ns convex.write

  "Writing, encoding CVX cells various kind of sources.

   Binary is big-endian and text is UTF-8.

   Also see [[convex.read]] for the opposite idea."


  {:author "Adam Helinski"}

  (:import (convex.core.data ACell)
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

  "Prints the given `cell` as a string.
  
   While standard `str` is sufficient for other type of cells, this function ensures that CVX strings are escaped
   so that reading produces a CVX string as well.
  
   For instance, CVX string \"foo\" produces the following Java string:

   | Function | Cell after reading |
   |---|---|
   | Clojure `str` | `\"foo\"` |
   | This namespace's `string` | `\"\"(+ 1 2)\"\"` |"

  ^String

  [^ACell cell]

  (.print cell))


;;;;;;;;;;
;;
;; Support for binary stuff removed.
;; Current utilities do not pack/unpack references since they are meant for incremental updates over the wire,
;;
;; which is not suited for any kind of local IO.

; 
; (defn blob
; 
;   "Encodes the given `cell` into a CVX blob."
; 
;   ^ABlob
; 
;   [^ACell cell]
; 
;   (Format/encodedBlob cell))
; 
; 
; 
; (defn byte-buffer
; 
;   "Encodes the given `cell` into a `java.nio.ByteBuffer`.
;   
;    Attention, for small data such as primitives, returned buffer might be read-only."
; 
;   ^ByteBuffer
; 
;   [^ACell cell]
; 
;   (Format/encodedBuffer cell))
; 
; 
; 
; (defn hex-string
; 
;   "Encodes the given `cell` into a hex string."
; 
;   ^String
; 
;   [^ACell cell]
; 
;   (Format/encodedString cell))
; 
; 
; 
; (defn stream-bin
; 
;   "Writes the given `cell` to the given `java.io.OutputStream` (parent class of binary streams)."
; 
;   ^OutputStream
; 
;   [^OutputStream output-stream ^ACell cell]
; 
;   (let [ch      (Channels/newChannel output-stream)
;         bb-data (byte-buffer cell)
;         n-byte  (.limit bb-data)]
;     (.write ch
;             (let [bb (ByteBuffer/allocate (Format/getVLCLength n-byte))]
;               (Format/writeVLCLong bb
;                                    n-byte)
;               (.position bb
;                          0)
;               bb))
;     (.write ch
;             bb-data))
;   output-stream)
