(ns convex.shell.req.str

  (:import (java.io BufferedReader
                    StringReader
                    StringWriter))
  (:refer-clojure :exclude [sort])
  (:require [convex.cell             :as $.cell]
            [convex.cvm              :as $.cvm]
            [convex.shell.req.stream :as $.shell.req.stream]
            [convex.std              :as $.std]))


;;;;;;;;;;


(defn sort

  [ctx [str+]]

  (or (when-not ($.std/vector? str+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Can only sort a vector")))
      ($.cvm/result-set ctx
                        ($.cell/vector
                          (map $.cell/string
                               (clojure.core/sort (map str
                                                       str+)))))))



(defn stream-in

  [ctx [id string]]

  (or (when-not ($.std/string? string)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "String input stream requires a string")))
      ($.cvm/result-set ctx
                        ($.cell/* [:stream
                                   ~(-> string
                                        (str)
                                        (StringReader.)
                                        (BufferedReader.)
                                        ($.cell/fake))
                                   ~id
                                   :string]))))



(defn stream-out

  [ctx [id]]

  ($.cvm/result-set ctx
                    ($.cell/* [:stream
                               ~($.cell/fake (StringWriter.))
                               ~id
                               :string])))


(defn stream-unwrap

  [ctx [handle]]

  ($.shell.req.stream/operation ctx
                                handle
                                #{:unwrap}
                                (fn [ctx-2 stream]
                                  ($.cvm/result-set ctx-2
                                                    ($.cell/string (str stream))))))
