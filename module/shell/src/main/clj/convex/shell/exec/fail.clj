(ns convex.shell.exec.fail

  ""

  (:require [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.shell.ctx :as $.shell.ctx]
            [convex.shell.kw  :as $.shell.kw]
            [convex.std       :as $.std]))


;;;;;;;;;; Notifying a failure or full halt


(let [trx-pop ($.cell/* ($.catch/pop))]

  (defn err

    "Must be called in case of failure, `err` being an error map (see the [[convex.shell.err]] namespace).
    
     Under `$.catch/*stack*` in the context is a stack of error handling transactions. This functions pops
     the next error handling transaction and prepends it to `$.trx/*list*`, the list of transactions pending
     for execution.

     Also, error becomes available under `$/*result*`.

     This simple scheme allows sophisticated exception handling to be implemented from CVX, as seen in the
     `$.catch` acccount."

    [env err]

    (let [err-2 ($.std/assoc err
                             $.shell.kw/exception?
                             ($.cell/* true))]
      (-> env
          (assoc :convex.shell/error
                 err-2)
          (cond->
            (env :convex.shell/ctx)
            (-> (update :convex.shell/ctx
                        $.cvm/exception-clear)
                ($.shell.ctx/def-result err-2)))
          ($.shell.ctx/prepend-trx trx-pop)))))
