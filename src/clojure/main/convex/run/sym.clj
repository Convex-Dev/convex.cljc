(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [cycle])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def catch

  ""

  ($.code/symbol "cvm.catch"))



(def cvm

  ""

  ($.code/symbol "cvm"))



(def cycle

  ""

  ($.code/symbol "*cvm.cycle*"))



(def error

  ""

  ($.code/symbol "*cvm.error*"))



(def juice-last

  ""

  ($.code/symbol "*cvm.juice.last*"))



(def trx-id

  ""

  ($.code/symbol "*cvm.trx.id*"))
