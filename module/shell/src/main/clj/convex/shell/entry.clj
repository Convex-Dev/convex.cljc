(ns convex.shell.entry

  (:import (convex.core.lang Context)
           (convex.core.lang.impl CoreFn
                                  ErrorValue)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io  :as java.io]
            [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.read      :as $.read]
            [convex.shell.env :as $.shell.env]
            [convex.shell.io  :as $.shell.io]
            [convex.shell.req :as $.shell.req]
            [convex.std       :as $.std]))


;;;;;;;;;;


(def address

  ($.cell/address 0))



(def invoker

  (proxy

    [CoreFn]

    [($.cell/* shell.invoke)]

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





(def ctx

  (let [ctx (reduce (fn [ctx path]
                      (if-some [resource (java.io/resource path)]
                        ;; CVX file on classpath.
                        (let [ctx-2 ($.cvm/eval
                                      ctx
                                      ($.cell/list (cons ($.cell/* do)
                                                         (try
                                                           (-> resource
                                                               (.openStream)
                                                               (InputStreamReader. StandardCharsets/UTF_8)
                                                               ($.read/stream))
                                                           (catch Throwable ex
                                                             (throw (ex-info "While reading CVX library"
                                                                             {::ex   ex
                                                                              ::path path})))))))
                              ex    ($.cvm/exception ctx-2)]
                          (when ex
                            (throw (ex-info "CVM exception while compiling CVX library"
                                            {::base :eval
                                             ::ex   ex
                                             ::path path})))
                          ctx-2)
                        ;; CVX file not on classpath.
                        (throw (ex-info "Mandatory CVX library is not on classpath"
                                        {::base :not-found
                                         ::path path}))))
                    ;;
                    (-> ($.cvm/ctx)
                        ($.cvm/juice-refill)
                        ($.cvm/fork-to address))
                    ;;
                    ["convex/shell2.cvx"])]
    (-> ctx
        ($.cvm/def address
                   ($.cell/* {shell.env    [true
                                            ~($.cell/fake {:convex.shell/req+            convex.shell.req/impl
                                                           :convex.shell/handle->stream  {($.cell/* :stderr) $.shell.io/stderr-txt
                                                                                          ($.cell/* :stdin)  $.shell.io/stdin-txt
                                                                                          ($.cell/* :stdout) $.shell.io/stdout-txt}
                                                           :convex.shell.etch/read-only? false})]
                              shell.invoke ~invoker
                              sys.eol      ~($.cell/string (System/lineSeparator))}))
        ($.cvm/fork-to $.cvm/genesis-user)
        ($.cvm/juice-refill))))


