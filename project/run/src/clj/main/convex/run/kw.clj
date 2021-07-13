(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            do
                            read])
  (:require [convex.data :as $.data]))


;;;;;;;;;;


(def ^Keyword advance
              ($.data/keyword "advance"))



(def ^Keyword cause
              ($.data/keyword "cause"))



(def ^Keyword compile
              ($.data/keyword "compile"))



(def ^Keyword cvm-sreq
              ($.data/keyword "cvm.sreq"))



(def ^Keyword dep
              ($.data/keyword "dep"))



(def ^Keyword do
              ($.data/keyword "do"))



(def ^Keyword exception?
              ($.data/keyword "exception?"))



(def ^Keyword env
              ($.data/keyword "env"))



(def ^Keyword expand
              ($.data/keyword "expand"))



(def ^Keyword form
              ($.data/keyword "form"))



(def ^Keyword hook-end
              ($.data/keyword "hook.end"))



(def ^Keyword hook-error
              ($.data/keyword "hook.error"))



(def ^Keyword hook-out
              ($.data/keyword "hook.out"))



(def ^Keyword hook-result
              ($.data/keyword "hook.result"))



(def ^Keyword log
              ($.data/keyword "log"))



(def ^Keyword main
              ($.data/keyword "main"))



(def ^Keyword out
              ($.data/keyword "out"))



(def ^Keyword path
              ($.data/keyword "path"))



(def ^Keyword phase
              ($.data/keyword "phase"))



(def ^Keyword read
              ($.data/keyword "read"))



(def ^Keyword report
              ($.data/keyword "report"))



(def ^Keyword run
              ($.data/keyword "run"))



(def ^Keyword screen-clear
              ($.data/keyword "screen.clear"))



(def ^Keyword splice
              ($.data/keyword "splice"))



(def ^Keyword src
              ($.data/keyword "src"))



(def ^Keyword sreq
              ($.data/keyword "special-trx"))



(def ^Keyword trx
              ($.data/keyword "trx"))



(def ^Keyword trx-eval
              ($.data/keyword "trx.eval"))



(def ^Keyword trx-prepare
              ($.data/keyword "trx.prepare"))



(def ^Keyword try
              ($.data/keyword "try"))



(def ^Keyword watch
              ($.data/keyword "watch"))
