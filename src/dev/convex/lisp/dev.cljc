(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require #?(:clj [convex.lisp                      :as $])
            [convex.lisp.ctx                          :as $.ctx]
            [convex.lisp.edn                          :as $.edn]
            [convex.lisp.eval                         :as $.eval]
            [convex.lisp.eval.src                     :as $.eval.src]
            [convex.lisp.form                         :as $.form]
            [convex.lisp.hex                          :as $.hex]
            [convex.lisp.schema                       :as $.schema]
            #?@(:clj [[convex.lisp.test]
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
                      [convex.lisp.test.fuzzy]
                      [convex.lisp.test.prop]
                      [convex.lisp.test.schema        :as $.test.schema]
                      [convex.lisp.test.util]
			          [clojure.data]])
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [clojure.test.check.generators            :as tc.gen]
            [clojure.walk]
            [clojure.test.check.properties            :as tc.prop]
            [clojure.test.check.results               :as tc.result]
            [malli.core                               :as malli]
            [malli.error]
            [malli.generator                          :as malli.gen])
  #?(:clj (:import clojure.lang.RT
                   (convex.core Init
                                State)
                   convex.core.data.Symbol)))


#?(:clj (set! *warn-on-reflection*
              true))


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


;;;;;;;;;;


#?(:clj (comment



  (-> State
      clojure.reflect/reflect
      clojure.pprint/pprint)



  (-> Init/STATE
      $/datafy
      :accounts
      (->> (into []
                 (comp (map :environment)
                       (filter some?))))
      clojure.pprint/pprint)



  (-> Init/STATE
      $.edn/write
      ;(->> (spit "/tmp/convex.edn"))
      $.edn/read
      ;clojure.pprint/pprint
      )
  


  (def tx
       ($.form/templ {'?amount (malli.gen/generate [:int
                                                    {:min 10
                                                     :max 1000}])
                      '?key    (malli.gen/generate ($.test.schema/generator :convex/hexstring-32))}
                     '(do
                        (let [addr (create-account ?key)]
                          (transfer addr
                                    ?amount)
                          [*balance*
                           (balance addr)]))))
                


  (def ctx
       ($.ctx/create-fake))
                




  (->> '(lookup-syntax 'conj)
       $/read-form
       ($.ctx/eval ($.ctx/fork ctx))
       $.ctx/result
       ;$/datafy
       )

  (->> '(quot 13 5.0)
       $/read-form
       ($.ctx/eval ($.ctx/create-fake))
       $.ctx/result
       $/datafy
       )



  (-> ($.ctx/expand-compile ($.ctx/create-fake)
                            ($/read "(+ 1 1)"))
      $.ctx/query
      $.ctx/result
      $/datafy
      )


  (->> '(dotimes [i -1] (log i))
       $/read-form
       ($.ctx/eval ($.ctx/create-fake))
       $.ctx/log
       $/datafy
       )

  (-> ($.ctx/eval ($.ctx/create-fake)
                  ($/read "(= (vec (mapcat (fn [x] [x x]) ())) (reduce (fn [acc x] (conj acc x x)) [] (quote ())))"))
      $.ctx/result
      )


  (ppr
    ;(malli/validate [:not [:enum 1 2]]
    ;                3
    (malli.gen/generate :convex/truthy

      #_[:and
                         {:registry {::data    [:or
                                                ::int
                                                ::vector]
                                     ::int     :int
                                     ::vector  [:vector
                                                [:ref ::data]]}}
                         [:or ::data]
                         [:fn
                          (fn [_]
                            false)]]


                        {:registry (-> (malli/default-schemas)
                                       $.schema/registry
                                       )
                         :size     2
                         }))





  ))
