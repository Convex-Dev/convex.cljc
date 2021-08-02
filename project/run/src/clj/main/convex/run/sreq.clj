(ns convex.run.sreq

  "Implemetation of special requests interpreted by the runner.
  
   A special requestion is merely a CVM vector respecting some particular shape that the
   runner follows typically for producing useful side-effects."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector)
           (convex.core.data.prim CVMLong)
           (convex.core.lang Context))
  (:require [convex.cell       :as $.cell]
            [convex.cvm        :as $.cvm]
            [convex.read       :as $.read]
            [convex.run.ctx    :as $.run.ctx]
            [convex.run.err    :as $.run.err]
            [convex.run.exec   :as $.run.exec]
            [convex.run.kw     :as $.run.kw]
            [convex.run.stream :as $.run.stream]
            [criterium.core    :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Helpers


(defn- -arg-long

  ;;

  [^AVector tuple]

  (.longValue ^CVMLong (.get tuple
                             2)))



(def ^:private -stream
               -arg-long)


;;;;;;;;;; Setup


(defmethod $.run.exec/sreq
  
  nil

  ;; No special request.

  [env _result]

  env)



(defmethod $.run.exec/sreq
  
  :unknown

  ;; Unknown special request, forwards an error to the error hook.

  [env tuple]

  ($.run.err/fail env
                  ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                  ($.cell/string "Unsupported special transaction")
                                  tuple)))

;;;;;;;;;; Code


(defmethod $.run.exec/sreq

  $.run.kw/read+

  ;; Reads the given string and parses to a list of forms.

  ;; TODO. Improve error reporting.
  
  [env ^AVector tuple]

  (try
    ($.run.ctx/def-result env
                          (-> (.get tuple
                                    2)
                              str
                              $.read/string+))
    (catch Throwable _err
      ($.run.err/fail env
                      ($.run.err/sreq ($.cell/code-std* :ARGUMENT)
                                      ($.cell/string "Unable to read source")
                                      tuple)))))


;;;;;;;;;; File


(defmethod $.run.exec/sreq

  $.run.kw/file-in

  [env ^AVector tuple]

  ($.run.stream/file-in env
                        (str (.get tuple
                                   2))))



(defmethod $.run.exec/sreq

  $.run.kw/file-out

  [env ^AVector tuple]

  ($.run.stream/file-out env
                         (str (.get tuple
                                    2))))


;;;;;;;;;; Logging


(defmethod $.run.exec/sreq
  
  $.run.kw/log-clear

  [env _tuple]

  (update env
          :convex.run/ctx
          (fn [ctx]
            ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                        :convex.cvm/state   ($.cvm/state ctx)}))))



(defmethod $.run.exec/sreq
  
  $.run.kw/log-get

  ;; Interns as result the current CVM log.

  [env _tuple]

  ($.run.ctx/def-result env
                        ($.cvm/log (env :convex.run/ctx))))


;;;;;;;;;; Performance


(defmethod $.run.exec/sreq

  $.run.kw/perf-bench

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
  
  ;; Interns as result the process environment map or a single request property.

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

  [env tuple]

  ($.run.stream/close env
                      (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-flush

  [env ^AVector tuple]

  ($.run.stream/flush env
                      (-stream tuple)))

(defmethod $.run.exec/sreq

  $.run.kw/stream-in

  [env tuple]

  ($.run.stream/in env
                   (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-in+

  [env tuple]

  ($.run.stream/in+ env
                    (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-line+

  [env tuple]

  ($.run.stream/in-line+ env
                         (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/stream-out

  [env ^AVector tuple]

  ($.run.stream/out env
                    (-stream tuple)
                    (.get tuple
                          3)))



(defmethod $.run.exec/sreq

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

  [env tuple]

  (update env
          :convex.run/ctx
          (fn [ctx]
            ($.cvm/time-advance ctx
                                (-arg-long tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/time-pop

  ;; Pops the CVM context saved with `(sreq/state.push)`.

  [env ^AVector tuple]

  (let [stack (env :convex.run/state-stack)]
    (if-some [ctx-restore (peek stack)]
      (-> env
          (assoc :convex.run/state-stack (pop stack)
                 :convex.run/ctx         ctx-restore)
          (as->
            env-2
            (if-some [trx (.get tuple
                                2)]
              ($.run.exec/trx env-2
                              trx)
              env-2)))
      ($.run.err/fail env
                      ($.run.err/sreq ($.cell/code-std* :STATE)
                                      ($.cell/string "No state to pop")
                                      tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/time-push

  ;; Saves the current CVM context which can later be restore with `(sreq/state.pop)`'.

  [env _tuple]

  (update env
          :convex.run/state-stack
          (fnil conj
                '())
          ($.cvm/fork (env :convex.run/ctx))))

