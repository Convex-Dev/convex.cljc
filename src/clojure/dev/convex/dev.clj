(ns convex.dev

  "Dev playground."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:import convex.core.ErrorCodes
           convex.core.lang.Reader)
  (:require [clojure.data]
            [clojure.pprint]
            [convex.break]
            [convex.break.gen]
            [convex.break.run.fuzz]
            [convex.break.test.account]
            [convex.break.test.actor]
            [convex.break.test.code]
            [convex.break.test.coerce]
            [convex.break.test.coll]
            [convex.break.test.ctrl]
            [convex.break.test.def]
            [convex.break.test.fn]
            [convex.break.test.fuzz]
            [convex.break.test.literal]
            [convex.break.test.loop]
            [convex.break.test.math]
            [convex.break.test.pred]
            [convex.break.test.set]
            [convex.break.test.symbolic]
            [convex.break.test.syntax]
            [convex.cvm                    :as $.cvm]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.cvm.file               :as $.cvm.file]
            [convex.cvm.raw                :as $.cvm.raw]
            [convex.cvm.test]
            [convex.example.exec]
            [convex.example.templ]
			[convex.lib.dev.lab.xform]
            [convex.lib.dev.trust]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
            [convex.lisp.test]
            [clojure.test.check.generators :as TC.gen]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


(comment


  (def ctx
       ($.cvm.file/deploy ($.cvm/ctx)
                          'lib
                          "src/convex/break/util.cvx"))

  ($.cvm.eval/result* ctx
                      lib/every?)




  (.close w*ctx)
  (def w*ctx
       ($.cvm.file/watch [["src/convex/break/util.cvx" '$]]))


  ($.cvm/exception @w*ctx)

  ($.cvm.eval/result* @w*ctx
                      $/foo)


  

      
  )
