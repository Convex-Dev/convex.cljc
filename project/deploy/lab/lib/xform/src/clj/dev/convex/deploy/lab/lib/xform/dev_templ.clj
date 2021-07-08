(ns convex.deploy.lab.lib.xform.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [clojure.pprint]
            [convex.clj      :as $.clj]
            [convex.clj.eval :as $.clj.eval]
            [convex.cvm      :as $.cvm]
            [convex.watch    :as $.watch]))


;;;;;;;;;;


(comment


  (def a*env
       (-> ($.watch/init {:convex.watch/on-change (fn [env]
                                                    (update env
                                                            :convex.sync/ctx
                                                            $.clj.eval/ctx
                                                            '(do
                                                               (def store
                                                                    (deploy (first store)))
                                                               (def xform
                                                                    (deploy (first xform))))))
                          :convex.watch/sym->dep  {'store "project/deploy/lab/lib/xform/src/cvx/dev/convex/xform/store.cvx"
                                                   'xform "project/deploy/lab/lib/xform/src/cvx/main/convex/xform.cvx"}})
           $.watch/start))


  ($.cvm/exception ($.watch/ctx a*env))

  ($.watch/stop a*env)



  ($.clj.eval/result* ($.watch/ctx a*env)
                      (xform/transduce (xform/comp (xform/filter (fn [x] (> x 0)))
                                                   (xform/map inc))
                                       conj
                                       []
                                       [-1 0 1 42]))



  (clojure.pprint/pprint
    ($.clj.eval/result* ($.watch/ctx a*env)
                        (xform/transduce (xform/comp (xform/filter (fn [item]
                                                                     (contains-key? (store/tag+ item)
                                                                                    :fruit)))
                                                     (xform/filter (fn [item]
                                                                     (< (store/price item)
                                                                        600)))
                                                     (xform/filter store/available?)
                                                     (xform/map store/code)
                                                     )
                                         xform/first
                                         store/inventory)))


  )
