(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [cycle])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def ^Symbol cycle

  ""

  ($.code/symbol "*cycle*"))



(def ^Symbol dep

  ""

  ($.code/symbol "dep"))



(def ^Symbol error

  ""

  ($.code/symbol "*error*"))



(def ^Symbol file

  ""

  ($.code/symbol "*file*"))



(def ^Symbol juice-last

  ""

  ($.code/symbol "*juice.last*"))



(def ^Symbol juice-total

  ""

  ($.code/symbol "*juice.total*"))



(def ^Symbol strx

  ""

  ($.code/symbol "strx"))



(def ^Symbol trx-form

  ""

  ($.code/symbol "*trx.form*"))



(def ^Symbol trx-id

  ""

  ($.code/symbol "*trx.id*"))
