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
            [convex.run.stream :as $.run.stream]
            [convex.run.sym    :as $.run.sym]))


;;;;;;;;;; Helpers used in special transaction implementations


(defn restore

  "Used in some hook implementations.
  
   Helps in restoring a default hook."


  ([env kw hook]

   (restore env
            kw
            hook
            hook))


  ([env kw restore? hook]

   (cond->
     env
     restore?
     (-> (assoc kw
                hook)
         (update :convex.run/restore
                 dissoc
                 kw)))))




(defn err-stream-not-found

  ""

  [env]

  ($.run.err/signal env
                    $.run.kw/err-stream
                    ($.data/string "Stream closed or does not exist")))







(defn stream-set

  ""

  [env kw cvx-sym ^AVector tuple]

  (let [id (.longValue ^CVMLong (.get tuple
                                      2))]
    (if (some? (get-in env
                       [:convex.run/stream+
                        id]))
      (-> env
          (assoc kw
                 id)
          ($.run.ctx/def-help {cvx-sym ($.data/long id)}))
      (err-stream-not-found env))))




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

  ($.run.err/signal env
                    ($.run.err/sreq ($.data/code-std* :STATE)
                                    tuple
                                    ($.data/string "Unsupported special transaction"))))

;;;;;;;;;; Implementations


(defmethod $.run.exec/sreq

  $.run.kw/advance

  ;; Advances the timestamp.

  [env ^AVector tuple]

  (update env
          :convex.sync/ctx
          (fn [ctx]
            ($.cvm/time-advance ctx
                                (.longValue ^CVMLong (.get tuple
                                                           2))))))



(defmethod $.run.exec/sreq

  $.run.kw/dep

  ;; Specifying dependency receives special treatment at the beginning of source.
  ;;
  ;; It is illegal everywhere else.

  [env ^AVector tuple]

  ($.run.err/signal env
                    ($.run.err/sreq ($.data/code-std* :FATAL)
                                    tuple
                                    ($.data/string "CVM special command 'sreq/dep' can only be used as the very first transaction"))))



(defmethod $.run.exec/sreq

  $.run.kw/do

  ;; Executes a user given vector of transactions.

  [env ^AVector tuple]

  ($.run.exec/trx+ env
                   (.get tuple
                         2)))



(defmethod $.run.exec/sreq

  $.run.kw/env
  
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

  $.run.kw/exit

  ;; Exits process with the user given status code.

  [_env ^AVector tuple]

  (let [^CVMLong status (.get tuple
                              2)]
    (if (= (System/getenv "CONVEX_DEV")
           "true")
      (throw (ex-info "Throw instead of exit since dev mode"
                      {::status status}))
      (System/exit (.longValue status)))))



(defmethod $.run.exec/sreq
  
  $.run.kw/hook-end

  ;; Registers or removes a vector of transactions that will be executed after all transactions from source have been executed.
  ;;
  ;; Even if an error occured.

  [env ^AVector tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/end]
        hook-restore (get-in env
                             path-restore)
        trx+         (.get tuple
                           2)]
    (if (and trx+
             (not= trx+
                   [nil]))
      (let [hook-old (or hook-restore
                         (env :convex.run.hook/end))]
        (-> env
            (cond->
              (not hook-restore)
              (assoc-in path-restore
                        hook-old))
            (assoc :convex.run.hook/end
                   (fn hook-new  [env-2]
                     (hook-old ($.run.exec/trx+ (dissoc env-2
                                                        :convex.run/error)
                                                trx+))))))
      (restore env
               :convex.run.hook/end
               hook-restore))))



(defmethod $.run.exec/sreq
  
  $.run.kw/hook-error

  ;; Registers a function called with an error whenever one occurs. Must returns a transaction to execute.
  ;;
  ;; Restores default hook on nil.

  [env ^AVector tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/error]
        hook-restore (get-in env
                             path-restore)
        trx          (.get tuple
                           2)]
    (if trx
      (let [hook-old (env :convex.run.hook/error)]
        (-> env
            (cond->
              (not hook-restore)
              (assoc-in path-restore
                        hook-old))
            (assoc :convex.run.hook/error
                   (fn hook-new [env-2]
                     (-> env-2
                         (assoc :convex.run.hook/error
                                hook-old)
                         (dissoc :convex.run/error)
                         ($.run.exec/trx trx)
                         (assoc :convex.run.hook/error
                                hook-new)
                         (update :convex.run/error
                                 #(or %
                                      (env-2 :convex.run/error))))))))
      (restore env
               :convex.run.hook/error
               hook-restore))))



(defmethod $.run.exec/sreq

  $.run.kw/in

  [env ^AVector tuple]

  ($.run.stream/in env
                   (or (.get tuple
                             2)
                       (env :convex.run/in))))



(defmethod $.run.exec/sreq

  $.run.kw/in+

  [env tuple]

  ($.run.stream/in+ env
                    (or (.get tuple
                              2)
                        (env :convex.run/in))))



(defmethod $.run.exec/sreq

  $.run.kw/in-line+

  [env tuple]

  ($.run.stream/in-line+ env
                         (or (.get tuple
                                   2)
                             (env :convex.run/in))))



(defmethod $.run.exec/sreq

  $.run.kw/in-set

  [env tuple]

  (stream-set env
              :convex.run/in
              $.run.sym/in
              tuple))



(defmethod $.run.exec/sreq
  
  $.run.kw/log

  ;; Interns as result the current CVM log.

  [env _tuple]

  ($.run.ctx/def-result env
                        ($.cvm/log (env :convex.sync/ctx))))



(defmethod $.run.exec/sreq

  $.run.kw/mode-eval

  [env _tuple]

  ($.run.ctx/def-mode env
                      $.run.exec/mode-eval
                      $.run.kw/mode-eval))



(defmethod $.run.exec/sreq

  $.run.kw/mode-exec

  [env _tuple]

  ($.run.ctx/def-mode env
                      $.run.exec/mode-exec
                      $.run.kw/mode-exec))



(defmethod $.run.exec/sreq

  $.run.kw/out

  ;; Outputs the given value using the output hook.

  [env ^AVector tuple]

  ($.run.stream/out env
                    (or (.get tuple
                              2)
                        (env :convex.run/out))
                    (.get tuple
                          3)))



(defmethod $.run.exec/sreq

  $.run.kw/out!

  [env ^AVector tuple]

  ($.run.stream/out! env
                     (or (.get tuple
                               2)
                         (env :convex.run/out))
                     (.get tuple
                           3)))



(defmethod $.run.exec/sreq

  $.run.kw/out-err-set

  [env tuple]

  (stream-set env
              :convex.run/err
              $.run.sym/out-err
              tuple))



(defmethod $.run.exec/sreq

  $.run.kw/out-flush

  ;; Outputs the given value using the output hook.

  [env ^AVector tuple]

  ($.run.stream/flush env
                      (or (.get tuple
                                2)
                          (env :convex.run/out))))



(defmethod $.run.exec/sreq

  $.run.kw/out-set

  [env tuple]

  (stream-set env
              :convex.run/out
              $.run.sym/out-err
              tuple))



(defmethod $.run.exec/sreq

  $.run.kw/read+

  ;; Reads the given string and parses to a list of forms.

  ;; TODO. Improve error reporting.
  
  [env ^AVector tuple]

  (let [[err
         code] (try
                 [nil
                  (-> (.get tuple
                            2)
                      str
                      $.read/string+)]
                  (catch Throwable _err
                    [($.run.err/sreq ($.data/code-std* :ARGUMENT)
                                     tuple
                                     ($.data/string "Unable to read source"))
                     nil]))]
    (if err
      ($.run.err/signal env
                        err)
      ($.run.ctx/def-result env
                            code))))



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
      ($.run.err/signal env
                        ($.run.err/sreq ($.data/code-std* :STATE)
                                        tuple
                                        ($.data/string "No state to pop"))))))



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
  ;; In that case, the error is interned under `help/*error*` and transactions from the "catch" vector are
  ;; executed.

  ;; TODO. Remove any previous result in case of error?

  [env ^AVector tuple]

  (let [trx-catch+ (.get tuple
                         3)
        hook-error (env :convex.run.hook/error)]
    (-> env
        (assoc :convex.run.hook/error
               (fn [env-2]
                 (-> env-2
                     (dissoc :convex.run/error)
                     (cond->
                       trx-catch+
                       (-> (assoc :convex.run.hook/error
                                  hook-error)
                           ($.run.exec/trx+ trx-catch+)))
                     (update :convex.run/error
                             #(or %
                                  ::try)))))
        ($.run.exec/trx+ (.get tuple
                               2))
        (assoc :convex.run.hook/error
               hook-error)
        (as->
          env-2
          (if (identical? (env-2 :convex.run/error)
                          ::try)
            (-> env-2
                (dissoc :convex.run/error)
                ($.run.ctx/error nil))
            env-2)))))
