(ns convex.shell.req.dep

  "Requests for the experimental dependency management framework.
  
   See [[convex.shell.dep]]."

  (:refer-clojure :exclude [read])
  (:require [convex.cell               :as $.cell]
            [convex.cvm                :as $.cvm]
            [convex.shell.dep          :as $.shell.dep]
            [convex.shell.dep.relative :as $.shell.dep.relative]
            [convex.shell.flow         :as $.shell.flow]))


;;;;;;;;;;


(defn deploy

  "Request for deploying a deploy vector."

  [ctx [required]]

  ($.shell.flow/safe
    (delay
      (let [env (-> {:convex.shell/ctx ctx}
                    ($.shell.dep/fetch required)
                    ($.shell.dep/deploy-fetched))]
        ($.cvm/result-set (env :convex.shell/ctx)
                          ($.cell/map (partition 2
                                                 (env :convex.shell.dep/let))))))))



(defn fetch

  "Request for fetching required dependencies given a deploy vector.
   Does not execute nor deploy anything in the Shell."

  [ctx [required]]
  
  ($.shell.flow/safe
    (delay
      (let [env ($.shell.dep/fetch {:convex.shell/ctx ctx}
                                   required)]
        ($.cvm/result-set (env :convex.shell/ctx)
                          ($.cell/* {:hash->file ~($.cell/map (env :convex.shell.dep/hash->file))
                                     :tree       ~($.cell/map (map (fn [[hash binding+]]
                                                                     [hash
                                                                      ($.cell/vector (map $.cell/vector
                                                                                          binding+))])
                                                                   (env :convex.shell.dep/hash->binding+)))}))))))



(defn read

  "Request for reading CVX files resolved from a deploy vector."

  [ctx [required]]

  ($.shell.flow/safe
    (delay
      ($.cvm/result-set ctx
                        (-> {:convex.shell/ctx           ctx
                             :convex.shell.dep/content   ($.cell/* {})
                             :convex.shell.dep/resolver+ {($.cell/* :relative)
                                                          $.shell.dep.relative/content}}
                            ($.shell.dep/fetch required)
                            (:convex.shell.dep/content))))))
