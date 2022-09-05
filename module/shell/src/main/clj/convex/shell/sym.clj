(ns convex.shell.sym

  "CVX symbols used by the shell."

  {:author "Adam Helinski"}

  (:import (convex.core.data Symbol))
  (:refer-clojure :exclude [list*])
  (:require [convex.cell :as $.cell]))


;;;;;;;;;; Dynamic symbols


(def ^Symbol active?*
             ($.cell/symbol "*active?*"))

(def ^Symbol list*
             ($.cell/symbol "*list*"))

(def ^Symbol out*
             ($.cell/symbol "*out*"))

(def ^Symbol result*
             ($.cell/symbol "*result*"))


;;;;;;;;;; Static symbols


(def ^Symbol $
             ($.cell/symbol "$"))

(def ^Symbol $-account
             ($.cell/symbol "$.account"))

(def ^Symbol $-catch
             ($.cell/symbol "$.catch"))

(def ^Symbol $-code
             ($.cell/symbol "$.code"))

(def ^Symbol $-db
             ($.cell/symbol "$.db"))

(def ^Symbol $-file
             ($.cell/symbol "$.file"))

(def ^Symbol $-fs
             ($.cell/symbol "$.fs"))

(def ^Symbol $-help
             ($.cell/symbol "$.help"))

(def ^Symbol $-log
             ($.cell/symbol "$.log"))

(def ^Symbol $-perf
             ($.cell/symbol "$.perf"))

(def ^Symbol $-process
             ($.cell/symbol "$.process"))

(def ^Symbol $-repl
             ($.cell/symbol "$.repl"))

(def ^Symbol $-stream
             ($.cell/symbol "$.stream"))

(def ^Symbol $-time
             ($.cell/symbol "$.time"))

(def ^Symbol $-term
             ($.cell/symbol "$.term"))

(def ^Symbol $-test
             ($.cell/symbol "$.test"))

(def ^Symbol $-trx
             ($.cell/symbol "$.trx"))

(def ^Symbol line
             ($.cell/symbol "line"))

(def ^Symbol version
             ($.cell/symbol "version"))

(def ^Symbol version-convex
             ($.cell/symbol "version-convex"))
