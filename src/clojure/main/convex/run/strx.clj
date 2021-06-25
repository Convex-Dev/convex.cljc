(ns convex.run.strx

  "Special transactions interpreted by the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.data AVector))
  (:require [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.run.ctx  :as $.run.ctx]
            [convex.run.err  :as $.run.err]
            [convex.run.exec :as $.run.exec]
            [convex.run.kw   :as $.run.kw]))

;;;;;;;;;; Miscellaneous


(defn screen-clear

  ""

  ;; https://www.delftstack.com/howto/java/java-clear-console/

  []

  (print "\033[H\033[2J")
  (flush))


;;;;;;;;;; Helpers used in special transaction implementations


(defn restore

  ""

  [env kw hook]

  (cond->
    env
    hook
    (-> (assoc kw
               hook)
        (update :convex.run/restore
                dissoc
                kw))))


;;;;;;;;;; Setup


(defmethod $.run.exec/strx nil

  [env _tuple]

  env)



(defmethod $.run.exec/strx :unknown

  [env form]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/ARGUMENT
                                    form
                                    ($.code/string "Unsupported special transaction"))))

;;;;;;;;;; Implementations


(defmethod $.run.exec/strx

  $.run.kw/dep

  [env trx]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/STATE
                                    trx
                                    ($.code/string "CVM special command 'strx/dep' can only be used as the very first transaction"))))



(defmethod $.run.exec/strx

  $.run.kw/do

  [env tuple]

  ($.run.exec/trx+ env
                   (.get tuple
                         2)))



(defmethod $.run.exec/strx

  $.run.kw/env

  [env tuple]

  (let [sym (.get tuple
                  2)]
    (if ($.code/symbol? sym)
      (if-some [k (.get tuple
                        3)]
        (if ($.code/string? k)
          ($.run.ctx/def-current env
                                 {sym ($.code/string (System/getenv (str k)))})
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/CAST
                                            tuple
                                            ($.code/string "Second argument to 'strx/env' must be a string"))))
        ($.run.exec/trx env
                        ($.code/def sym
                                    ($.code/map (map (fn [[k v]]
                                                       [($.code/string k)
                                                        ($.code/string v)])
                                                     (System/getenv))))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/CAST
                                        tuple
                                        ($.code/string "First argument to 'strx/env' must be a symbol"))))))



(defmethod $.run.exec/strx
  
  $.run.kw/hook-end

  [env tuple]

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



(defmethod $.run.exec/strx
  
  $.run.kw/hook-error

  ;; TODO. Ensure failing hook is handled properly.

  [env tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/error]
        hook-restore (get-in env
                             path-restore)
        form-hook    (.get tuple
                           2)]
    (if form-hook
      (let [env-2 ($.run.exec/trx env
                                  form-hook)]
        (if (env-2 :convex.run/error)
          env-2
          (-> env-2
              (cond->
                (not hook-restore)
                (assoc-in path-restore
                          (env-2 :convex.run.hook/error)))
              (assoc :convex.run.hook/error
                     (let [form-hook-2 ($.run.exec/result env-2)]
                       (fn hook [env-3]
                         (let [cause (env-3 :convex.run/error)
                               form  ($.code/list [form-hook-2
                                                   ($.code/quote (env-3 :convex.run/error))])
                               env-4 (-> env-3
                                         (dissoc :convex.run/error)
                                         (assoc :convex.run.hook/error
                                                identity)
                                         ($.run.exec/trx form))
                               err   (env-4 :convex.run/error)]
                           (-> (if err
                                 ($.run.err/fatal env-4
                                                  form
                                                  ($.code/string "Calling error hook failed")
                                                  cause)
                                 (let [form-2 ($.run.exec/result env-4)
                                       env-5  ($.run.exec/trx env-4
                                                              form-2)]
                                   (if (env-5 :convex.run/error)
                                     ($.run.err/fatal env-5
                                                      form-2
                                                      ($.code/string "Evaluating output from error hook failed")
                                                      cause)
                                     (assoc env-5
                                            :convex.run/error
                                            cause))))
                               (assoc :convex.run.hook/error
                                      hook)))))))))
      (restore env
               :convex.run.hook/error
               hook-restore))))



(defmethod $.run.exec/strx
  
  $.run.kw/hook-out

  [env tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/out]
        hook-restore (get-in env
                             path-restore)
        form-hook    (.get tuple
                           2)]
    (if form-hook
      (let [env-2 ($.run.exec/trx env
                                  form-hook)]
        (if (env-2 :convex.run/error)
          env-2
          (let [hook-old (or hook-restore
                             (env-2 :convex.run.hook/out))]
            (-> env-2
                (cond->
                  (not hook-restore)
                  (assoc-in path-restore
                            hook-old))
                (assoc :convex.run.hook/out
                       (let [form-hook-2 ($.run.exec/result env-2)]
                         (fn hook-new [env-3 x]
                           (let [hook-error (env-3 :convex.run.hook/error)
                                 form       ($.code/list [form-hook-2
                                                          ($.code/quote x)])
                                 env-4      (-> env-3
                                                (assoc :convex.run.hook/error
                                                       identity)
                                                ($.run.exec/trx form)
                                                (assoc :convex.run.hook/error
                                                       hook-error))
                                 err        (env-4 :convex.run/error)]
                             (if (identical? err
                                             (env-3 :convex.run/error))
                               (hook-old env-4
                                         ($.run.exec/result env-4))
                               (-> env-4
                                   (assoc :convex.run.hook/out
                                          hook-old)
                                   ($.run.err/fatal form
                                                    ($.code/string "Calling output hook failed, using default output")
                                                    err)
                                   (assoc :convex.run.hook/out
                                          hook-new)))))))))))
      (restore env
               :convex.run.hook/out
               hook-restore))))



(defmethod $.run.exec/strx
  
  $.run.kw/hook-trx

  [env tuple]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/trx]
        hook-restore (get-in env
                             path-restore)
        form-hook    (.get tuple
                           2)
        env-2        (restore env
                              :convex.run.hook/trx
                              hook-restore)]
    (if (some? form-hook)
      (let [env-3 ($.run.exec/trx env-2
                                  form-hook)]
        (if (env-3 :convex.run/error)
          env-3
          (let [hook-old (or hook-restore
                             (env-3 :convex.run.hook/trx))]
            (-> env-3
                (assoc-in path-restore
                          hook-old)
                (assoc :convex.run.hook/trx
                       (let [form-hook-2 ($.run.exec/result env-3)]
                         (fn hook-new [env-4 form]
                           (let [env-5 (-> env-4
                                           (assoc :convex.run.hook/trx
                                                  hook-old)
                                           ($.run.exec/trx ($.code/list [form-hook-2
                                                                         ($.code/quote form)])))]
                             (-> (if (env-5 :convex.run/error)
                                   env-5
                                   ($.run.exec/trx env-5))
                                 (assoc :convex.run.hook/trx
                                        hook-new))))))))))
      env-2)))



(defmethod $.run.exec/strx
  
  $.run.kw/log

  ;; TODO. Error handling.

  [env tuple]

  (if-some [sym (.get tuple
                      2)]
    ($.run.ctx/def-current env
                           {sym ($.cvm/log (env :convex.sync/ctx))})
    ($.run.err/signal env
                      ($.run.err/strx ErrorCodes/ARGUMENT
                                      tuple
                                      ($.code/string "Argument for 'strx/log' must be symbol for defining the log")))))



(defmethod $.run.exec/strx

  $.run.kw/out

  [env ^AVector tuple]

  ((env :convex.run.hook/out)
   env
   (.get tuple
         2)))



(defmethod $.run.exec/strx

  $.run.kw/read
  
  [env tuple]
  
  (if-some [sym (.get tuple
                      2)]
      (if ($.code/symbol? sym)
        (if-some [form-src (.get tuple
                                 3)]
          (let [env-2 ($.run.exec/trx env
                                      form-src)]
            (if (env-2 :convex.run/error)
              env-2
              (let [src ($.run.exec/result env-2)]
                (if ($.code/string? src)
                  (try
                    ($.run.ctx/def-current env
                                           {sym (-> src
                                                    str
                                                    $.cvm/read
                                                    $.code/vector)})
                    (catch Throwable _err
                      ($.run.err/signal env
                                        ($.run.err/strx ErrorCodes/ARGUMENT
                                                        tuple
                                                        ($.code/string "Unable to read source")))))
                  ($.run.err/signal env
                                    ($.run.err/strx ErrorCodes/CAST
                                                    tuple
                                                    ($.code/string "Second argument to 'strx/read' must evaluate to source code (a string)")))))))
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/ARGUMENT
                                            tuple
                                            ($.code/string "'strx/read' is missing the source argument"))))
        ($.run.err/signal env
                          ($.run.err/strx ErrorCodes/CAST
                                          tuple
                                          ($.code/string "First argument to 'strx/read' must be a symbol"))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/ARGUMENT
                                        tuple
                                        ($.code/string "'strx/read' is missing a symbol to define")))))



(defmethod $.run.exec/strx
  
  $.run.kw/splice

  [env tuple]

  (let [env-2 ($.run.exec/trx env
                              (.get tuple
                                    2))]
    (if (env-2 :convex.run/error)
      env-2
      (let [result ($.run.exec/result env-2)]
        (if ($.code/vector? result)
          ($.run.exec/trx+ env-2
                           result)
          ($.run.err/signal env-2
                            ($.run.err/strx ErrorCodes/CAST
                                            tuple
                                            ($.code/string "In 'strx/splice', argument must evaluate to a vector of transactions"))))))))



(defmethod $.run.exec/strx
  
  $.run.kw/screen-clear

  [env _tuple]

  (screen-clear)
  env)



(defmethod $.run.exec/strx
  
  $.run.kw/try

  [env tuple]

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
