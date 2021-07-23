(ns convex.write

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ABlob
                             ACell
                             Format)
           (java.io OutputStream)
           (java.nio ByteBuffer)))


;;;;;;;;;;


(defn blob

  ""

  ^ABlob

  [^ACell cell]

  (Format/encodedBlob cell))



(defn byte-buffer

  ""

  ^ByteBuffer

  [^ACell cell]

  (Format/encodedBuffer cell))



(defn hex-string

  ""

  ^String

  [^ACell cell]

  (Format/encodedString cell))



(defn os-bin

  ""

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
