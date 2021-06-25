(ns convex.run.kw

  "CVM keywords used by the [[convex.run]] namespace."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [compile
                            do
                            read])
  (:require [convex.code :as $.code]))


;;;;;;;;;;


(def catch
  
  ""

  ($.code/keyword "catch"))



(def cause

  ""

  ($.code/keyword "cause"))



(def compile

  ""

  ($.code/keyword "compile"))


(def cvm-strx

  ""

  ($.code/keyword "cvm.strx"))



(def dep

  ""

  ($.code/keyword "dep"))



(def do

  ""

  ($.code/keyword "do"))



(def exception?

  ""

  ($.code/keyword "exception?"))



(def env

  ""

  ($.code/keyword "env"))



(def expand

  ""

  ($.code/keyword "expand"))



(def file-open

  ""

  ($.code/keyword "file.open"))



(def form

  ""
  
  ($.code/keyword "form"))



(def hook-end

  ""

  ($.code/keyword "hook.end"))



(def hook-error

  ""

  ($.code/keyword "hook.error"))



(def hook-out

  ""

  ($.code/keyword "hook.out"))



(def hook-trx

  ""

  ($.code/keyword "hook.trx"))



(def log

  ""

  ($.code/keyword "log"))



(def out

  ""

  ($.code/keyword "out"))



(def path

  ""

  ($.code/keyword "path"))



(def phase

  ""

  ($.code/keyword "phase"))



(def read

  ""

  ($.code/keyword "read"))



(def run

  ""

  ($.code/keyword "run"))



(def screen-clear

  ""

  ($.code/keyword "screen.clear"))



(def splice

  ""

  ($.code/keyword "splice"))



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

  ($.code/keyword "trx.prepare"))



(def try

  ""

  ($.code/keyword "try"))
