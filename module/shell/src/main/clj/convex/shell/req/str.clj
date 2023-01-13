(ns convex.shell.req.str

  (:import (java.io StringWriter))
  (:require [convex.cell             :as $.cell]
            [convex.cvm              :as $.cvm]
            [convex.shell.req.stream :as $.shell.req.stream]))


;;;;;;;;;;


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
