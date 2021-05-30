(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  #?(:clj (:import convex.core.lang.Reader))
  (:require [convex.cvm                               :as $.cvm]
            [convex.cvm.eval                          :as $.cvm.eval]
            [convex.cvm.eval.src                      :as $.cvm.src]
            [convex.cvm.type                          :as $.cvm.type]
            #?(:clj [convex.lisp                      :as $.lisp])
            [convex.lisp.gen                          :as $.lisp.gen]
            #?@(:clj [[convex.break.eval]
                      [convex.break.gen]
                      [convex.break.prop]
                      [convex.break.run.fuzz]
                      [convex.break.test.account]
                      [convex.break.test.actor]
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
                      [convex.cvm.test]
                      [convex.example.exec]
                      [convex.example.templ]
                      [convex.lisp.test]
			          [clojure.data]])
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [hawk.core                                :as watcher]))


#?(:clj (set! *warn-on-reflection*
              true))


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


#?(:clj (comment


  ($.cvm.eval/result ($.cvm/ctx)
                     ($.lisp/templ* (nth ~($.lisp/blob "ff") 0))
                     )



  ($.cvm/account ($.cvm/ctx))




  (def ctx
       ($.cvm/watch {"src/convex/util.cvx" '$}))

  ($.cvm.eval/result @ctx
                     '$/every?)

  (.close ctx)

  ))
