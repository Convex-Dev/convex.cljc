(ns convex.shell.ctx

  "Preparing the genesis context used by the Shell.
  
   The Shell CVX library is executed in the core account so that all those functions
   are accessible from any account."

  {:author "Adam Helinski"}

  (:import (convex.core.init Init)
           (convex.core.data AccountStatus)
           (java.io InputStreamReader)
           (java.nio.charset StandardCharsets))
  (:require [clojure.java.io :as java.io]
            [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [convex.key-pair :as $.key-pair] 
            [convex.read     :as $.read]
            [convex.std      :as $.std]
            [promesa.exec    :as promesa.exec]))


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


(let [               ctx  (-> ($.cvm/ctx {:convex.cvm/genesis-key+ [(-> ($.cell/blob (byte-array 32))
                                                                        ($.key-pair/ed25519)
                                                                        ($.key-pair/account-key))]})
                              ($.cvm/juice-refill)
                              ($.cvm/fork-to Init/CORE_ADDRESS))
      ^AccountStatus core ($.cvm/account ctx)]


  (def core-env

    "Genesis Core environment."

    (.getEnvironment core))



  (def core-meta

    "Genesis Core metadata."

    (.getMetadata core))


  (def genesis

    "Genesis context prepared for the Shell."

    (let [ctx-2 (-> ctx
                    ($.cvm/def Init/CORE_ADDRESS
                               ($.std/merge ($.cell/* {.account.genesis   ~$.cvm/genesis-user
                                                       .sys.eol           ~($.cell/string (System/lineSeparator))
                                                       .sys.vthread?      ~($.cell/boolean promesa.exec/virtual-threads-available?)
                                                       .version.java      [~($.cell/string (System/getProperty "java.vendor"))
                                                                           ~($.cell/string (System/getProperty "java.version"))]})
                                            (first (-resource-cvx "convex/shell/version.cvx"))))
                    ($.cvm/eval ($.std/concat ($.cell/* (let [$CORE$ ~Init/CORE_ADDRESS]))
                                              (-resource-cvx "convex/shell.cvx"))))]
      (when ($.cvm/exception ctx-2)
        ;; Throw on purpose.
        ($.cvm/result ctx-2))
      (-> ctx-2
          ($.cvm/fork-to $.cvm/genesis-user)
          ($.cvm/juice-refill)))))
