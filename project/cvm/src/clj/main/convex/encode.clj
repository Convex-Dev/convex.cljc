(ns convex.encode

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data ABlob
                             ACell
                             Format)
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
