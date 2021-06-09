(ns convex.dev

  "Dev playground."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:import convex.core.ErrorCodes
           convex.core.lang.Reader)
  (:require [clojure.data]
            [clojure.pprint]
            [convex.app.fuzz]
            [convex.break]
            [convex.break.gen]
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
            [convex.code                   :as $.code]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.cvm.test]
            [convex.disk                   :as $.disk]
            [convex.example.exec]
            [convex.example.templ]
			[convex.lib.lab.xform]
            [convex.lib.trust]
            [convex.lib.test.trust]
            [convex.clj                   :as $.clj]
            [convex.clj.gen               :as $.clj.gen]
            [convex.clj.test]
            [convex.sync                   :as $.sync]
            [clojure.test.check.generators :as TC.gen]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


(comment


  (def ctx
       (:ctx ($.disk/load [["src/convex/break/util.cvx"
                            {:wrap (partial $.code/deploy
                                            '$)}]])))


  ($.cvm/exception ctx)

  ($.clj.eval/result* ctx
                      $/foo)




  (def w*ctx
       ($.disk/watch [["src/convex/break/util.cvx"
                       {:wrap (partial $.code/deploy
                                       '$)}]]
                     {:on-error println}))

  (.close w*ctx)


  ($.cvm/exception @w*ctx)

  ($.clj.eval/result* @w*ctx
                      $/foo)


  

      
  )
