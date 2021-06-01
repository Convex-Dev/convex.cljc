(ns convex.dev

  "Dev playground."

  {:author "Adam Helinski"}

  (:import convex.core.lang.Reader)
  (:require [clojure.data]
            [clojure.pprint]
            [convex.break.eval]
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
            [convex.cvm                    :as $.cvm]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.cvm.eval.src           :as $.cvm.src]
            [convex.cvm.test]
            [convex.cvm.type               :as $.cvm.type]
            [convex.example.exec]
            [convex.example.templ]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
            [convex.lisp.test]
            [clojure.test.check.generators :as TC.gen]
            [hawk.core                     :as watcher]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


(comment


  (def w*ctx
       ($.cvm/watch {"src/convex/util.cvx" '$}))

  ($.cvm.eval/result @w*ctx
                     ($.lisp/templ* (do
                                      (defn f [a]
                                        (if (zero? a)
                                          :ok
                                          (tailcall (f (dec a)))))
                                      (f 10))))

  (.close w*ctx)

      
  )
