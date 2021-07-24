(ns convex.run.sreq

  "Implemetation of special requests interpreted by the runner.
  
   A special requestion is merely a CVM vector respecting some particular shape that the
   runner follows typically for producing useful side-effects."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector)
           (convex.core.data.prim CVMLong)
           (java.io BufferedReader))
  (:require [convex.cvm      :as $.cvm]
            [convex.data     :as $.data]
            [convex.io       :as $.io]
            [convex.read     :as $.read]
            [convex.run.ctx  :as $.run.ctx]
            [convex.run.err  :as $.run.err]
            [convex.run.exec :as $.run.exec]
            [convex.run.kw   :as $.run.kw]
            [convex.write    :as $.write]))


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


(defn stream-id

  ""

  [env kw-default tuple]

  (or (some-> ^CVMLong (.get tuple
                             2)
              .longValue)
      (env kw-default)))



(defn stream

  ""

  [env kw-default ^AVector tuple capability f]

  (if-some [id (or (some-> ^CVMLong (.get tuple
                                          2)
                           .longValue)
                   (env kw-default))]
    (try
      
      ($.run.ctx/def-result env
                            (f (get-in env
                                       [:convex.run/stream+
                                        id])))

      (catch ClassCastException _ex
        ($.run.err/signal env
                          ($.data/code-std* :ARGUMENT)
                          ($.data/string (str "Stream is missing capability: "
                                              capability))))

      (catch Throwable _ex
        ($.run.err/signal env
                          $.run.kw/err-stream
                          ($.data/string (str "Stream failed while performing: "
                                              capability)))))
    (err-stream-not-found env)))



(defn stream-set

  ""

  [env kw ^AVector tuple]

  (let [id (.longValue ^CVMLong (.get tuple
                                      2))]
    (if (some? (get-in env
                       [:convex.run/stream+
                        id]))
      (assoc env
             kw
             id)
      (err-stream-not-found env))))



;;;;;;;;;; Setup


(defmethod $.run.exec/sreq
  
  nil

  ;; No special request, simply finalizes a regular transaction and evaluates result hook (if present).

  [env _result]

  (if-some [hook (env :convex.run.hook/result)]
    (-> env
        (dissoc :convex.run.hook/result)
        ($.run.exec/trx hook)
        (assoc :convex.run.hook/result
               hook))
    env))



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
        f            (.get tuple
                           2)]
    (if f
      (-> env
          (cond->
            (not hook-restore)
            (assoc-in path-restore
                      (env :convex.run.hook/error)))
          (assoc :convex.run.hook/error
                 (fn hook [env-2]
                   (let [err   (env-2 :convex.run/error)
                         ctx   ($.cvm/invoke (-> (or (env-2 :convex.sync/ctx)
                                                     (env-2 :convex.sync/ctx-base))
                                                 $.cvm/juice-refill)
                                             f
                                             ($.cvm/arg+* err))
                         env-3 (assoc env-2
                                      :convex.sync/ctx
                                      ctx)
                         ex    ($.cvm/exception ctx)]
                     (if ex
                       ($.run.err/fatal env-3
                                        ($.run.err/error ex)
                                        ;err
                                        ($.data/string "Calling error hook failed")
                                        (-> ex
                                            $.run.err/error
                                            ($.run.err/assoc-cause (env-2 :convex.run/error))))
                       (let [form  ($.cvm/result ctx)
                             env-4 (-> env-3
                                       (assoc :convex.run.hook/error
                                              identity)
                                       (dissoc :convex.run/error)
                                       ($.run.exec/sreq form)
                                       (assoc :convex.run.hook/error
                                              hook))
                             err-2 (env-4 :convex.run/error)]
                         (if err-2
                           ($.run.err/fatal env-4
                                            form
                                            ($.data/string "Evaluating output from error hook failed")
                                            ($.run.err/assoc-cause err-2
                                                                   err))
                           (assoc env-4
                                  :convex.run/error
                                  err))))))))
      (restore env
               :convex.run.hook/error
               hook-restore))))



(defmethod $.run.exec/sreq
  
  $.run.kw/hook-out

  ;; Registers a function called with a value whenever it has to be outputted. Returns a possibly modified value.
  ;;
  ;; Restores default hook on nil.

  [env ^AVector tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/out]
        hook-restore (get-in env
                             path-restore)
        f            (.get tuple
                           2)]
    (if f
      (let [hook-old (or hook-restore
                         (env :convex.run.hook/out))]
        (-> env
            (cond->
              (not hook-restore)
              (assoc-in path-restore
                        hook-old))
            (assoc :convex.run.hook/out
                   (fn hook-new [env-2 x]
                     (let [ctx    ($.cvm/invoke (-> env-2
                                                    :convex.sync/ctx
                                                    $.cvm/juice-refill)
                                                f
                                                ($.cvm/arg+* x))
                           env-3  (assoc env-2
                                         :convex.sync/ctx
                                         ctx)
                           ex     ($.cvm/exception ctx)]
                       (if ex
                         (-> env-3
                             (assoc :convex.run.hook/out
                                    hook-old)
                             ($.run.err/fatal ($.data/list [f
                                                            x])
                                              ($.data/string "Calling output hook failed, using default output")
                                              ($.run.err/error ex))
                             (assoc :convex.run.hook/out
                                    hook-new))
                         (hook-old env-3
                                   ($.cvm/result ctx))))))))
      (restore env
               :convex.run.hook/out
               hook-restore))))



(defmethod $.run.exec/sreq
  
  $.run.kw/hook-result

  ;; Registers a transaction that will be executed after every transaction in source that does not result
  ;; in a special request.
  ;;
  ;; Removes hook on nil.

  [env ^AVector tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/result]
        trx-restore  (get-in env
                             path-restore
                             ::nil)
        trx-restore? (not (identical? trx-restore
                                      ::nil))
        trx-new      (.get tuple
                           2)]
    (if trx-new
      (-> env
          (cond->
            (not trx-restore?)
            (assoc-in path-restore
                      (env :convex.run.hook/result)))
          (assoc :convex.run.hook/result
                 trx-new))
      (restore env
               :convex.run.hook/result
               trx-restore?
               trx-restore))))



(defmethod $.run.exec/sreq

  $.run.kw/in

  [env tuple]

  (stream env
          :convex.run/in
          tuple
          "read"
          (fn [[stream mode]]
            ((case mode
               :bin $.read/stream-bin
               :txt $.read/stream-txt)
             stream))))



(defmethod $.run.exec/sreq

  $.run.kw/in+

  [env tuple]

  (stream env
          :convex.run/in
          tuple
          "read"
          (fn [[stream mode]]
            ((case mode
               :bin $.read/stream-bin+
               :txt $.read/stream-txt+)
             stream))))



(defmethod $.run.exec/sreq

  $.run.kw/in-line

  [env tuple]

  (stream env
          :convex.run/in
          tuple
          "read line"
          (fn [[stream _mode]]
            (-> stream
                BufferedReader.
                $.read/line))))



(defmethod $.run.exec/sreq

  $.run.kw/in-set

  [env tuple]

  (stream-set env
              :convex.run/in
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

  (stream env
          :convex.run/out
          tuple
          "write"
          (fn [[stream mode]]
            (let [cell (.get tuple
                             3)]
              ((case mode
                 :bin $.write/stream-bin
                 :txt $.write/stream-txt)
               stream
               cell)
              cell))))



(defmethod $.run.exec/sreq

  $.run.kw/out!

  [env ^AVector tuple]

  (stream env
          :convex.run/out
          tuple
          "write"
          (fn [[stream mode]]
            (let [cell (.get tuple
                             3)]
              (case mode
                :bin ($.write/stream-bin stream
                                         cell)
                :txt (do
                       ($.write/stream-txt stream
                                           cell)
                       ($.io/newline stream)))
              ($.io/flush stream)
              cell))))



(defmethod $.run.exec/sreq

  $.run.kw/out-flush

  ;; Outputs the given value using the output hook.

  [env ^AVector tuple]

  (stream env
          :convex.run/out
          tuple
          "flush"
          (fn [[stream _mode]]
            ($.io/flush stream)
            nil)))



(defmethod $.run.exec/sreq

  $.run.kw/out-set

  [env tuple]

  (stream-set env
              :convex.run/out
              tuple))



(defmethod $.run.exec/sreq

  $.run.kw/read

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
