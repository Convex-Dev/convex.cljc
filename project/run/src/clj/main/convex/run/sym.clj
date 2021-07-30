(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [cycle
                            list])
  (:require [convex.data :as $.data]))


;;;;;;;;;;


(def ^Symbol $
             ($.data/symbol "$"))



(def ^Symbol $-catch
             ($.data/symbol "$.catch"))



(def ^Symbol $-file
             ($.data/symbol "$.file"))



(def ^Symbol $-process
             ($.data/symbol "$.process"))



(def ^Symbol $-repl
             ($.data/symbol "$.repl"))



(def ^Symbol $-stream
             ($.data/symbol "$.stream"))



(def ^Symbol $-time
             ($.data/symbol "$.time"))



(def ^Symbol $-test
             ($.data/symbol "$-test"))



(def ^Symbol $-trx
             ($.data/symbol "$.trx"))



(def ^Symbol cycle
             ($.data/symbol "*cycle*"))



(def ^Symbol dep
             ($.data/symbol "dep"))



(def ^Symbol env
             ($.data/symbol "env"))



(def ^Symbol err
             ($.data/symbol "*err*"))



(def ^Symbol line
             ($.data/symbol "line"))



(def ^Symbol list
             ($.data/symbol "*list*"))



(def ^Symbol result
             ($.data/symbol "*result*"))



(def ^Symbol sreq
             ($.data/symbol "sreq"))



(def ^Symbol stack
             ($.data/symbol "*stack*"))



(def ^Symbol watch?
             ($.data/symbol "*watch?*"))
