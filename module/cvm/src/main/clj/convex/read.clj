(ns convex.read

  "Reading, parsing various kind of sources into CVX cells without any evaluation.

   Attention, currently, functions that read only one cell fail when the input contains more than one.
   In the future, behavior should be improved. For instance, consuming cells one by one from a stream.

   Also see the [[convex.write]] namespace for the opposite idea."

  {:author "Adam Helinski"}

  (:import (convex.core.data AList)
           (convex.core.lang.reader AntlrReader)
           (java.io BufferedReader
                    InputStreamReader
                    Reader)
           (org.antlr.v4.runtime CharStreams))
  (:require [clojure.java.io]))


(set! *warn-on-reflection*
      true)


(declare stream
         string)


;;;;;;;;;;


(defn file

  "Reads all cells from the given `filename` and returns them in a CVX list."

  ^AList

  [^String filename]

  (AntlrReader/readAll (CharStreams/fromFileName filename)))



(defn line

  "Reads a line from the given `java.io.BufferedReader` and parses the result as a CVX list of cells."

  [^BufferedReader buffered-reader]

  (some-> (.readLine buffered-reader)
          (string)))



(defn resource

  "Reads one cell from resource located under `path` on the classpath."

  ^AList

  [path]

  (-> path
      (clojure.java.io/resource)
      (.openStream)
      (InputStreamReader.)
      (stream)))



(defn stream

  "Reads all cells from the given `java.io.Reader` (parent class of text streams) and returns them
   in a CVX list."

  ^AList

  [^Reader reader]

  (AntlrReader/readAll (CharStreams/fromReader reader)))



(defn string

  "Reads all cells from the given `string` and returns them in a CVX list."

  ^AList

  [^String string]

  (AntlrReader/readAll string))
