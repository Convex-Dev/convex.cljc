(ns convex.lib.lab.xform

  "Dev environment for prototyping transducers."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}

  (:require [clojure.pprint]
            [convex.clj      :as $.clj]
            [convex.clj.eval :as $.clj.eval]
            [convex.cvm      :as $.cvm]
            [convex.watch    :as $.watch]))


;;;;;;;;;;


(comment


  (def a*env
       (-> ($.watch/init {:on-change (fn [env]
                                       (update env
                                               :ctx
                                               $.clj.eval/ctx
                                               '(do
                                                  (def store
                                                       (deploy store))
                                                  (def xform
                                                       (deploy xform)))))
                          :sym->dep  {'store "src/convex/lib/lab/xform/store.cvx"
                                      'xform "src/convex/lib/lab/xform.cvx"}})
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
                                                     ;(xform/filter (fn [item]
                                                     ;                (< (store/price item)
                                                     ;                   600)))
                                                     ;(xform/filter store/available?)
                                                     ;(xform/map store/code)
                                                     )
                                         xform/first
                                         store/inventory)))


  )


;;;;;;;;;; Clojure transducers


; cat
; dedupe
; distinct
; drop
; drop-while
; filter
; halt-when
; interpose
; keep
; keep-indexed
; map
; map-indexed
; mapcat
; partition-all
; partition-by
; random-sample
; remove
; replace
; take
; take-nth
; take-while


; https://github.com/cgrand/xforms
