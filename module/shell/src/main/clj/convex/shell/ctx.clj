(ns convex.shell.ctx

  "Preparing the genesis context used by the Shell.
  
   The Shell CVX library is executed in the core account so that all those functions
   are accessible from any account."

  {:author "Adam Helinski"}

  (:import (convex.core.init Init)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io         :as java.io]
            [convex.cell             :as $.cell]
            [convex.cvm              :as $.cvm]
            [convex.gen              :as $.gen]
            [convex.read             :as $.read]
            [convex.shell.req.gen    :as $.shell.req.gen]
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
                           ($.std/merge ($.cell/* {.account.genesis   ~$.cvm/genesis-user
                                                   .gen.address       ~($.shell.req.gen/create ($.cell/long -1)
                                                                                               $.gen/address)
                                                   .gen.any           ~($.shell.req.gen/create ($.cell/long -2)
                                                                                               $.gen/any)
                                                   .gen.any.coll      ~($.shell.req.gen/create ($.cell/long -3)
                                                                                               $.gen/any-coll)
                                                   .gen.any.list      ~($.shell.req.gen/create ($.cell/long -4)
                                                                                               $.gen/any-list)
                                                   .gen.any.map       ~($.shell.req.gen/create ($.cell/long -5)
                                                                                               $.gen/any-map)
                                                   .gen.any.set       ~($.shell.req.gen/create ($.cell/long -3)
                                                                                               $.gen/any-set)
                                                   .gen.any.vector    ~($.shell.req.gen/create ($.cell/long -7)
                                                                                               $.gen/any-vector)
                                                   .gen.blob-32       ~($.shell.req.gen/create ($.cell/long -8)
                                                                                               $.gen/blob-32)
                                                   .gen.boolean       ~($.shell.req.gen/create ($.cell/long -9)
                                                                                               $.gen/boolean)
                                                   .gen.char          ~($.shell.req.gen/create ($.cell/long -10)
                                                                                               $.gen/char)
                                                   .gen.char.alphanum ~($.shell.req.gen/create ($.cell/long -11)
                                                                                               $.gen/char-alphanum)
                                                   .gen.double        ~($.shell.req.gen/create ($.cell/long -12)
                                                                                               $.gen/double)
                                                   .gen.falsy         ~($.shell.req.gen/create ($.cell/long -13)
                                                                                               $.gen/falsy)
                                                   .gen.keyword       ~($.shell.req.gen/create ($.cell/long -14)
                                                                                               $.gen/keyword)
                                                   .gen.long          ~($.shell.req.gen/create ($.cell/long -15)
                                                                                               $.gen/long)
                                                   .gen.number        ~($.shell.req.gen/create ($.cell/long -16)
                                                                                               $.gen/number)
                                                   .gen.nil           ~($.shell.req.gen/create ($.cell/long -17)
                                                                                               $.gen/nothing)
                                                   .gen.scalar        ~($.shell.req.gen/create ($.cell/long -18)
                                                                                               $.gen/scalar)
                                                   .gen.symbol        ~($.shell.req.gen/create ($.cell/long -19)
                                                                                               $.gen/symbol)
                                                   .gen.truthy        ~($.shell.req.gen/create ($.cell/long -20)
                                                                                               $.gen/truthy)
                                                   .stream.stderr     [:stream
                                                                       ~$.shell.req.stream/stderr
                                                                       -3
                                                                       :stderr]
                                                   .stream.stdin      [:stream
                                                                       ~$.shell.req.stream/stdin
                                                                       -2
                                                                       :stdin]
                                                   .stream.stdout     [:stream
                                                                       ~$.shell.req.stream/stdout
                                                                       -1
                                                                       :stdout]
                                                   .sys.eol           ~($.cell/string (System/lineSeparator))
                                                   .version.java      [~($.cell/string (System/getProperty "java.vendor"))
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
