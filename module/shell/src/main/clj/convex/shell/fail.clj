(ns convex.shell.fail

  "Helpers for handling Shell failures."

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

  "Transforms the given CVM exception into a CVX map."

  [^ErrorValue ex]

  (-> ($.cell/error (.getCode ex)
                    (.getMessage ex)
                    ($.cell/vector (.getTrace ex)))
      ($.std/assoc ($.cell/* :address)
                   (.getAddress ex))))



(defn top-exception

  "Called when an unforeseen JVM exception is caught.
   Prints the exception to a tmp EDN file the user can inspect and
   report as this would be almost certainly about an actual bug in
   the Shell."

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
