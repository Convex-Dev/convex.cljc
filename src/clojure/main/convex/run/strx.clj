(ns convex.run.strx

  "Special transactions interpreted by the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.lang Reader))
  (:require [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.run.ctx  :as $.run.ctx]
            [convex.run.err  :as $.run.err]
            [convex.run.exec :as $.run.exec]
            [convex.run.sym  :as $.run.sym]))

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

  [_env _form]

  nil)



(defmethod $.run.exec/strx :unknown

  [env form]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/ARGUMENT
                                    form
                                    ($.code/string "Unsupported special transaction"))))

;;;;;;;;;; Implementations


(defmethod $.run.exec/strx

  $.run.sym/dep

  [env trx]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/STATE
                                    trx
                                    ($.code/string "CVM special command 'cvm.dep' can only be used as the very first transaction"))))



(defmethod $.run.exec/strx

  $.run.sym/do

  [env trx]

  ($.run.exec/trx+ env
                   (rest trx)))



(defmethod $.run.exec/strx

  $.run.sym/env

  [env trx]

  (let [sym (second trx)]
    (if ($.code/symbol? sym)
      (if-some [k (when (= (count trx)
                           3)
                    (nth (seq trx)
                         2))]
        (if ($.code/string? k)
          ($.run.exec/trx env
                          ($.code/def sym
                                      ($.code/string (System/getenv (str k)))))
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "Second argument to 'cvm.env' must be a string"))))
        ($.run.exec/trx env
                        ($.code/def sym
                                    ($.code/map (map (fn [[k v]]
                                                       [($.code/string k)
                                                        ($.code/string v)])
                                                     (System/getenv))))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/CAST
                                        trx
                                        ($.code/string "First argument to 'cvm.env' must be a symbol"))))))



(defmethod $.run.exec/strx
  
  $.run.sym/hook-end

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/end]
        hook-restore (get-in env
                             path-restore)
        trx+         (next trx)]
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
  
  $.run.sym/hook-error

  ;; TODO. Ensure failing hook is handled properly.

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/error]
        hook-restore (get-in env
                             path-restore)
        form-hook    (second trx)]
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
  
  $.run.sym/hook-out

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/out]
        hook-restore (get-in env
                             path-restore)
        form-hook    (second trx)]
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
  
  $.run.sym/hook-trx

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run.hook/trx]
        hook-restore (get-in env
                             path-restore)
        form-hook    (second trx)
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
  
  $.run.sym/log

  ;; TODO. Error handling.

  [env trx]

  (if-some [cvm-sym (second trx)]
    ($.run.exec/trx env
                    ($.code/def cvm-sym
                                ($.cvm/log (env :convex.sync/ctx))))
    ($.run.err/signal env
                      ($.run.err/strx ErrorCodes/ARGUMENT
                                      trx
                                      ($.code/string "Argument for 'cvm.log' must be symbol for defining the log")))))



(defmethod $.run.exec/strx

  $.run.sym/out

  [env trx]

  (if-some [form (second trx)]
    (let [env-2 ($.run.exec/trx env
                                form)]
      (if (env-2 :convex.run/error)
        env-2
        ((env-2 :convex.run.hook/out)
         env-2
         ($.run.exec/result env-2))))
    env))



(defmethod $.run.exec/strx
  
  $.run.sym/read

  [env trx]

  (if-some [sym (second trx)]
    (if ($.code/symbol? sym)
      (if-some [src (when (= (count trx)
                             3)
                      (nth (seq trx)
                           2))]
        (if ($.code/string? src)
          (try
            ($.run.exec/trx env
                            ($.code/def sym
                                        (-> src
                                            str
                                            Reader/readAll
                                            $.code/vector
                                            $.code/quote)))
            (catch Throwable _err
              ($.run.err/signal env
                                ($.run.err/strx ErrorCodes/ARGUMENT
                                                trx
                                                ($.code/string "Cannot read source")))))
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "Second argument to 'cvm.read' must be source code (a string)"))))
        ($.run.err/signal env
                          ($.run.err/strx ErrorCodes/ARGUMENT
                                          trx
                                          ($.code/string "'cvm.read' is missing a source string"))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/CAST
                                        trx
                                        ($.code/string "First argument to 'cvm.read' must be a symbol"))))
    ($.run.err/signal env
                      ($.run.err/strx ErrorCodes/ARGUMENT
                                      trx
                                      ($.code/string "'cvm.read' is missing a symbol to define")))))



(defmethod $.run.exec/strx
  
  $.run.sym/splice

  [env trx]

  (let [env-2 ($.run.exec/trx env
                              (second trx))]
    (if (env-2 :convex.run/error)
      env-2
      (let [result ($.run.exec/result env-2)]
        (if ($.code/vector? result)
          ($.run.exec/trx+ env-2
                           result)
          ($.run.err/signal env-2
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "In 'cvm.splice', argument must evaluate to a vector of transactions"))))))))



(defmethod $.run.exec/strx
  
  $.run.sym/screen-clear

  [env _trx]

  (screen-clear)
  env)



(defmethod $.run.exec/strx
  
  $.run.sym/try

  [env trx]

  (let [trx-last (last trx)
        catch?   (= ($.run.exec/strx-dispatch trx-last)
                    $.run.sym/catch)
        on-error (env :convex.run.hook/error)]
    (-> env
        (assoc :convex.run.hook/error
               (fn [env-2]
                 (-> env-2
                     (dissoc :convex.run/error)
                     (cond->
                       catch?
                       (-> (assoc :convex.run.hook/error
                                  on-error)
                           ($.run.exec/trx+ (rest trx-last))))
                     (update :convex.run/error
                             #(or %
                                  ::try)))))
        ($.run.exec/trx+ (-> trx
                             rest
                             (cond->
                               catch?
                               butlast)))
        (assoc :convex.run.hook/error
               on-error)
        (as->
          env-2
          (if (identical? (env-2 :convex.run/error)
                          ::try)
            (-> env-2
                (dissoc :convex.run/error)
                ($.run.ctx/def-error nil))
            env-2)))))
