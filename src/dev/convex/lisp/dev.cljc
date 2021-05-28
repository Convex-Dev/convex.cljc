(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  #?(:clj (:import convex.core.lang.Reader))
  (:require [convex.cvm                               :as $.cvm]
            [convex.cvm.eval                          :as $.cvm.eval]
            [convex.cvm.eval.src                      :as $.cvm.src]
            [convex.cvm.type                          :as $.cvm.type]
            #?(:clj [convex.lisp                      :as $.lisp])
            [convex.lisp.edn                          :as $.edn]
            [convex.lisp.gen                          :as $.gen]
            [convex.lisp.hex                          :as $.hex]
            #?(:clj [convex.lisp.run.fuzz])
            [convex.lisp.schema                       :as $.schema]
            #?@(:clj [[convex.cvm.test]
                      [convex.lisp.test]
                      [convex.lisp.test.core.account]
                      [convex.lisp.test.core.actor]
                      [convex.lisp.test.core.coerce]
                      [convex.lisp.test.core.coll]
                      [convex.lisp.test.core.ctrl]
                      [convex.lisp.test.core.def]
                      [convex.lisp.test.core.fn]
                      [convex.lisp.test.core.loop]
                      [convex.lisp.test.core.math]
                      [convex.lisp.test.core.pred]
                      [convex.lisp.test.core.set]
                      [convex.lisp.test.data]
                      [convex.lisp.test.edn]
                      [convex.lisp.test.eval          :as $.test.eval]
                      [convex.lisp.test.fuzz]
                      [convex.lisp.test.gen]
                      [convex.lisp.test.prop]
                      [convex.lisp.test.util]
                      [convex.example.exec]
                      [convex.example.templ]
			          [clojure.data]])
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [clojure.test.check.properties            :as tc.prop]))


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


  (reduce (fn [a b]
            a)
          []
          ($.cvm.type/map {}))
  

  ($.cvm/account ($.cvm/ctx))

  ))
