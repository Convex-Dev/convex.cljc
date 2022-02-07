(ns convex.run.kw

  "CVX keywords used by the runner."

  {:author "Adam Helinski"}

  (:import (convex.core.data Keyword))
  (:refer-clojure :exclude [compile
                            eval])
  (:require [convex.cell :as $.cell]))


;;;;;;;;;;


(def ^Keyword client-close
              ($.cell/keyword "client.close"))

(def ^Keyword client-connect
              ($.cell/keyword "client.connect"))

(def ^Keyword client-query
              ($.cell/keyword "client.query"))

(def ^Keyword client-transact
              ($.cell/keyword "client.transact"))

(def ^Keyword code-read+
              ($.cell/keyword "code.read+"))

(def ^Keyword compile
              ($.cell/keyword "compile"))

(def ^Keyword cvm-sreq
              ($.cell/keyword "cvm.sreq"))

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

(def ^Keyword kp-from-seed
              ($.cell/keyword "kp.from-seed"))

(def ^Keyword kp-from-store
              ($.cell/keyword "kp.from-store"))

(def ^Keyword kp-gen
              ($.cell/keyword "kp.gen"))

(def ^Keyword kp-save
              ($.cell/keyword "kp.save"))

(def ^Keyword kp-seed
              ($.cell/keyword "kp.seed"))

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

(def ^Keyword testnet-create-account
              ($.cell/keyword "testnet.create-account"))

(def ^Keyword testnet-request-coins
              ($.cell/keyword "testnet.request-coins"))

(def ^Keyword time-advance
              ($.cell/keyword "time.advance"))

(def ^Keyword time-pop
              ($.cell/keyword "time.pop"))

(def ^Keyword time-push
              ($.cell/keyword "time.push"))

(def ^Keyword trx
              ($.cell/keyword "trx"))
