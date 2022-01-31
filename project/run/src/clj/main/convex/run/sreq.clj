(ns convex.run.sreq

  "Implementation of requests interpreted by the runner between transactions.
  
   A reqest is merely a CVX vector following some particular convention that the
   runner follows for producing effects beyond the scope of the CVM."

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               Ed25519KeyPair)
           (convex.core.data AVector
                             Blob)
           (convex.core.data.prim CVMLong)
           (convex.core.lang Context)
           (java.security UnrecoverableKeyException))
  (:require [convex.cell       :as $.cell]
            [convex.cvm        :as $.cvm]
            [convex.pfx        :as $.pfx]
            [convex.read       :as $.read]
            [convex.run.ctx    :as $.run.ctx]
            [convex.run.err    :as $.run.err]
            [convex.run.exec   :as $.run.exec]
            [convex.run.kw     :as $.run.kw]
            [convex.run.stream :as $.run.stream]
            [convex.run.sym    :as $.run.sym]
            [convex.sign       :as $.sign]
            [convex.std        :as $.std]
            [criterium.core    :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Helpers


(defn- -def-key-pair

  ;; Given a key pair, defines as result a CVX vector `[PublicKey PrivateKey]`

  [env ^AKeyPair kp]

  ($.run.ctx/def-result env
                        ($.cell/vector [($.sign/account-key kp)
                                        (.getEncodedPrivateKey kp)])))



(defn- -key-store

  ;; Get a key store, handling all possible paths.

  ;; TODO. Refactor. Works but ugly.

  [env tuple cvx-path cvx-passphrase]

  (let [fail       (fn [message]
                     [($.run.exec/fail env
                                       ($.run.err/sreq ($.cell/code-std* :UNEXPECTED)
                                                       ($.cell/string message)
                                                       tuple))
                      nil])
        path       (str cvx-path)
        passphrase (str cvx-passphrase)
        [env-2
         store]    (try
                     [nil
                      ($.pfx/load path
                                  passphrase)]
                     (catch Throwable _ex
                       (fail "Unable to open key store from existing file ; maybe verify passphrase?")))]
    (cond
      env-2 [env-2
             nil]
      store [nil
             store]
      :else (let [[env-3
                   store]  (try
                             [nil
                              ($.pfx/create path
                                            passphrase)]
                             (catch Throwable _ex
                               (fail "Unable to create key store at the given path ; maybe verify passphrase?")))]
              (cond
                env-3 [env-3
                       nil]
                store [nil
                       store]
                :else (recur env
                             tuple
                             path
                             passphrase))))))



(defn- -stream

  ;; Given a request, returns the stream values it contains as a Java long.

  [^AVector tuple]

  (.longValue ^CVMLong (.get tuple
                             2)))


;;;;;;;;;; Setup


(defmethod $.run.exec/sreq
  
  nil

  ;; No request, simply finalizes a regular transactions.

  [env result]

  ($.run.ctx/def-result env
                        result))



(defmethod $.run.exec/sreq
  
  :unknown

  ;; Unknown request, consided as failure.

  [env tuple]

  ($.run.exec/fail env
                   ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                   ($.cell/string "Unsupported special transaction")
                                   tuple)))

;;;;;;;;;; Code


(defmethod $.run.exec/sreq

  $.run.kw/code-read+

  ;; Reads the given string and parses it to a list of forms.

  ;; TODO. Improve error reporting.
  
  [env ^AVector tuple]

  (try
    ($.run.ctx/def-result env
                          (-> (.get tuple
                                    2)
                              str
                              $.read/string+))
    (catch Throwable _err
      ($.run.exec/fail env
                       ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                       ($.cell/string "Unable to read source")
                                       tuple)))))


;;;;;;;;;; File


(defmethod $.run.exec/sreq

  $.run.kw/file-in

  ;; Opens a file for reading.

  [env ^AVector tuple]

  ($.run.stream/file-in env
                        (str (.get tuple
                                   2))))



(defmethod $.run.exec/sreq

  $.run.kw/file-out

  ;; Opens a file for writing.

  [env ^AVector tuple]

  ($.run.stream/file-out env
                         (str (.get tuple
                                    2))))


;;;;;;;;;; Key pair management


(defmethod $.run.exec/sreq

  $.run.kw/kp-gen

  ;; Generates key pair randomly.

  [env _tuple]

  (-def-key-pair env
                 ($.sign/ed25519)))



(defmethod $.run.exec/sreq

  $.run.kw/kp-from-seed

  ;; Reconstitutes a key pair from a seed.

  [env tuple]

  (-def-key-pair env
                 ($.sign/ed25519 ($.std/get tuple
                                            ($.cell/* 2)))))



(defmethod $.run.exec/sreq

  $.run.kw/kp-from-store

  ;; Retrieves a key pair from a key store.

  [env tuple]
  
  (let [[path
         passphrase-store
         alias-key-pair
         passphrase-key-pair] (drop 2
                                    tuple)
        [env-2
         key-store]           (-key-store env
                                          tuple
                                          path
                                          passphrase-store)]
    (or env-2
        (try
          (if-some [key-pair ($.pfx/key-pair-get key-store
                                                 (str alias-key-pair)
                                                 (str passphrase-key-pair))]
            (-def-key-pair env
                           key-pair)
            ($.run.ctx/def-result env
                                  nil))
          (catch UnrecoverableKeyException _ex
            ($.run.exec/fail env
                             ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                             ($.cell/string "Unable to retrieve key pair from opened key store ; is the passphrase correct?")
                                             tuple)))
          (catch Throwable _ex
            ($.run.exec/fail env
                             ($.run.err/sreq ($.cell/code-std* :UNEXPECTED)
                                             ($.cell/string "Unable to retrieve key pair from opened key store")
                                             tuple)))))))



(defmethod $.run.exec/sreq

  $.run.kw/kp-save

  ;; Save a key pair to a key store.

  [env tuple]

  (let [[path
         passphrase-store
         alias-key-pair
         key-pair
         passphrase-key-pair] (drop 2
                                    tuple)
        [env-2
         key-store]           (-key-store env
                                          tuple
                                          path
                                          passphrase-store)]
    (or env-2
        (try

          (-> key-store
              ($.pfx/key-pair-set (str alias-key-pair)
                                  (Ed25519KeyPair/create ($.cell/key ($.std/get key-pair
                                                                                ($.cell/* 0)))
                                                         ^Blob ($.std/get key-pair
                                                                          ($.cell/* 1)))
                                  (str passphrase-key-pair))
              ($.pfx/save (str path)
                          (str passphrase-store)))
          ($.run.ctx/def-result env
                                nil)

          (catch Throwable _ex
            ($.run.exec/fail env
                             ($.run.err/sreq ($.cell/code-std* :UNEXPECTED)
                                             ($.cell/string "Unable to add key pair to key store ; is the file accessible?")
                                             tuple)))))))



(defmethod $.run.exec/sreq

  $.run.kw/kp-seed

  ;; Retrieves the seed of the given key pair.

  [env tuple]

  (try
    ($.run.ctx/def-result env
                          (let [cvx-kp ($.std/nth tuple
                                                  2)]
                            ($.sign/seed (Ed25519KeyPair/create ($.cell/key ($.std/nth cvx-kp
                                                                                       0))
                                                                ^Blob ($.std/nth cvx-kp
                                                                                 1)))))
    (catch Throwable _err
      ($.run.exec/fail env
                       ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                       ($.cell/string "Unknown error while extracting seed from key pair ; is it really an ED25519 key pair?")
                                       tuple)))))


;;;;;;;;;; Logging


(defmethod $.run.exec/sreq
  
  $.run.kw/log-clear

  ;; Clears the CVM log.

  [env _tuple]

  (let [ctx   (env :convex.run/ctx)
        ctx-2 ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                          :convex.cvm/state   ($.cvm/state ctx)})]
    (-> env
        (assoc :convex.run/ctx
               ctx-2)
        ($.run.ctx/def-result ($.cvm/log ctx-2)))))



(defmethod $.run.exec/sreq
  
  $.run.kw/log-get

  ;; Interns the current state of the CVM log under `$/*result*`.

  [env _tuple]

  ($.run.ctx/def-result env
                        ($.cvm/log (env :convex.run/ctx))))


;;;;;;;;;; Performance


(defmethod $.run.exec/sreq

  $.run.kw/perf-bench

  ;; Benchmarks a transaction using Criterium.

  [env ^AVector tuple]

  (let [ctx   ($.cvm/fork (env :convex.run/ctx))
        cell  (.get tuple
                    2)
        stat+ (criterium/benchmark* (fn []
                                      (.query ^Context ctx
                                              cell))
                                    {})]
    ($.run.ctx/def-result env
                          ($.cell/map {($.cell/keyword "mean")   ($.cell/double (first (stat+ :mean)))
                                       ($.cell/keyword "stddev") ($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))



(defmethod $.run.exec/sreq

  $.run.kw/perf-track

  ;; Tracks juice consumption of the given transaction.

  [env ^AVector tuple]

  ($.run.exec/trx-track env
                        (.get tuple
                              2)))


;;;;;;;;;; Process


(defmethod $.run.exec/sreq

  $.run.kw/process-exit

  ;; Exits process with the user given status code.

  [_env ^AVector tuple]

  (let [status (.longValue ^CVMLong (.get tuple
                                          2))]
    (if (= (System/getenv "CONVEX_DEV")
           "true")
      (throw (ex-info "Throw instead of exit since dev mode"
                      {::status status}))
      (System/exit status))))



(defmethod $.run.exec/sreq

  $.run.kw/process-env
  
  ;; Interns under `$/*result*` the process environment map or a single requested variable.

  [env ^AVector tuple]

  ($.run.ctx/def-result env
                        (if-some [env-var (.get tuple
                                                2)]
                          (some-> (System/getenv (str env-var))
                                  $.cell/string)
                          ($.cell/map (map (fn [[k v]]
                                             [($.cell/string k)
                                              ($.cell/string v)])
                                           (System/getenv))))))


;;;;;;;;;; Streams


(defmethod $.run.exec/sreq

  $.run.kw/stream-close

  ;; Closes the given stream.

  [env tuple]

  ($.run.stream/close env
                      (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-flush

  ;; Flushes the given stream.

  [env ^AVector tuple]

  ($.run.stream/flush env
                      (-stream tuple)))

(defmethod $.run.exec/sreq

  $.run.kw/stream-in

  ;; Reads a single cell from the given stream.

  [env tuple]

  ($.run.stream/in env
                   (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-in+

  ;; Reads all available cells from the given stream.

  [env tuple]

  ($.run.stream/in+ env
                    (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-line+

  ;; Reads line from the given stream and extracts all available cells.

  [env tuple]

  ($.run.stream/line+ env
                      (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-out

  ;; Writes a cell to the given stream.

  [env ^AVector tuple]

  ($.run.stream/out env
                    (-stream tuple)
                    (.get tuple
                          3)))



(defmethod $.run.exec/sreq

  ;; Writes a cell to the given stream, appends a new line, and flushes everything.

  $.run.kw/stream-out!

  [env ^AVector tuple]

  ($.run.stream/out! env
                     (-stream tuple)
                     (.get tuple
                           3)))


;;;;;;;;;; Time


(defmethod $.run.exec/sreq

  $.run.kw/time-advance

  ;; Advances the timestamp.

  [env ^AVector tuple]

  (let [^CVMLong interval (.get tuple
                                2)]
    (-> env
        (update :convex.run/ctx
                (fn [ctx]
                  ($.cvm/time-advance ctx
                                      (.longValue interval))))
        ($.run.ctx/def-result interval))))



(defmethod $.run.exec/sreq

  $.run.kw/time-pop

  ;; Pops the last context saved with `$.time/push`.

  [env ^AVector tuple]

  (let [stack (env :convex.run/state-stack)]
    (if-some [ctx-restore (peek stack)]
      (-> env
          (assoc :convex.run/state-stack (pop stack)
                 :convex.run/ctx         ctx-restore)
          ($.run.ctx/def-trx+ ($.cell/list [(.get tuple
                                                  2)])))
      ($.run.exec/fail env
                       ($.run.err/sreq ($.cell/code-std* :STATE)
                                       ($.cell/string "No state to pop")
                                       tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/time-push

  ;; Saves a fork of the current context which can later be restored using `$.time/pop`.

  [env _tuple]

  (update env
          :convex.run/state-stack
          (fnil conj
                '())
          (-> (env :convex.run/ctx)
              $.cvm/fork
              ($.cvm/def $.run.ctx/addr-$-trx
                         {$.run.sym/list* nil}))))
