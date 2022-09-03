(ns convex.shell.kw

  "CVX keywords used by the shell."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            do
                            eval
                            read])
  (:require [convex.cell :as $.cell]))


;;;;;;;;;;


(def ^Keyword code-read+
              ($.cell/keyword "code.read+"))

(def ^Keyword compile
              ($.cell/keyword "compile"))

(def ^Keyword cvm-sreq
              ($.cell/keyword "cvm.sreq"))

(def ^Keyword dev-fatal
              ($.cell/keyword "dev.fatal"))

(def ^Keyword err-fs
              ($.cell/keyword "FS"))

(def ^Keyword err-reader
              ($.cell/keyword "READER"))

(def ^Keyword err-stream
              ($.cell/keyword "STREAM"))

(def ^Keyword etch-flush
              ($.cell/keyword "etch.flush"))

(def ^Keyword etch-open
              ($.cell/keyword "etch.open"))

(def ^Keyword etch-path
              ($.cell/keyword "etch.path"))

(def ^Keyword etch-read
              ($.cell/keyword "etch.read"))

(def ^Keyword etch-write
              ($.cell/keyword "etch.write"))

(def ^Keyword etch-root-read
              ($.cell/keyword "etch.root-read"))

(def ^Keyword etch-root-write
              ($.cell/keyword "etch.root-write"))

(def ^Keyword eval
              ($.cell/keyword "eval"))

(def ^Keyword exception?
              ($.cell/keyword "exception?"))

(def ^Keyword exec
              ($.cell/keyword "exec"))

(def ^Keyword expand
              ($.cell/keyword "expand"))

(def ^Keyword file-delete
              ($.cell/keyword "file.delete"))

(def ^Keyword file-stream-in
              ($.cell/keyword "file.stream.in"))

(def ^Keyword file-stream-out
              ($.cell/keyword "file.stream.out"))

(def ^Keyword file-tmp
              ($.cell/keyword "file.tmp"))

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
              ($.cell/keyword "perf.track"))

(def ^Keyword phase
              ($.cell/keyword "phase"))

(def ^Keyword process-env
              ($.cell/keyword "process.env"))

(def ^Keyword process-exit
              ($.cell/keyword "process.exit"))

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

(def ^Keyword stream
              ($.cell/keyword "stream"))

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

(def ^Keyword stream-open?
              ($.cell/keyword "stream.open?"))

(def ^Keyword stream-out
              ($.cell/keyword "stream.out"))

(def ^Keyword stream-outln
              ($.cell/keyword "stream.outln"))

(def ^Keyword stream-txt-out
              ($.cell/keyword "stream.txt.out"))

(def ^Keyword stream-txt-outln
              ($.cell/keyword "stream.txt.outln"))

(def ^Keyword time-advance
              ($.cell/keyword "time.advance"))

(def ^Keyword time-pop
              ($.cell/keyword "time.pop"))

(def ^Keyword time-push
              ($.cell/keyword "time.push"))

(def ^Keyword trx
              ($.cell/keyword "trx"))
