(ns convex.shell.sreq

  "Implementation of requests interpreted by the shell between transactions.
  
   A reqest is merely a CVX vector following some particular convention that the
   shell follows for producing effects beyond the scope of the CVM."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector)
           (convex.core.data.prim CVMLong)
           (convex.core.lang Context))
  (:require [convex.cell         :as $.cell]
            [convex.cvm          :as $.cvm]
            [convex.db           :as $.db]
            [convex.read         :as $.read]
            [convex.shell.ctx    :as $.shell.ctx]
            [convex.shell.err    :as $.shell.err]
            [convex.shell.exec   :as $.shell.exec]
            [convex.shell.kw     :as $.shell.kw]
            [convex.shell.stream :as $.shell.stream]
            [convex.shell.sym    :as $.shell.sym]
            [criterium.core      :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Helpers


(defn- -stream

  ;; Given a request, returns the stream values it contains as a Java long.

  [^AVector tuple]

  (.longValue ^CVMLong (.get tuple
                             2)))


;;;;;;;;;; Setup


(defmethod $.shell.exec/sreq
  
  nil

  ;; No request, simply finalizes a regular transactions.

  [env result]

  ($.shell.ctx/def-result env
                          result))



(defmethod $.shell.exec/sreq
  
  :unknown

  ;; Unknown request, consided as failure.

  [env tuple]

  ($.shell.exec/fail env
                     ($.shell.err/sreq ($.cell/code-std* :ARGUMENT)
                                       ($.cell/string "Unsupported special transaction")
                                       tuple)))

;;;;;;;;;; Code


(defmethod $.shell.exec/sreq

  $.shell.kw/code-read+

  ;; Reads the given string and parses it to a list of forms.

  ;; TODO. Improve error reporting.
  
  [env ^AVector tuple]

  (try
    ($.shell.ctx/def-result env
                          (-> (.get tuple
                                    2)
                              (str)
                              ($.read/string+)))
    (catch Throwable _err
      ($.shell.exec/fail env
                         ($.shell.err/sreq ($.cell/code-std* :ARGUMENT)
                                           ($.cell/string "Unable to read source")
                                           tuple)))))


;;;;;;;;;; Etch


(defn- -db

  ;;

  [env f]

  (-> env
      (update :convex.shell.db/instance
              (fn [instance]
                (or instance
                    ($.db/current-set ($.db/open-tmp "convex-shell")))))
      ($.shell.ctx/def-result (f))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-flush

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.db/flush)
         nil)))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-path

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.cell/string ($.db/path)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-read

  ;;

  [env ^AVector tuple]

  (-db env
       (fn []
         ($.db/read (.get tuple
                          2)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-root-read

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.db/root-read))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-root-write

  ;;

  [env ^AVector tuple]

  (-db env
       (fn []
         ($.db/root-write (.get tuple
                                2)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-write

  ;;

  [env ^AVector tuple]

  (-db env
       ($.db/write (.get tuple
                         2))))


;;;;;;;;;; File


(defmethod $.shell.exec/sreq

  $.shell.kw/file-stream-in

  ;; Opens a file for reading.

  [env ^AVector tuple]

  ($.shell.stream/file-in env
                          (str (.get tuple
                                     2))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-stream-out

  ;; Opens a file for writing.

  [env ^AVector tuple]

  ($.shell.stream/file-out env
                           (str (.get tuple
                                       2))))


;;;;;;;;;; Logging


(defmethod $.shell.exec/sreq
  
  $.shell.kw/log-clear

  ;; Clears the CVM log.

  [env _tuple]

  (let [ctx   (env :convex.shell/ctx)
        ctx-2 ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                          :convex.cvm/state   ($.cvm/state ctx)})]
    (-> env
        (assoc :convex.shell/ctx
               ctx-2)
        ($.shell.ctx/def-result ($.cvm/log ctx-2)))))



(defmethod $.shell.exec/sreq
  
  $.shell.kw/log-get

  ;; Interns the current state of the CVM log under `$/*result*`.

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cvm/log (env :convex.shell/ctx))))


;;;;;;;;;; Performance


(defmethod $.shell.exec/sreq

  $.shell.kw/perf-bench

  ;; Benchmarks a transaction using Criterium.

  [env ^AVector tuple]

  (let [ctx   ($.cvm/fork (env :convex.shell/ctx))
        cell  (.get tuple
                    2)
        stat+ (criterium/benchmark* (fn []
                                      (.query ^Context ctx
                                              cell))
                                    {})]
    ($.shell.ctx/def-result env
                            ($.cell/map {($.cell/keyword "mean")   ($.cell/double (first (stat+ :mean)))
                                         ($.cell/keyword "stddev") ($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))



(defmethod $.shell.exec/sreq

  $.shell.kw/perf-track

  ;; Tracks juice consumption of the given transaction.

  [env ^AVector tuple]

  ($.shell.exec/trx-track env
                          (.get tuple
                                2)))


;;;;;;;;;; Process


(defmethod $.shell.exec/sreq

  $.shell.kw/process-exit

  ;; Exits process with the user given status code.

  [_env ^AVector tuple]

  (let [status (.longValue ^CVMLong (.get tuple
                                          2))]
    (if (= (System/getenv "CONVEX_DEV")
           "true")
      (throw (ex-info "Throw instead of exit since dev mode"
                      {::status status}))
      (System/exit status))))



(defmethod $.shell.exec/sreq

  $.shell.kw/process-env
  
  ;; Interns under `$/*result*` the process environment map or a single requested variable.

  [env ^AVector tuple]

  ($.shell.ctx/def-result env
                          (if-some [env-var (.get tuple
                                                  2)]
                            (some-> (System/getenv (str env-var))
                                    ($.cell/string))
                            ($.cell/map (map (fn [[k v]]
                                               [($.cell/string k)
                                                ($.cell/string v)])
                                             (System/getenv))))))


;;;;;;;;;; Streams


(defmethod $.shell.exec/sreq

  $.shell.kw/stream-close

  ;; Closes the given stream.

  [env tuple]

  ($.shell.stream/close env
                        (-stream tuple)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-flush

  ;; Flushes the given stream.

  [env ^AVector tuple]

  ($.shell.stream/flush env
                        (-stream tuple)))

(defmethod $.shell.exec/sreq

  $.shell.kw/stream-in

  ;; Reads a single cell from the given stream.

  [env tuple]

  ($.shell.stream/in env
                     (-stream tuple)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-in+

  ;; Reads all available cells from the given stream.

  [env tuple]

  ($.shell.stream/in+ env
                      (-stream tuple)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-line+

  ;; Reads line from the given stream and extracts all available cells.

  [env tuple]

  ($.shell.stream/line+ env
                        (-stream tuple)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-out

  ;; Writes a cell to the given stream.

  [env ^AVector tuple]

  ($.shell.stream/out env
                      (-stream tuple)
                      (.get tuple
                            3)))



(defmethod $.shell.exec/sreq

  ;; Writes a cell to the given stream, appends a new line, and flushes everything.

  $.shell.kw/stream-outln

  [env ^AVector tuple]

  ($.shell.stream/out! env
                       (-stream tuple)
                       (.get tuple
                             3)))


;;;;;;;;;; Time


(defmethod $.shell.exec/sreq

  $.shell.kw/time-advance

  ;; Advances the timestamp.

  [env ^AVector tuple]

  (let [^CVMLong interval (.get tuple
                                2)]
    (-> env
        (update :convex.shell/ctx
                (fn [ctx]
                  ($.cvm/time-advance ctx
                                      (.longValue interval))))
        ($.shell.ctx/def-result interval))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-pop

  ;; Pops the last context saved with `$.time/push`.

  [env ^AVector tuple]

  (let [stack (env :convex.shell/state-stack)]
    (if-some [ctx-restore (peek stack)]
      (-> env
          (assoc :convex.shell/state-stack (pop stack)
                 :convex.shell/ctx         ctx-restore)
          ($.shell.ctx/def-trx+ ($.cell/list [(.get tuple
                                                  2)])))
      ($.shell.exec/fail env
                         ($.shell.err/sreq ($.cell/code-std* :STATE)
                                           ($.cell/string "No state to pop")
                                           tuple)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-push

  ;; Saves a fork of the current context which can later be restored using `$.time/pop`.

  [env _tuple]

  (update env
          :convex.shell/state-stack
          (fnil conj
                '())
          (-> (env :convex.shell/ctx)
              ($.cvm/fork)
              ($.cvm/def $.shell.ctx/addr-$-trx
                         {$.shell.sym/list* nil}))))
