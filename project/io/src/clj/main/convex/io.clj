(ns convex.io

  "Basic IO utilities."

  {:author "Adam Helinski"}

  (:import (java.io File
                    FileDescriptor
                    FileInputStream
                    FileOutputStream
                    FileReader
                    FileWriter
                    Flushable
                    Writer))
  (:refer-clojure :exclude [flush
                            newline]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; STDIO streams


(def ^FileDescriptor stderr

  ""

  FileDescriptor/err)



(def ^FileOutputStream stderr-bin

  ""

  (FileOutputStream. stderr))



(def ^FileWriter stderr-txt

  ""

  (FileWriter. stderr))



(def ^FileDescriptor stdin

  ""

  FileDescriptor/in)



(def ^FileInputStream stdin-bin

  ""

  (FileInputStream. stdin))



(def ^FileReader stdin-txt

  ""

  (FileReader. FileDescriptor/in))



(def ^FileDescriptor stdout

  ""

  FileDescriptor/out)



(def ^FileOutputStream stdout-bin

  ""

  (FileOutputStream. stdout))



(def ^FileWriter stdout-txt

  ""
  
  (FileWriter. FileDescriptor/out))


;;;;;;;;;; Opening files


(defn file-in

  ""

  [^String path]

  (FileReader. (File. path)))



(defn file-out

  ""

  [^String path]

  (let [file (File. path)]
    (-> file
        .getParentFile
        .mkdirs)
    (FileWriter. file)))
  

;;;;;;;;;; Miscellaneous operations


(defn flush

  "Flushes the given `out`."

  [^Flushable out]

  (.flush out)
  out)



(defn newline

  ""

  [^Writer out]

  (.write out
          (System/lineSeparator)))
