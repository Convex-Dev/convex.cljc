(ns convex.shell.ctx

  (:import (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io         :as java.io]
            [convex.cell             :as $.cell]
            [convex.cvm              :as $.cvm]
            [convex.read             :as $.read]
            [convex.shell.ctx.core   :as $.shell.ctx.core]
            [convex.shell.req.stream :as $.shell.req.stream]
            [convex.std              :as $.std]))


;;;;;;;;;; Private


(defn- -resource-cvx

  [path]

  (or (-> path
          (java.io/resource)
          (some-> (.openStream)
                  (InputStreamReader. StandardCharsets/UTF_8)
                  ($.read/stream)))
      (throw (Exception. (format "CVX file missing from classpath: %s"
                                 path)))))


;;;;;;;;;; Public


(def genesis

  (let [ctx (-> ($.cvm/ctx)
                ($.cvm/juice-refill)
                ($.cvm/fork-to $.shell.ctx.core/address)
                ($.cvm/def $.shell.ctx.core/address
                           ($.std/merge ($.cell/* {.account.genesis ~$.cvm/genesis-user
                                                   .stream.stderr   [:stream
                                                                     ~$.shell.req.stream/stderr
                                                                     -3
                                                                     :stderr]
                                                   .stream.stdin    [:stream
                                                                     ~$.shell.req.stream/stdin
                                                                     -2
                                                                     :stdin]
                                                   .stream.stdout   [:stream
                                                                     ~$.shell.req.stream/stdout
                                                                     -1
                                                                     :stdout]
                                                   .sys.eol         ~($.cell/string (System/lineSeparator))})
                                        (first (-resource-cvx "convex/shell/version.cvx"))))
                ($.cvm/eval ($.std/concat ($.cell/* (let [$CORE$ ~$.shell.ctx.core/address]))
                                          (-resource-cvx "convex/shell2.cvx"))))]
    (when ($.cvm/exception ctx)
      ;; Throw on purpose.
      ($.cvm/result ctx))
    (-> ctx
        ($.cvm/fork-to $.cvm/genesis-user)
        ($.cvm/juice-refill))))
