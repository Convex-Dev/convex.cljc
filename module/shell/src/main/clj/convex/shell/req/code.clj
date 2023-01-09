(ns convex.shell.req.code

  (:import (convex.core.exceptions ParseException))
  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.read :as $.read]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn read+

  [ctx arg+]

  (let [src (first arg+)]
    (if ($.std/string? src)
      ($.cvm/result-set
        ctx
        (try
          (-> (first arg+)
              (str)
              ($.read/string))
          ;;
          (catch ParseException ex
            ($.cvm/exception-set ctx
                                 ($.cell/* :READER)
                                 ($.cell/string (.getMessage ex))))
          ;;
          (catch Throwable _ex
            ($.cvm/exception-set ctx
                                 ($.cell/* :READER)
                                 ($.cell/string "Unable to read string as Convex data")))))
      ($.cvm/exception-set ctx
                           ($.cell/code-std* :ARGUMENT)
                           ($.cell/string "Source to read is not a string")))))
