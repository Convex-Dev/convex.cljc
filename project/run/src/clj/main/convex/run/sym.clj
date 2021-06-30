(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [cycle])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def ^Symbol cycle
             ($.code/symbol "*cycle*"))



(def ^Symbol dep
             ($.code/symbol "dep"))



(def ^Symbol error
             ($.code/symbol "*error*"))



(def ^Symbol file
             ($.code/symbol "*file*"))



(def ^Symbol juice-total
             ($.code/symbol "*juice*"))



(def ^Symbol sreq
             ($.code/symbol "sreq"))



(def ^Symbol trx-form
             ($.code/symbol "*trx.form*"))



(def ^Symbol trx-id
             ($.code/symbol "*trx.id*"))



(def ^Symbol trx-last-form
             ($.code/symbol "*trx.last.form*"))



(def ^Symbol trx-last-id
             ($.code/symbol "*trx.last.id*"))



(def ^Symbol trx-last-juice
             ($.code/symbol "*trx.last.juice*"))



(def ^Symbol trx-last-result
             ($.code/symbol "*trx.last.result*"))
