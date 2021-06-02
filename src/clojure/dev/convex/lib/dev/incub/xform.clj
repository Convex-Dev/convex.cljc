(ns convex.lib.dev.incub.xform

  "Dev environment for prototyping transducers."

  {:author "Adam Helinski"}

  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.eval :as $.cvm.eval]
            [convex.lisp     :as $.lisp]))


;;;;;;;;;;


(comment


  (def w*ctx
       ($.cvm/watch {"src/convex/lib/incub/xform.cvx" '$}))

  ($.cvm/exception @w*ctx)

  (.close w*ctx)



  ;; Fails because of: https://github.com/Convex-Dev/convex/issues/187

  ($.cvm.eval/result @w*ctx
                     '($/transduce ($/map inc)
                                   conj
                                   []
                                   [1 2]))



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
