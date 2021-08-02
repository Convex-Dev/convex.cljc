(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [list])
  (:require [convex.cell :as $.cell]))


;;;;;;;;;;


(def ^Symbol $
             ($.cell/symbol "$"))



(def ^Symbol $-account
             ($.cell/symbol "$.account"))



(def ^Symbol $-catch
             ($.cell/symbol "$.catch"))



(def ^Symbol $-doc
             ($.cell/symbol "$.doc"))



(def ^Symbol $-file
             ($.cell/symbol "$.file"))



(def ^Symbol $-help
             ($.cell/symbol "$.help"))



(def ^Symbol $-log
             ($.cell/symbol "$.log"))


(def ^Symbol $-process
             ($.cell/symbol "$.process"))



(def ^Symbol $-repl
             ($.cell/symbol "$.repl"))



(def ^Symbol $-stream
             ($.cell/symbol "$.stream"))



(def ^Symbol $-time
             ($.cell/symbol "$.time"))



(def ^Symbol $-term
             ($.cell/symbol "$.term"))



(def ^Symbol $-test
             ($.cell/symbol "$-test"))



(def ^Symbol $-trx
             ($.cell/symbol "$.trx"))



(def ^Symbol dep
             ($.cell/symbol "dep"))



(def ^Symbol env
             ($.cell/symbol "env"))



(def ^Symbol err
             ($.cell/symbol "*err*"))



(def ^Symbol line
             ($.cell/symbol "line"))



(def ^Symbol list
             ($.cell/symbol "*list*"))



(def ^Symbol result
             ($.cell/symbol "*result*"))



(def ^Symbol sreq
             ($.cell/symbol "sreq"))



(def ^Symbol stack
             ($.cell/symbol "*stack*"))



(def ^Symbol version
             ($.cell/symbol "version"))
