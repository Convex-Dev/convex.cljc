(ns convex.shell.req.dep

  (:refer-clojure :exclude [read])
  (:require [convex.cell               :as $.cell]
            [convex.cvm                :as $.cvm]
            [convex.shell.ctx          :as $.shell.ctx]
            [convex.shell.dep          :as $.shell.dep]
            [convex.shell.dep.relative :as $.shell.dep.relative]))


;;;;;;;;;;


(defn deploy

  [ctx [required]]

  ($.shell.ctx/safe
    (delay
      (let [env (-> {:convex.shell/ctx ctx}
                    ($.shell.dep/fetch required)
                    ($.shell.dep/deploy-fetched))]
        ($.cvm/result-set (env :convex.shell/ctx)
                          ($.cell/map (partition 2
                                                 (env :convex.shell.dep/let))))))))



(defn fetch

  [ctx [required]]
  
  ($.shell.ctx/safe
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

  [ctx [required]]

  ($.shell.ctx/safe
    (delay
      ($.cvm/result-set ctx
                        (-> {:convex.shell/ctx           ctx
                             :convex.shell.dep/content   ($.cell/* {})
                             :convex.shell.dep/resolver+ {($.cell/* :relative)
                                                          $.shell.dep.relative/content}}
                            ($.shell.dep/fetch required)
                            (:convex.shell.dep/content))))))
