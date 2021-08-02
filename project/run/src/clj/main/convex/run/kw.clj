(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace and its children."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            do
                            eval
                            read])
  (:require [convex.cell :as $.cell]))


;;;;;;;;;;


(def ^Keyword cause
              ($.cell/keyword "cause"))



(def ^Keyword compile
              ($.cell/keyword "compile"))



(def ^Keyword cvm-sreq
              ($.cell/keyword "cvm.sreq"))



(def ^Keyword dep
              ($.cell/keyword "dep"))



(def ^Keyword exception?
              ($.cell/keyword "exception?"))



(def ^Keyword err-reader
              ($.cell/keyword "READER"))



(def ^Keyword err-stream
              ($.cell/keyword "STREAM"))


(def ^Keyword eval
              ($.cell/keyword "eval"))



(def ^Keyword exec
              ($.cell/keyword "exec"))



(def ^Keyword expand
              ($.cell/keyword "expand"))



(def ^Keyword file-in
              ($.cell/keyword "file.in"))



(def ^Keyword file-out
              ($.cell/keyword "file.out"))



(def ^Keyword form
              ($.cell/keyword "form"))



(def ^Keyword juice
              ($.cell/keyword "juice"))



(def ^Keyword juice-expand
              ($.cell/keyword "juice.expand"))



(def ^Keyword juice-compile
              ($.cell/keyword "juice.compile"))



(def ^Keyword juice-exec
              ($.cell/keyword "juice.exec"))



(def ^Keyword log-clear
              ($.cell/keyword "log.clear"))



(def ^Keyword log-get
              ($.cell/keyword "log.get"))



(def ^Keyword path
              ($.cell/keyword "path"))



(def ^Keyword perf-bench
              ($.cell/keyword "perf.bench"))



(def ^Keyword perf-track
              ($.cell/keyword "perf-track"))



(def ^Keyword phase
              ($.cell/keyword "phase"))



(def ^Keyword process-env
              ($.cell/keyword "process.env"))



(def ^Keyword process-exit
              ($.cell/keyword "process.exit"))



(def ^Keyword read+
              ($.cell/keyword "read+"))



(def ^Keyword report
              ($.cell/keyword "report"))



(def ^Keyword result
              ($.cell/keyword "result"))



(def ^Keyword splice
              ($.cell/keyword "splice"))



(def ^Keyword src
              ($.cell/keyword "src"))



(def ^Keyword sreq
              ($.cell/keyword "sreq"))



(def ^Keyword stream-close
              ($.cell/keyword "stream.close"))



(def ^Keyword stream-flush
              ($.cell/keyword "stream.flush"))



(def ^Keyword stream-in
              ($.cell/keyword "stream.in"))



(def ^Keyword stream-in+
              ($.cell/keyword "stream.in+"))



(def ^Keyword stream-line+
              ($.cell/keyword "stream.line+"))


(def ^Keyword stream-out
              ($.cell/keyword "stream.out"))



(def ^Keyword stream-out!
              ($.cell/keyword "stream.out!"))



(def ^Keyword time-advance
              ($.cell/keyword "time.advance"))



(def ^Keyword time-pop
              ($.cell/keyword "time.pop"))



(def ^Keyword time-push
              ($.cell/keyword "time.push"))


(def ^Keyword trx
              ($.cell/keyword "trx"))
