(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [cycle])
  (:require [convex.data :as $.data]))


;;;;;;;;;;


(def ^Symbol cycle
             ($.data/symbol "*cycle*"))



(def ^Symbol dep
             ($.data/symbol "dep"))



(def ^Symbol env
             ($.data/symbol "env"))



(def ^Symbol error
             ($.data/symbol "*error*"))



(def ^Symbol file
             ($.data/symbol "*file*"))


(def ^Symbol hook-end
             ($.data/symbol "*hook.end*"))



(def ^Symbol hook-error
             ($.data/symbol "*hook.error*"))



(def ^Symbol in
             ($.data/symbol "*in*"))



(def ^Symbol line
             ($.data/symbol "line"))



(def ^Symbol out
             ($.data/symbol "*out*"))



(def ^Symbol out-err
             ($.data/symbol "*out.err*"))



(def ^Symbol result
             ($.data/symbol "*result*"))



(def ^Symbol single-run?
             ($.data/symbol "single-run?"))



(def ^Symbol sreq
             ($.data/symbol "sreq"))
