(ns convex.shell.req.reader
  
  "Requests relating to the CVX reader."

  {:author "Adam Helinski"}

  (:import (convex.core.exceptions ParseException))
  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.read :as $.read]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn form+

  "Request for reading cells from a string."

  [ctx [src]]

  (or (when-not ($.std/string? src)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/string "Source to read is not a string")))
      (try
        ($.cvm/result-set ctx
                          (-> src
                              (str)
                              ($.read/string)))
        ;;
        (catch ParseException ex
          ($.cvm/exception-set ctx
                               ($.cell/* :READER)
                               ($.cell/string (.getMessage ex))))
        ;;
        (catch Throwable _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :READER)
                               ($.cell/string "Unable to read string as Convex data"))))))
