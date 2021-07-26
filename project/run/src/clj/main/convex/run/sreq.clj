(ns convex.run.sreq

  "Implemetation of special requests interpreted by the runner.
  
   A special requestion is merely a CVM vector respecting some particular shape that the
   runner follows typically for producing useful side-effects."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector)
           (convex.core.data.prim CVMLong))
  (:require [convex.cvm        :as $.cvm]
            [convex.data       :as $.data]
            [convex.read       :as $.read]
            [convex.run.ctx    :as $.run.ctx]
            [convex.run.err    :as $.run.err]
            [convex.run.exec   :as $.run.exec]
            [convex.run.kw     :as $.run.kw]
            [convex.run.stream :as $.run.stream]))


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
                  ($.run.err/sreq ($.data/code-std* :ARGUMENT)
                                  ($.data/string "Unsupported special transaction")
                                  tuple)))

;;;;;;;;;; Implementations


(defmethod $.run.exec/sreq

  $.run.kw/advance

  ;; Advances the timestamp.

  [env tuple]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/time-advance ctx
                                (-arg-long tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/close

  [env ^AVector tuple]

  (let [id (-stream tuple)]
    (-> env
        ($.run.stream/close id)
        (update :convex.run/stream+
                dissoc
                id))))



(defmethod $.run.exec/sreq

  $.run.kw/dep

  ;; Specifying dependency receives special treatment at the beginning of source.
  ;;
  ;; It is illegal everywhere else.

  [env ^AVector tuple]

  ($.run.err/fail env
                  ($.run.err/sreq ($.data/code-std* :FATAL)
                                  ($.data/string "CVM special command 'sreq/dep' can only be used as the very first transaction")
                                  tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/do

  ;; Executes a user given vector of transactions.

  [env ^AVector tuple]

  ($.run.exec/trx+ env
                   (.get tuple
                         2)))



(defmethod $.run.exec/sreq

  $.run.kw/file.in

  [env ^AVector tuple]

  ($.run.stream/file-in env
                        (str (.get tuple
                                   2))))



(defmethod $.run.exec/sreq

  $.run.kw/file.out

  [env ^AVector tuple]

  ($.run.stream/file-out env
                         (str (.get tuple
                                    2))))



(defmethod $.run.exec/sreq

  $.run.kw/in

  [env tuple]

  ($.run.stream/in env
                   (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/in+

  [env tuple]

  ($.run.stream/in+ env
                    (-stream tuple)))



(defmethod $.run.exec/sreq

  $.run.kw/in-line+

  [env tuple]

  ($.run.stream/in-line+ env
                         (-stream tuple)))



(defmethod $.run.exec/sreq
  
  $.run.kw/log

  ;; Interns as result the current CVM log.

  [env _tuple]

  ($.run.ctx/def-result env
                        ($.cvm/log (env :convex.sync/ctx))))



(defmethod $.run.exec/sreq

  $.run.kw/monitor

  [env ^AVector tuple]

  ($.run.exec/trx-monitor env
                          (.get tuple
                                2)))



(defmethod $.run.exec/sreq

  $.run.kw/out

  ;; Outputs the given value using the output hook.

  [env ^AVector tuple]

  ($.run.stream/out env
                    (-stream tuple)
                    (.get tuple
                          3)))



(defmethod $.run.exec/sreq

  $.run.kw/out!

  [env ^AVector tuple]

  ($.run.stream/out! env
                     (-stream tuple)
                     (.get tuple
                           3)))



(defmethod $.run.exec/sreq

  $.run.kw/out-flush

  ;; Outputs the given value using the output hook.

  [env ^AVector tuple]

  ($.run.stream/flush env
                      (-stream tuple)))



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
                                  $.data/string)
                          ($.data/map (map (fn [[k v]]
                                             [($.data/string k)
                                              ($.data/string v)])
                                           (System/getenv))))))



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
                      ($.run.err/sreq ($.data/code-std* :ARGUMENT)
                                      ($.data/string "Unable to read source")
                                      tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/state-pop

  ;; Pops the CVM context saved with `(sreq/state.push)`.

  [env ^AVector tuple]

  (let [stack (env :convex.run/state-stack)]
    (if-some [ctx-restore (peek stack)]
      (-> env
          (assoc :convex.run/state-stack (pop stack)
                 :convex.sync/ctx         ctx-restore)
          (as->
            env-2
            (if-some [trx (.get tuple
                                2)]
              ($.run.exec/trx env-2
                              trx)
              env-2)))
      ($.run.err/fail env
                      ($.run.err/sreq ($.data/code-std* :STATE)
                                      ($.data/string "No state to pop")
                                      tuple)))))



(defmethod $.run.exec/sreq

  $.run.kw/state-push

  ;; Saves the current CVM context which can later be restore with `(sreq/state.pop)`'.

  [env _tuple]

  (update env
          :convex.run/state-stack
          (fnil conj
                '())
          ($.cvm/fork (env :convex.sync/ctx))))



(defmethod $.run.exec/sreq
  
  $.run.kw/try

  ;; Provides a try-catch mechanism.
  ;;
  ;; First vector of transaction if for trying and stops as soon as an error occurs.
  ;; In that case, the error is interned under `env/*error*` and transactions from the "catch" vector are
  ;; executed.

  ;; TODO. Remove any previous result in case of error?

  [env ^AVector tuple]

  (let [trx-catch (.get tuple
                        3)
        fail      (env :convex.run/fail)]
    (-> env
        (assoc :convex.run/fail
               (fn [env-2]
                 (-> env-2
                     (dissoc :convex.run/error)
                     (cond->
                       trx-catch
                       (-> (assoc :convex.run/fail
                                  fail)
                           ($.run.exec/trx trx-catch)))
                     (update :convex.run/error
                             #(or %
                                  ::try)))))
        ($.run.exec/trx (.get tuple
                              2))
        (assoc :convex.run/fail
               fail)
        (as->
          env-2
          (if (identical? (env-2 :convex.run/error)
                          ::try)
            (-> env-2
                (dissoc :convex.run/error)
                ($.run.ctx/error nil))
            env-2)))))
