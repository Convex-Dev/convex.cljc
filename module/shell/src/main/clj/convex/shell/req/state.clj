(ns convex.shell.req.state

  (:import (convex.core State))
  (:require [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.shell.ctx.core :as $.shell.ctx.core]
            [convex.shell.fail     :as $.shell.fail]
            [convex.std            :as $.std]))


;;;;;;;;;;


(defn- -safe

  [ctx f select-ctx-ok]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Can only execute a no-arg function")))
      (let [ctx-2 (-> ctx
                      ($.cvm/fork)
                      ($.cvm/invoke f
                                    ($.cvm/arg+*)))
            ex    ($.cvm/exception ctx-2)]
        (if ex
          ($.cvm/result-set ctx
                            ($.cell/* [false
                                       ~($.shell.fail/mappify-cvm-ex ex)]))
          ($.cvm/result-set (select-ctx-ok ctx
                                           ctx-2)
                            ($.cell/* [true
                                       ~($.cvm/result ctx-2)]))))))


;;;;;;;;;;


(defn safe

  [ctx [f]]

  (-safe ctx
         f
         (fn [_ctx-old ctx-new]
           ctx-new)))



(defn switch

  [ctx [^State state result]]

  (or (when-not (instance? State
                           state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/string "Can only load a state")))
      (-> ctx
          ($.cvm/state-set (.putAccount state
                                        $.shell.ctx.core/address
                                        ($.cvm/account ctx
                                                       $.shell.ctx.core/address)))
          ($.cvm/result-set result))))



(defn tmp

  [ctx [f]]

  (-safe ctx
         f
         (fn [ctx-old _ctx-new]
           ctx-old)))
