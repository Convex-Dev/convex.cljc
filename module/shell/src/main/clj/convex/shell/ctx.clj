(ns convex.shell.ctx

  (:import (convex.core.lang Context)
           (convex.core.lang.impl CoreFn
                                  ErrorValue)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io       :as java.io]
            [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.read           :as $.read]
            [convex.shell.ctx.core :as $.shell.ctx.core]
            [convex.shell.env      :as $.shell.env]
            [convex.shell.io       :as $.shell.io]
            [convex.shell.req      :as $.shell.req]
            [convex.std            :as $.std]))


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


(def invoker

  (proxy

    [CoreFn]

    [($.cell/* .shell.invoke)]

    (invoke [ctx arg+]
      (let [sym (first arg+)]
        (if ($.std/symbol? sym)
          (if-some [f (-> ctx
                          ($.shell.env/get)
                          (get-in [:convex.shell/req+
                                   sym]))]
            
            (let [^Context    ctx-2 (f ctx
                                       (rest arg+))
                  ^ErrorValue ex    ($.cvm/exception ctx-2)]
              (if ex
                (.withException ctx-2
                                (doto ex
                                  (.addTrace (format "In Convex Shell request: %s"
                                                     sym))))
                ctx-2))

            ($.cvm/exception-set ctx
                                 ($.cell/code-std* :ARGUMENT)
                                 ($.cell/string (format "Unknown Convex Shell request: %s"
                                                        sym))))
          ($.cvm/exception-set ctx
                               ($.cell/code-std* :ARGUMENT)
                               ($.cell/string "Convex Shell invocation require a symbol")))))))


;;;;;;;;;;


(def genesis

  (-> ($.cvm/ctx)
      ($.cvm/juice-refill)
      ($.cvm/fork-to $.shell.ctx.core/address)
      ($.cvm/eval ($.std/concat ($.cell/* (let [$CORE$ ~$.shell.ctx.core/address]))
                                (-resource-cvx "convex/shell2.cvx")))

      ($.cvm/def $.shell.ctx.core/address
                 ($.std/merge ($.cell/* {.shell.env    [true
                                                        ~($.cell/fake {:convex.shell/req+            convex.shell.req/impl
                                                                       :convex.shell/handle->stream  {($.cell/* :stderr) $.shell.io/stderr-txt
                                                                                                      ($.cell/* :stdin)  $.shell.io/stdin-txt
                                                                                                      ($.cell/* :stdout) $.shell.io/stdout-txt}
                                                                       :convex.shell.etch/read-only? false})]
                                         .shell.invoke ~invoker
                                         .sys.eol      ~($.cell/string (System/lineSeparator))})
                              (first (-resource-cvx "convex/shell/version.cvx"))))
      ($.cvm/fork-to $.cvm/genesis-user)
      ($.cvm/juice-refill)))
