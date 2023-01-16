(ns convex.shell.ctx

  (:import (convex.core.init Init)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io         :as java.io]
            [convex.cell             :as $.cell]
            [convex.cvm              :as $.cvm]
            [convex.read             :as $.read]
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
                ($.cvm/fork-to Init/CORE_ADDRESS)
                ($.cvm/def Init/CORE_ADDRESS
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
                                                   .sys.eol         ~($.cell/string (System/lineSeparator))
                                                   .version.java    [~($.cell/string (System/getProperty "java.vendor"))
                                                                     ~($.cell/string (System/getProperty "java.version"))]})
                                        (first (-resource-cvx "convex/shell/version.cvx"))))
                ($.cvm/eval ($.std/concat ($.cell/* (let [$CORE$ ~Init/CORE_ADDRESS]))
                                          (-resource-cvx "convex/shell.cvx"))))]
    (when ($.cvm/exception ctx)
      ;; Throw on purpose.
      ($.cvm/result ctx))
    (-> ctx
        ($.cvm/fork-to $.cvm/genesis-user)
        ($.cvm/juice-refill))))
