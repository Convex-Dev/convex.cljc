(ns convex.run.sym

  "CVM symbols used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core.lang Symbols))
  (:refer-clojure :exclude [cycle
                            do
                            read
                            try])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def catch

  ""

  ($.code/symbol "cvm.catch"))



(def cycle

  ""

  ($.code/symbol "*cvm.cycle*"))



(def do

  ""

  Symbols/DO)



(def dep

  ""

  ($.code/symbol "dep"))



(def error

  ""

  ($.code/symbol "*cvm.error*"))



(def env

  ""

  ($.code/symbol "env"))



(def hook-end

  ""

  ($.code/symbol "hook.end"))



(def hook-error

  ""

  ($.code/symbol "hook.error"))



(def hook-out

  ""

  ($.code/symbol "hook.out"))



(def hook-trx

  ""

  ($.code/symbol "hook.trx"))



(def juice-last

  ""

  ($.code/symbol "*cvm.juice.last*"))



(def log

  ""

  Symbols/LOG)



(def out

  ""

  ($.code/symbol "out"))



(def read

  ""

  ($.code/symbol "read"))



(def screen-clear

  ""

  ($.code/symbol "screen.clear"))



(def splice

  ""

  ($.code/symbol "splice"))



(def strx

  ""

  ($.code/symbol "strx"))



(def trx-id

  ""

  ($.code/symbol "*cvm.trx.id*"))



(def try

  ""

  ($.code/symbol "try"))
