(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            do
                            eval
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



(def ^Keyword err-reader
              ($.data/keyword "READER"))



(def ^Keyword err-stream
              ($.data/keyword "STREAM"))


(def ^Keyword eval
              ($.data/keyword "eval"))



(def ^Keyword exec
              ($.data/keyword "exec"))



(def ^Keyword expand
              ($.data/keyword "expand"))



(def ^Keyword file.in
              ($.data/keyword "file.in"))



(def ^Keyword file.out
              ($.data/keyword "file.out"))



(def ^Keyword form
              ($.data/keyword "form"))



(def ^Keyword juice
              ($.data/keyword "juice"))



(def ^Keyword juice-expand
              ($.data/keyword "juice.expand"))



(def ^Keyword juice-compile
              ($.data/keyword "juice.compile"))



(def ^Keyword juice-exec
              ($.data/keyword "juice.exec"))



(def ^Keyword log
              ($.data/keyword "log"))



(def ^Keyword main
              ($.data/keyword "main"))



(def ^Keyword main-watch
              ($.data/keyword "main.watch"))



(def ^Keyword monitor
              ($.data/keyword "monitor"))



(def ^Keyword path
              ($.data/keyword "path"))



(def ^Keyword phase
              ($.data/keyword "phase"))



(def ^Keyword process-env
              ($.data/keyword "process.env"))



(def ^Keyword process-exit
              ($.data/keyword "process.exit"))



(def ^Keyword read+
              ($.data/keyword "read+"))



(def ^Keyword report
              ($.data/keyword "report"))



(def ^Keyword result
              ($.data/keyword "result"))



(def ^Keyword splice
              ($.data/keyword "splice"))



(def ^Keyword src
              ($.data/keyword "src"))



(def ^Keyword sreq
              ($.data/keyword "sreq"))



(def ^Keyword state-pop
              ($.data/keyword "state.pop"))



(def ^Keyword state-push
              ($.data/keyword "state.push"))



(def ^Keyword stream-close
              ($.data/keyword "stream.close"))



(def ^Keyword stream-flush
              ($.data/keyword "stream.flush"))



(def ^Keyword stream-in
              ($.data/keyword "stream.in"))



(def ^Keyword stream-in+
              ($.data/keyword "stream.in+"))



(def ^Keyword stream-line+
              ($.data/keyword "stream.line+"))


(def ^Keyword stream-out
              ($.data/keyword "stream.out"))



(def ^Keyword stream-out!
              ($.data/keyword "stream.out!"))



(def ^Keyword trx
              ($.data/keyword "trx"))



(def ^Keyword watch
              ($.data/keyword "watch"))
