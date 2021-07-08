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



(def ^Symbol error
             ($.data/symbol "*error*"))



(def ^Symbol file
             ($.data/symbol "*file*"))



(def ^Symbol juice-total
             ($.data/symbol "*juice*"))



(def ^Symbol sreq
             ($.data/symbol "sreq"))



(def ^Symbol trx-form
             ($.data/symbol "*trx.form*"))



(def ^Symbol trx-id
             ($.data/symbol "*trx.id*"))



(def ^Symbol trx-last-form
             ($.data/symbol "*trx.last.form*"))



(def ^Symbol trx-last-id
             ($.data/symbol "*trx.last.id*"))



(def ^Symbol trx-last-juice
             ($.data/symbol "*trx.last.juice*"))



(def ^Symbol trx-last-result
             ($.data/symbol "*trx.last.result*"))
