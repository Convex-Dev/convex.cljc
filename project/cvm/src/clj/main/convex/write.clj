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
           (java.nio ByteBuffer)))


;;;;;;;;;;


(defn blob

  "Encodes the given `cell` into a CVX blob."

  ^ABlob

  [^ACell cell]

  (Format/encodedBlob cell))



(defn byte-buffer

  "Encodes the given `cell` into a `java.nio.ByteBuffer`."

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

  [^OutputStream os ^ACell cell]

  (let [bb-data (byte-buffer cell)
        n-byte  (.limit bb-data)]
    (.write os
            (let [ba (byte-array (Format/getVLCLength n-byte))]
              (Format/writeVLCLong (ByteBuffer/wrap ba)
                                   n-byte)
              ba))
    (.write os
            (.array bb-data)
            0
            n-byte))
  os)



(defn stream-txt

  "Writes the given `cell` to the given `java.io.Writer` (parent class of text streams)."

  ^Writer

  [^Writer writer ^ACell cell]

  (.write writer
          (str cell))
  writer)
