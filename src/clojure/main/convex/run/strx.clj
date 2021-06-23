(ns convex.run.strx

  "Special transactions interpreted by the runner."

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.lang Reader))
  (:require [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.run.err  :as $.run.err]
            [convex.run.exec :as $.run.exec]
            [convex.run.sym  :as $.run.sym]))


;;;;;;;;;;


(defn dep

  ""

  [env trx]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/STATE
                                    trx
                                    ($.code/string "CVM special command 'cvm.dep' can only be used as the very first transaction"))))



(defn do'

  ""

  [env trx]

  ($.run.exec/eval-trx+ env
                        (rest trx)))



(defn env


  ""

  [env trx]

  (let [sym (second trx)]
    (if ($.code/symbol? sym)
      (if-some [k (when (= (count trx)
                           3)
                    (nth (seq trx)
                         2))]
        (if ($.code/string? k)
          ($.run.exec/eval-form env
                                ($.code/def sym
                                            ($.code/string (System/getenv (str k)))))
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "Second argument to 'cvm.env' must be a string"))))
        ($.run.exec/eval-form env
                              ($.code/def sym
                                          ($.code/map (map (fn [[k v]]
                                                             [($.code/string k)
                                                              ($.code/string v)])
                                                           (System/getenv))))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/CAST
                                        trx
                                        ($.code/string "First argument to 'cvm.env' must be a symbol"))))))



(defn hook-end

  ""

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run/end]
        restore      (get-in env
                             path-restore)
        trx+         (next trx)]
    (if (and trx+
             (not= trx+
                   [nil]))
      (let [end (or restore
                    (env :convex.run/end))]
        (-> env
            (cond->
              (not restore)
              (assoc-in path-restore
                        end))
            (assoc :convex.run/end
                   (fn end-2 [env-2]
                     (end ($.run.exec/eval-trx+ (dissoc env-2
                                                        :convex.run/error)
                                                trx+))))))
      (cond->
        env
        restore
        (-> (assoc :convex.run/end
                   restore)
            (update :convex.run/restore
                    dissoc
                    :convex.run/end))))))



(defn hook-error

  ""

  ;; TODO. Ensure failing hook is handled properly.

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run/on-error]
        restore      (get-in env
                             path-restore)
        hook         (second trx)]
    (if hook
      (let [env-2 ($.run.exec/eval-trx env
                                       hook)]
        (if (env-2 :convex.run/error)
          env-2
          (-> env-2
              (cond->
                (not restore)
                (assoc-in path-restore
                          (env-2 :convex.run/on-error)))
              (assoc :convex.run/on-error
                     (let [hook-2 (-> env-2
                                      :convex.sync/ctx
                                      $.cvm/result)]
                       (fn on-error [env-3]
                         (let [cause (env-3 :convex.run/error)
                               form  ($.code/list [hook-2
                                                   ($.code/quote (env-3 :convex.run/error))])
                               env-4 (-> env-3
                                         (dissoc :convex.run/error)
                                         (assoc :convex.run/on-error
                                                identity)
                                         ($.run.exec/eval-form form))
                               err   (env-4 :convex.run/error)]
                           (-> (if err
                                 ($.run.err/fatal env-4
                                                  form
                                                  ($.code/string "Calling error hook failed")
                                                  cause)
                                 (let [form-2 (-> env-4
                                                  :convex.sync/ctx
                                                  $.cvm/result)
                                       env-5  ($.run.exec/eval-trx env-4
                                                                   form-2)]
                                   (if (env-5 :convex.run/error)
                                     ($.run.err/fatal env-5
                                                      form-2
                                                      ($.code/string "Evaluating output from error hook failed")
                                                      cause)
                                     (assoc env-5
                                            :convex.run/error
                                            cause))))
                               (assoc :convex.run/on-error
                                      on-error)))))))))
      (cond->
        env
        restore
        (-> (assoc :convex.run/on-error
                   restore)
            (update :convex.run/restore
                    dissoc
                    :convex.run/on-error))))))



(defn hook-out

  ""

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run/out]
        restore      (get-in env
                             path-restore)
        hook         (second trx)]
    (if hook
      (let [env-2 ($.run.exec/eval-trx env
                                       hook)]
        (if (env-2 :convex.run/error)
          env-2
          (let [out (or restore
                        (env-2 :convex.run/out))]
            (-> env-2
                (cond->
                  (not restore)
                  (assoc-in path-restore
                            out))
                (assoc :convex.run/out
                       (fn out-2 [env-3 x]
                         (let [on-error (env-3 :convex.run/on-error)
                               form     ($.code/list [hook
                                                      ($.code/quote x)])
                               env-4    (-> env-3
                                            (assoc :convex.run/on-error
                                                   identity)
                                            ($.run.exec/eval-form form)
                                            (assoc :convex.run/on-error
                                                   on-error))
                               err      (env-4 :convex.run/error)]
                           (if (identical? err
                                           (env-3 :convex.run/error))
                             (out env-4
                                  (-> env-4
                                      :convex.sync/ctx
                                      $.cvm/result))
                             (-> env-4
                                 (assoc :convex.run/out
                                        out)
                                 ($.run.err/fatal form
                                                  ($.code/string "Calling output hook failed, using default output")
                                                  err)
                                 (assoc :convex.run/out
                                        out-2))))))))))
      (cond->
        env
        restore
        (-> (assoc :convex.run/out
                   restore)
            (update :convex.run/restore
                    dissoc
                    :convex.run/out))))))



(defn hook-trx

  ""

  [env trx]

  (if-some [hook (second trx)]
    (assoc env
           :convex.run.hook/trx
           hook)
    (dissoc env
            :convex.run.hook/trx)))



(defn log

  ""

  ;; TODO. Error handling.

  [env trx]

  (if-some [cvm-sym (second trx)]
    ($.run.exec/eval-form env
                          ($.code/def cvm-sym
                                      ($.cvm/log (env :convex.sync/ctx))))
    ($.run.err/signal env
                      ($.run.err/strx ErrorCodes/ARGUMENT
                                      trx
                                      ($.code/string "Argument for 'cvm.log' must be symbol for defining the log")))))



(defn out

  ""

  [env trx]

  (if-some [form-2 (second trx)]
    (let [env-2 ($.run.exec/eval-trx env
                                     form-2)]
      (if (env-2 :convex.run/error)
        env-2
        ((env-2 :convex.run/out)
         env-2
         (-> env-2
             :convex.sync/ctx
             $.cvm/result))))
    env))



(defn out-clear

  ""

  ;; https://www.delftstack.com/howto/java/java-clear-console/

  [env _trx]

  (print "\033[H\033[2J")
  (flush)
  env)



(defn read'

  ""

  [env trx]

  (if-some [sym (second trx)]
    (if ($.code/symbol? sym)
      (if-some [src (when (= (count trx)
                             3)
                      (nth (seq trx)
                           2))]
        (if ($.code/string? src)
          (try
            ($.run.exec/eval-form env
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



(defn splice

  "Like [[do']] but dynamic, evaluates its argument to a vector of transactions."

  [env trx]

  (let [env-2 ($.run.exec/eval-trx env
                                   (second trx))]
    (if (env-2 :convex.run/error)
      env-2
      (let [result (-> env-2
                       :convex.sync/ctx
                       $.cvm/result)]
        (if ($.code/vector? result)
          ($.run.exec/eval-trx+ env-2
                                result)
          ($.run.err/signal env-2
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "In 'cvm.splice', argument must evaluate to a vector of transactions"))))))))



(defn try'

  ""

  [env trx]

  (let [trx-last (last trx)
        catch?   ($.code/call? trx-last
                               $.run.sym/catch)
        on-error (env :convex.run/on-error)]
    (-> env
        (assoc :convex.run/on-error
               (fn [env-2]
                 (-> env-2
                     (dissoc :convex.run/error)
                     (cond->
                       catch?
                       (-> (assoc :convex.run/on-error
                                  on-error)
                           ($.run.exec/eval-form ($.code/def $.run.sym/error
                                                             (env-2 :convex.run/error)))
                           ($.run.exec/eval-trx+ (rest trx-last))
                           ($.run.exec/eval-form ($.code/undef $.run.sym/error))))
                     (assoc :convex.run/error
                            :try))))
        ($.run.exec/eval-trx+ (-> trx
                                  rest
                                  (cond->
                                    catch?
                                    butlast)))
        (assoc :convex.run/on-error
               on-error)
        (dissoc :convex.run/error))))


;;;;;;;;;;


(def registry

  ""

  {"cvm.dep"        dep
   "cvm.do"         do'
   "cvm.env"        env
   "cvm.hook.end"   hook-end
   "cvm.hook.error" hook-error
   "cvm.hook.out"   hook-out
   "cvm.hook.trx"   hook-trx
   "cvm.log"        log
   "cvm.out"        out
   "cvm.out.clear"  out-clear
   "cvm.read"       read'
   "cvm.splice"     splice
   "cvm.try"        try'})
