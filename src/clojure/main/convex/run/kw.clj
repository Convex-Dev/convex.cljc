(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [read])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def cause

  ""

  ($.code/keyword "cause"))



(def exception?

  ""

  ($.code/keyword "exception?"))



(def expand

  ""

  ($.code/keyword "expand"))



(def file-open

  ""

  ($.code/keyword "file.open"))



(def form

  ""
  
  ($.code/keyword "form"))



(def path

  ""

  ($.code/keyword "path"))



(def phase

  ""

  ($.code/keyword "phase"))



(def read

  ""

  ($.code/keyword "read"))



(def src

  ""

  ($.code/keyword "src"))



(def strx

  ""

  ($.code/keyword "special-trx"))



(def trx

  ""

  ($.code/keyword "trx"))



(def trx-eval

  ""

  ($.code/keyword "trx.eval"))



(def trx-prepare

  ""

  ($.code/keyword "trx/prepare"))
