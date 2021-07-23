(ns convex.all.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [clojure.data]
            [clojure.java.io]
            [clojure.pprint]
            [convex.app.fuzz]
            [convex.app.run]
            [convex.app.run.help]
            [convex.break]
            [convex.break.gen]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.data                   :as $.data]
            [convex.encode                 :as $.encode]
            [convex.run                    :as $.run]
            [convex.run.ctx                :as $.run.ctx]
            [convex.run.err                :as $.run.err]
            [convex.run.exec               :as $.run.exec]
            [convex.run.kw                 :as $.run.kw]
            [convex.run.sreq               :as $.run.sreq]
            [convex.run.sym                :as $.run.sym]
            [convex.sync                   :as $.sync]
            [convex.test.break.account]
            [convex.test.break.actor]
            [convex.test.break.code]
            [convex.test.break.coerce]
            [convex.test.break.coll]
            [convex.test.break.ctrl]
            [convex.test.break.def]
            [convex.test.break.fn]
            [convex.test.break.fuzz]
            [convex.test.break.literal]
            [convex.test.break.loop]
            [convex.test.break.math]
            [convex.test.break.pred]
            [convex.test.break.set]
            [convex.test.break.symbolic]
            [convex.test.break.syntax]
            [convex.test.clj]
            [convex.test.clj.translate]
            [convex.test.cvm]
            [convex.test.cvx.lib.trust]
            [convex.watch                  :as $.watch]
            [clojure.test.check.generators :as TC.gen]
            [hawk.core                     :as hawk]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


(comment


  )
