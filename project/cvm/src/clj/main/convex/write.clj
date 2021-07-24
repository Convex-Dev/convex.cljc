(ns convex.write

  "Writing, encoding CVX cells various kind of sources.

   Binary is big-endian and text is UTF-8.

   Also see [[convex.read]] for the opposite idea."


  {:author "Adam Helinski"}

  (:import (convex.core.data ABlob
                             ACell
                             Format)
           (java.io OutputStream
                    Writer)
           (java.nio ByteBuffer)
           (java.nio.channels Channels)))


;;;;;;;;;;


(defn blob

  "Encodes the given `cell` into a CVX blob."

  ^ABlob

  [^ACell cell]

  (Format/encodedBlob cell))



(defn byte-buffer

  "Encodes the given `cell` into a `java.nio.ByteBuffer`.
  
   Attention, for small data such as primitives, returned buffer might be read-only."

  ^ByteBuffer

  [^ACell cell]

  (Format/encodedBuffer cell))



(defn hex-string

  "Encodes the given `cell` into a hex string."

  ^String

  [^ACell cell]

  (Format/encodedString cell))



(defn stream-bin

  "Writes the given `cell` to the given `java.io.OutputStream` (parent class of binary streams)."

  ^OutputStream

  [^OutputStream output-stream ^ACell cell]

  (let [ch      (Channels/newChannel output-stream)
        bb-data (byte-buffer cell)
        n-byte  (.limit bb-data)]
    (.write ch
            (let [bb (ByteBuffer/allocate (Format/getVLCLength n-byte))]
              (Format/writeVLCLong bb
                                   n-byte)
              (.position bb
                         0)
              bb))
    (.write ch
            bb-data))
  output-stream)



(defn stream-txt

  "Writes the given `cell` to the given `java.io.Writer` (parent class of text streams)."

  ^Writer

  [^Writer writer ^ACell cell]

  (.write writer
          (str cell))
  writer)
