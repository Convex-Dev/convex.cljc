(ns convex.lib.dev.lab.xform

  "Dev environment for prototyping transducers."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}

  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.eval :as $.cvm.eval]
            [convex.cvm.raw  :as $.cvm.raw]
            [convex.disk     :as $.disk]
            [convex.lisp     :as $.lisp]))


;;;;;;;;;;


(comment


  (def w*ctx
       ($.disk/watch [["src/convex/lib/lab/xform/store.cvx"
                      {:code (partial $.cvm.raw/deploy
                                      'store)}]

                     ["src/convex/lib/lab/xform.cvx"
                      {:code (partial $.cvm.raw/deploy
                                      'xform)}]]))


  ($.cvm/exception @w*ctx)

  (.close w*ctx)



  ($.cvm.eval/result* @w*ctx
                      (xform/transduce (xform/comp (xform/filter (fn [x] (> x 0)))
                                                   (xform/map inc))
                                       conj
                                       []
                                       [-1 0 1 42]))




  ($.cvm.eval/result* @w*ctx
                      (xform/transduce (xform/comp (xform/filter (fn [item]
                                                                   (contains-key? (store/tag+ item)
                                                                                  :fruit)))
                                                   ;(xform/filter (fn [item]
                                                   ;                (< (store/price item)
                                                   ;                   600)))
                                                   ;(xform/filter store/available?)
                                                   (xform/map store/code)
                                                   )
                                       xform/last
                                       store/inventory))


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
