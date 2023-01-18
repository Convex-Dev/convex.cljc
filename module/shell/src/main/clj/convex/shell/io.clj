(ns convex.shell.io

  "Basic IO utilities and STDIO."

  {:author "Adam Helinski"}

  (:import (java.io BufferedReader
                    File
                    FileDescriptor
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

  "File descriptor for STDERR."

  FileDescriptor/err)



(def ^FileWriter stderr-txt

  "Text stream for STDERR."

  (FileWriter. stderr))



(def ^FileDescriptor stdin

  "File descriptor for STDIN."

  FileDescriptor/in)



(def ^FileReader stdin-txt

  "Text stream for STDIN."

  (BufferedReader. (FileReader. stdin)))



(def ^FileDescriptor stdout

  "File descriptor for STDOUT."

  FileDescriptor/out)



(def ^FileWriter stdout-txt

  "Text stream for STDOUT."
  
  (FileWriter. FileDescriptor/out))


;;;;;;;;;; Opening files


(defn file-in

  "Opens an input text stream for the file located under `path`."

  [^String path]

  (BufferedReader. (FileReader. (File. path))))



(defn file-out

  "Opens an output text stream for the file located under `path`.
   By default, overwrites any existing file. Writes will be appended to the end
   if `append?` is true."

  
  ([path]

   (file-out path
             false))


  ([^String path append?]

   (let [file (File. path)]
     (some-> (.getParentFile file)
             (.mkdirs))
     (FileWriter. file
                  (boolean append?)))))
  

;;;;;;;;;; Miscellaneous operations


(defn flush

  "Flushes the given `out`."

  [^Flushable out]

  (.flush out)
  out)



(defn newline

  "Writes a new line to the given text output stream."

  [^Writer out]

  (.write out
          (System/lineSeparator)))
