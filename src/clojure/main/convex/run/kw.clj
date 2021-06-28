(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            do
                            read])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def ^Keyword catch
  
  ""

  ($.code/keyword "catch"))



(def ^Keyword cause

  ""

  ($.code/keyword "cause"))



(def ^Keyword compile

  ""

  ($.code/keyword "compile"))



(def ^Keyword cvm-sreq

  ""

  ($.code/keyword "cvm.sreq"))



(def ^Keyword dep

  ""

  ($.code/keyword "dep"))



(def ^Keyword do

  ""

  ($.code/keyword "do"))



(def ^Keyword exception?

  ""

  ($.code/keyword "exception?"))



(def ^Keyword env

  ""

  ($.code/keyword "env"))



(def ^Keyword expand

  ""

  ($.code/keyword "expand"))



(def ^Keyword file-open

  ""

  ($.code/keyword "file.open"))



(def ^Keyword form

  ""
  
  ($.code/keyword "form"))



(def ^Keyword hook-end

  ""

  ($.code/keyword "hook.end"))



(def ^Keyword hook-error

  ""

  ($.code/keyword "hook.error"))



(def ^Keyword hook-out

  ""

  ($.code/keyword "hook.out"))



(def ^Keyword hook-result

  ""

  ($.code/keyword "hook.result"))



(def ^Keyword log

  ""

  ($.code/keyword "log"))



(def ^Keyword out

  ""

  ($.code/keyword "out"))



(def ^Keyword path

  ""

  ($.code/keyword "path"))



(def ^Keyword phase

  ""

  ($.code/keyword "phase"))



(def ^Keyword read

  ""

  ($.code/keyword "read"))



(def ^Keyword run

  ""

  ($.code/keyword "run"))



(def ^Keyword screen-clear

  ""

  ($.code/keyword "screen.clear"))



(def ^Keyword splice

  ""

  ($.code/keyword "splice"))



(def ^Keyword src

  ""

  ($.code/keyword "src"))



(def ^Keyword sreq

  ""

  ($.code/keyword "special-trx"))



(def ^Keyword trx

  ""

  ($.code/keyword "trx"))



(def ^Keyword trx-eval

  ""

  ($.code/keyword "trx.eval"))



(def ^Keyword trx-prepare

  ""

  ($.code/keyword "trx.prepare"))



(def ^Keyword try

  ""

  ($.code/keyword "try"))
