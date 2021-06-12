(ns convex.lib.lab.xform

  "Dev environment for prototyping transducers."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}

  (:require [clojure.pprint]
            [convex.clj.eval :as $.clj.eval]
            [convex.cvm      :as $.cvm]
            [convex.disk     :as $.disk]
            [convex.clj      :as $.clj]))


;;;;;;;;;;


(comment


  (def w*ctx
       ($.disk/watch {'store "src/convex/lib/lab/xform/store.cvx"
                      'xform "src/convex/lib/lab/xform.cvx"}
                     (fn [env]
                       (update env
                               :ctx
                               $.clj.eval/ctx
                               '(do
                                  (def store
                                       (deploy store))
                                  (def xform
                                       (deploy xform)))))))


  ($.cvm/exception @w*ctx)

  (.close w*ctx)



  ($.clj.eval/result* @w*ctx
                      (xform/transduce (xform/comp (xform/filter (fn [x] (> x 0)))
                                                   (xform/map inc))
                                       conj
                                       []
                                       [-1 0 1 42]))



  (clojure.pprint/pprint
    ($.clj.eval/result* @w*ctx
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
