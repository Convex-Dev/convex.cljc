(ns convex.shell.fail

  (:import (convex.core.lang.impl ErrorValue)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute))
  (:require [clojure.java.io :as java.io]
            [clojure.pprint  :as pprint]
            [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [convex.std      :as $.std]))


;;;;;;;;;;


(defn mappify-cvm-ex

  "Transforms the given CVM exception into a map.
  
   If prodived, associates to the resulting error map a [[phase]] and the current transaction that caused this error."


  [^ErrorValue ex]

  (-> ($.cell/error (.getCode ex)
                    (.getMessage ex)
                    ($.cell/vector (.getTrace ex)))
      ($.std/assoc ($.cell/* :address)
                   (.getAddress ex))))



(defn top-exception

  "Called when a JVM exception is caught at the very top level of the shell.
   No `env` is available at that point. This is last resort."

  [ctx ex]

  (let [path (str (Files/createTempFile "convex-shell-fatal-"
                                        ".edn"
                                        (make-array FileAttribute
                                                    0)))]
    (with-open [writer (java.io/writer path)]
      (pprint/pprint {:convex.shell/exception (Throwable->map ex)}
                     writer))
    ($.cvm/exception-set ctx
                         ($.cell/* :SHELL.FATAL)
                         ($.cell/* {:report ~($.cell/string path)}))))
