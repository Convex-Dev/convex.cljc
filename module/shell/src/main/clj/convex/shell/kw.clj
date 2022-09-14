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


(def ^Keyword arg
              ($.cell/keyword "arg"))

(def ^Keyword catch-rethrow
              ($.cell/keyword "catch.rethrow"))

(def ^Keyword cause
              ($.cell/keyword "cause"))

(def ^Keyword code-read+
              ($.cell/keyword "code.read+"))

(def ^Keyword compile
              ($.cell/keyword "compile"))

(def ^Keyword cvm-sreq
              ($.cell/keyword "cvm.sreq"))

(def ^Keyword dev-fatal
              ($.cell/keyword "dev.fatal"))

(def ^Keyword err-db
              ($.cell/keyword "DB"))

(def ^Keyword err-filesystem
              ($.cell/keyword "FILESYSTEM"))

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

(def ^Keyword etch-read-only
              ($.cell/keyword "etch.read-only"))

(def ^Keyword etch-read-only?
              ($.cell/keyword "etch.read-only?"))

(def ^Keyword etch-write
              ($.cell/keyword "etch.write"))

(def ^Keyword etch-root-read
              ($.cell/keyword "etch.root.read"))

(def ^Keyword etch-root-write
              ($.cell/keyword "etch.root.write"))

(def ^Keyword eval
              ($.cell/keyword "eval"))

(def ^Keyword exception?
              ($.cell/keyword "exception?"))

(def ^Keyword exec
              ($.cell/keyword "exec"))

(def ^Keyword expand
              ($.cell/keyword "expand"))

(def ^Keyword file-copy
              ($.cell/keyword "file.copy"))

(def ^Keyword file-delete
              ($.cell/keyword "file.delete"))

(def ^Keyword file-exists
              ($.cell/keyword "file.exists"))

(def ^Keyword file-stream-in
              ($.cell/keyword "file.stream.in"))

(def ^Keyword file-stream-out
              ($.cell/keyword "file.stream.out"))

(def ^Keyword file-tmp
              ($.cell/keyword "file.tmp"))

(def ^Keyword file-tmp-dir
              ($.cell/keyword "file.tmp-dir"))

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

(def ^Keyword juice-limit
              ($.cell/keyword "juice.limit"))

(def ^Keyword juice-limit-set
              ($.cell/keyword "juice.limit.set"))

(def ^Keyword juice-track
              ($.cell/keyword "juice.track"))

(def ^Keyword library-path
              ($.cell/keyword "library-path"))

(def ^Keyword log-clear
              ($.cell/keyword "log.clear"))

(def ^Keyword log-get
              ($.cell/keyword "log.get"))

(def ^Keyword path
              ($.cell/keyword "path"))

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

(def ^Keyword state-load
              ($.cell/keyword "state.load"))

(def ^Keyword state-safe
              ($.cell/keyword "state.safe"))

(def ^Keyword stream
              ($.cell/keyword "stream"))

(def ^Keyword stream-close
              ($.cell/keyword "stream.close"))

(def ^Keyword stream-flush
              ($.cell/keyword "stream.flush"))

(def ^Keyword stream-in+
              ($.cell/keyword "stream.in+"))

(def ^Keyword stream-line
              ($.cell/keyword "stream.line"))

(def ^Keyword stream-open?
              ($.cell/keyword "stream.open?"))

(def ^Keyword stream-out
              ($.cell/keyword "stream.out"))

(def ^Keyword stream-outln
              ($.cell/keyword "stream.outln"))

(def ^Keyword stream-txt-in
              ($.cell/keyword "stream.txt.in"))

(def ^Keyword stream-txt-line
              ($.cell/keyword "stream.txt.line"))

(def ^Keyword stream-txt-out
              ($.cell/keyword "stream.txt.out"))

(def ^Keyword stream-txt-outln
              ($.cell/keyword "stream.txt.outln"))

(def ^Keyword time-advance
              ($.cell/keyword "time.advance"))

(def ^Keyword time-bench
              ($.cell/keyword "time.bench"))

(def ^Keyword time-iso->unix
              ($.cell/keyword "time.iso->unix"))

(def ^Keyword time-nano
              ($.cell/keyword "time.nano"))

(def ^Keyword time-unix
              ($.cell/keyword "time.unix"))

(def ^Keyword time-unix->iso
              ($.cell/keyword "time.unix->iso"))

(def ^Keyword trx
              ($.cell/keyword "trx"))
