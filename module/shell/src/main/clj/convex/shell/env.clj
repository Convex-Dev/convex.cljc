(ns convex.shell.env

  (:refer-clojure :exclude [get
                            update])
  (:require [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.shell.ctx.core :as $.shell.ctx.core]
            [convex.std            :as $.std]))


;;;;;;;


(defn- -get

  [ctx]

   ($.cvm/look-up ctx
                  $.shell.ctx.core/address
                  ($.cell/* .shell.env)))



(defn get

  [ctx]

  (-> ctx
      (-get)
      (second)
      (deref)))



(defn update

  [ctx f]

  (let [v (-get ctx)]
    ($.cvm/def ctx
               $.shell.ctx.core/address
               ($.cell/* {.shell.env [~(-> v
                                           ($.std/nth 0)
                                           ($.std/true?)
                                           (not)
                                           ($.cell/boolean))
                                      ~($.cell/fake (f @($.std/nth v
                                                                   1)))]}))))