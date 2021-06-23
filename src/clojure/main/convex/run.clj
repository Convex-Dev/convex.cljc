(ns convex.run

  ""

  ;; TOOD. Reader errors cannot be very meaningful as long as Parboiled is used.

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
           (convex.core.lang Reader)
           (java.io File))
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]
            [convex.code      :as $.code]
            [convex.cvm       :as $.cvm]
            [convex.run.err   :as $.run.err]
            [convex.run.kw    :as $.run.kw]
            [convex.run.sym   :as $.run.sym]
            [convex.sync      :as $.sync]
            [convex.watch     :as $.watch]))


(declare eval-form
         eval-trx
         eval-trx+)


;;;;;;;;;; Miscellaneous


(def d*ctx-base

  ""

  (delay
    ($.cvm/juice-refill ($.cvm/ctx))))



(defn sym->dep

  ""

  ;; TODO. Error if invalid format.

  [trx+]

  (let [trx-first (first trx+)]
    (when ($.code/list? trx-first)
      (let [item (first trx-first)]
        (when ($.code/symbol? item)
          (when (= (str item)
                   "cvm.dep")
            (not-empty (reduce (fn [sym->dep x]
                                 (assoc sym->dep
                                        (str (first x))
                                        (.getCanonicalPath (File. (str (second x))))))
                               {}
                               (second trx-first)))))))))


;;;;;;;;;; Special transactions


(defn cvm-dep

  ""

  [env trx]

  ($.run.err/signal env
                    ($.run.err/strx ErrorCodes/STATE
                                    trx
                                    ($.code/string "CVM special command 'cvm.dep' can only be used as the very first transaction"))))



(defn cvm-do

  ""

  [env trx]

  (eval-trx+ env
             (rest trx)))



(defn cvm-env


  ""

  [env trx]

  (let [sym (second trx)]
    (if ($.code/symbol? sym)
      (if-some [k (when (= (count trx)
                           3)
                    (nth (seq trx)
                         2))]
        (if ($.code/string? k)
          (eval-form env
                     ($.code/def sym
                                 ($.code/string (System/getenv (str k)))))
          ($.run.err/signal env
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "Second argument to 'cvm.env' must be a string"))))
        (eval-form env
                   ($.code/def sym
                               ($.code/map (map (fn [[k v]]
                                                  [($.code/string k)
                                                   ($.code/string v)])
                                                (System/getenv))))))
      ($.run.err/signal env
                        ($.run.err/strx ErrorCodes/CAST
                                        trx
                                        ($.code/string "First argument to 'cvm.env' must be a symbol"))))))



(defn cvm-hook-end

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
                     (end (eval-trx+ (dissoc env-2
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



(defn cvm-hook-error

  ""

  ;; TODO. Ensure failing hook is handled properly.

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run/on-error]
        restore      (get-in env
                             path-restore)
        hook         (second trx)]
    (if hook
      (let [env-2 (eval-trx env
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
                                         (eval-form form))
                               err   (env-4 :convex.run/error)]
                           (-> (if err
                                 ($.run.err/fatal env-4
                                                  form
                                                  ($.code/string "Calling error hook failed")
                                                  cause)
                                 (let [form-2 (-> env-4
                                                  :convex.sync/ctx
                                                  $.cvm/result)
                                       env-5  (eval-trx env-4
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



(defn cvm-hook-out

  ""

  [env trx]

  (let [path-restore [:convex.run/restore
                      :convex.run/out]
        restore      (get-in env
                             path-restore)
        hook         (second trx)]
    (if hook
      (let [env-2 (eval-trx env
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
                                            (eval-form form)
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



(defn cvm-hook-trx

  ""

  [env trx]

  (if-some [hook (second trx)]
    (assoc env
           :convex.run.hook/trx
           hook)
    (dissoc env
            :convex.run.hook/trx)))



(defn cvm-log

  ""

  ;; TODO. Error handling.

  [env trx]

  (if-some [cvm-sym (second trx)]
    (eval-form env
               ($.code/def cvm-sym
                           ($.cvm/log (env :convex.sync/ctx))))
    ($.run.err/signal env
                      ($.run.err/strx ErrorCodes/ARGUMENT
                                      trx
                                      ($.code/string "Argument for 'cvm.log' must be symbol for defining the log")))))



(defn cvm-out

  ""

  [env trx]

  (if-some [form-2 (second trx)]
    (let [env-2 (eval-trx env
                          form-2)]
      (if (env-2 :convex.run/error)
        env-2
        ((env-2 :convex.run/out)
         env-2
         (-> env-2
             :convex.sync/ctx
             $.cvm/result))))
    env))



(defn cvm-out-clear

  ""

  ;; https://www.delftstack.com/howto/java/java-clear-console/

  [env _trx]

  (print "\033[H\033[2J")
  (flush)
  env)



(defn cvm-read

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
            (eval-form env
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



(defn cvm-splice

  "Like [[cvm-do]] but dynamic, evaluates its argument to a vector of transactions."

  [env trx]

  (let [env-2 (eval-trx env
                        (second trx))]
    (if (env-2 :convex.run/error)
      env-2
      (let [result (-> env-2
                       :convex.sync/ctx
                       $.cvm/result)]
        (if ($.code/vector? result)
          (eval-trx+ env-2
                     result)
          ($.run.err/signal env-2
                            ($.run.err/strx ErrorCodes/CAST
                                            trx
                                            ($.code/string "In 'cvm.splice', argument must evaluate to a vector of transactions"))))))))



(defn cvm-try

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
                           (eval-form ($.code/def $.run.sym/error
                                                  (env-2 :convex.run/error)))
                           (eval-trx+ (rest trx-last))
                           (eval-form ($.code/undef $.run.sym/error))))
                     (assoc :convex.run/error
                            :try))))
        (eval-trx+ (-> trx
                       rest
                       (cond->
                         catch?
                         butlast)))
        (assoc :convex.run/on-error
               on-error)
        (dissoc :convex.run/error))))


;;;;;


(defn strx

  "Special transaction"

  [trx]

  (when ($.code/list? trx)
    (let [sym-string (str (first trx))]
      (when (clojure.string/starts-with? sym-string
                                         "cvm.")
        (case sym-string
          "cvm.dep"        cvm-dep
          "cvm.do"         cvm-do
          "cvm.env"        cvm-env
          "cvm.hook.end"   cvm-hook-end
          "cvm.hook.error" cvm-hook-error
          "cvm.hook.out"   cvm-hook-out
          "cvm.hook.trx"   cvm-hook-trx
          "cvm.log"        cvm-log
          "cvm.out"        cvm-out
          "cvm.out.clear"  cvm-out-clear
          "cvm.read"       cvm-read
          "cvm.splice"     cvm-splice
          "cvm.try"        cvm-try
          (fn [env _trx]
            ($.run.err/signal env
                              ($.run.err/strx ErrorCodes/ARGUMENT
                                              trx
                                              ($.code/string "Unsupported special transaction")))))))))


;;;;;;;;;; Preparing transactions


(defn expand

  ""

  [env form]

  (let [ctx ($.cvm/expand (env :convex.sync/ctx)
                               form)
        ex  ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/expand
                                         form)))))



(defn inject-value+

  ""

  [env]

  (let [form  ($.code/do [($.code/def $.run.sym/juice-last
                                      ($.code/long (env :convex.run/juice-last)))
                          ($.code/def $.run.sym/trx-id
                                      ($.code/long (env :convex.run/i-trx)))])
        ctx   ($.cvm/eval (env :convex.sync/ctx)  
                          form)
        ex    ($.cvm/exception ctx)]
    (cond->
      (assoc env
             :convex.sync/ctx
             ctx)
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/trx-prepare
                                         form)))))


;;;;;;;;;; Evaluation


(defn eval-form

  ""

  [env form]

  (let [ctx   ($.cvm/juice-refill (env :convex.sync/ctx))
        juice ($.cvm/juice ctx)
        ctx-2 ($.cvm/eval ctx
                          form)
        ex    ($.cvm/exception ctx-2)]
    (cond->
      (-> env
          (assoc :convex.run/juice-last (- juice
                                           ($.cvm/juice ctx-2))
                 :convex.sync/ctx       ctx-2)
          (update :convex.run/i-trx
                  inc))
      ex
      ($.run.err/signal ($.run.err/error ex
                                         $.run.kw/trx-eval
                                         form)))))



(defn eval-trx

  ""

  [env trx]

  (if-some [f-strx (strx trx)]
    (f-strx env
            trx)
    (let [env-2 (inject-value+ env)]
      (if (env-2 :convex.run/error)
        env-2
        (let [env-3 (expand env-2
                            trx)]
          (if (env-3 :convex.run/error)
            env-3
            (let [trx-2 (-> env-3
                            :convex.sync/ctx
                            $.cvm/result)]
              (if-some [f-strx-2 (strx trx-2)]
                (f-strx-2 env-3
                          trx-2)
                (if-some [hook (env-3 :convex.run.hook/trx)]
                  (let [env-4 (eval-form env-3
                                         ($.code/list [hook
                                                       ($.code/quote trx-2)]))]
                    (if (env-4 :convex.run/error)
                      env-4
                      (eval-form env-4
                                 (-> env-4
                                     :convex.sync/ctx
                                     $.cvm/result))))
                  (eval-form env-3
                             trx-2))))))))))



(defn eval-trx+

  ""

  
  ([env]

   (eval-trx+ env
              (env :convex.run/trx+)))


  ([env trx+]

   (reduce (fn [env-2 trx]
             (let [env-3 (eval-trx env-2
                                   trx)]
               (if (env-3 :convex.run/error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))



(defn exec-trx+

  ""

  [env]

  (-> env
      (dissoc :convex.run/restore
              :convex.run.hook/trx)
      (merge (env :convex.run/restore))
      (assoc :convex.run/i-trx      0
             :convex.run/juice-last 0)
      (update :convex.sync/ctx
              (fn [ctx]
                ($.cvm/eval ctx
                            ($.code/def $.run.sym/cycle
                                        ($.code/long (or (env :convex.watch/cycle)
                                                         0))))))
      eval-trx+
      (as->
        env-2
        ((env-2 :convex.run/end)
         env-2))))


;;;;;;;;;; 


(defn init

  ""

  [env]

  (-> env
      (update :convex.run.hook/trx
              #(or %
                   (fn [_env-2 trx]
                     trx)))
      (update :convex.run/end
              #(or %
                   identity))
      (update :convex.run/on-error
              #(or %
                   (fn [env-2]
                     ((env-2 :convex.run/out)
                      env-2
                      (env-2 :convex.run/error)))))
      (update :convex.run/out
              #(or %
                   (fn [env-2 x]
                     (when x
                       (-> x
                           str
                           tap>))
                     env-2)))))



(defn slurp-file

  ""

  [env path]

  (let [[err
         src] (try
                [nil
                 (slurp path)]
                (catch Throwable err
                  [err
                   nil]))]
    (if err
      ($.run.err/signal env
                        (-> ($.code/error ErrorCodes/ARGUMENT
                                          ($.code/string "Unable to open file"))
                            (.assoc $.run.kw/path
                                    ($.code/string path))
                            ($.run.err/assoc-phase $.run.kw/file-open)))
      (assoc env
             :convex.run/src
             src))))



(defn process-src

  ""

  [env]

  (let [src    (env :convex.run/src)
        [err
         trx+] (try
                 [nil
                  (vec ($.cvm/read src))]
                 (catch Throwable err
                   [err
                    nil]))]
    (if err
      ($.run.err/signal env
                        (-> ($.code/error ErrorCodes/ARGUMENT
                                          ($.code/string "Unable to parse source code"))
                            (.assoc $.run.kw/src
                                    ($.code/string src))
                            ($.run.err/assoc-phase $.run.kw/read)))
      (let [sym->dep' (sym->dep trx+)]
        (-> env
            (assoc :convex.run/sym->dep sym->dep'
                   :convex.run/trx+     (cond->
                                          trx+
                                          sym->dep'
                                          rest))
            (dissoc :convex.run/src))))))



(defn main-file

  ""

  [env path]

  (let [env-2 (slurp-file env
                          path)]
    (if (env-2 :convex.run/error)
      env-2
      (process-src env-2))))



(defn once

  ""

  [env]

  (if (env :convex.run/error)
    env
    (if-some [sym->dep' (env :convex.run/sym->dep)]
      (let [env-2    (merge env
                            ($.sync/disk ($.cvm/fork @d*ctx-base)
                                         sym->dep'))
            err-sync (env-2 :convex.sync/error)]
        (if err-sync
          ;; TODO. Better error.
          ($.run.err/signal env-2
                            ErrorCodes/STATE
                            nil)
          (exec-trx+ env-2)))
      (-> env
          (assoc :convex.sync/ctx
                 ($.cvm/fork @d*ctx-base))
          exec-trx+))))


;;;;;;;;;; Evaluating a given source string


(defn eval

  ""


  ([src]

   (eval nil
         src))


  ([env src]

   (-> env
       init
       (assoc :convex.run/src
              src)
       process-src
       once)))


;;;;;;;;;; Load files


(defn load

  ""


  ([path]

   (load nil
         path))


  ([env path]

   (-> env
       init
       (main-file path)
       once)))


;;;;;;;;;; Watch files


(let [-restart  (fn [a*env env]
                  ;;
                  ;; Restart watcher whenever a change in dependency is detected.
                  ;;
                  ($.watch/-stop env)
                  ($.watch/-start a*env
                                  (select-keys env
                                               [:convex.run/dep+
                                                :convex.run/dep-lock
                                                :convex.run/end
                                                :convex.run/on-error
                                                :convex.run/out
                                                :convex.run/trx+
                                                :convex.sync/ctx-base
                                                :convex.watch/cycle
                                                :convex.watch/extra+
                                                :convex.watch/ms-debounce
                                                :convex.watch/on-change
                                                :convex.watch/sym->dep])))]

  (defn watch

    ""


    ([path]

     (watch nil
            path))


    ([env ^String path]

     (let [a*env ($.watch/init (-> env
                                   init
                                   (assoc :convex.watch/extra+
                                          #{(.getCanonicalPath (File. path))})))]
       (send a*env
             (fn [env]
               (assoc env
                      :convex.watch/on-change
                      (fn on-change [{:as      env-2
                                      dep-old+ :convex.run/dep+}]
                        (let [env-3 (dissoc env-2
                                            :convex.run/error)]
                          (or ;;
                              ;; Handles watcher error if any.
                              ;;
                              (when-some [[etype
                                           arg]  (env-3 :convex.watch/error)]
                                (case etype
                                  :exception ($.run.err/fatal env-3
                                                              ($.code/error ErrorCodes/FATAL
                                                                            ($.code/string "Unknown fatal error occured when setting up the file watcher")))
                                  :not-found (if (= arg
                                                    (first (env-3 :convex.watch/extra+)))
                                               ($.run.err/fatal env-3
                                                                ($.code/error ErrorCodes/FATAL
                                                                              ($.code/string "Main file does not exist")))
                                               ;;
                                               ;; Dependency is missing, restart watching only main file for retrying on new changes.
                                               ;; A "dep-lock" is used so that a new watcher with the same failing dependencies is not restarted right away.
                                               ;;
                                               (-restart a*env
                                                         ($.run.err/fatal (-> (assoc env-3
                                                                                     :convex.run/dep-lock
                                                                                     dep-old+)
                                                                              (dissoc :convex.run/dep+
                                                                                      :convex.watch/sym->dep))
                                                                          (-> ($.code/error ErrorCodes/FATAL
                                                                                            ($.code/string "Missing file for requested dependency"))
                                                                              (.assoc $.run.kw/path
                                                                                      ($.code/string arg))))))))
                              ;;
                              ;; Handles sync error if any.
                              ;;
                              (when-some [_err (env-3 :convex.sync/error)]
                                ;; TODO. Better error.
                                ($.run.err/signal env-3
                                                  ErrorCodes/STATE
                                                  nil))
                              ;;
                              ;; No significant errors were detected so try evaluation.
                              ;;
                              (let [dep-lock (env-3 :convex.run/dep-lock)]
                                (if (or dep-lock
                                        (nil? dep-old+)
                                        (seq (env-3 :convex.watch/extra->change)))
                                  (let [env-4     (main-file env-3
                                                             path)
                                        sym->dep' (env-4 :convex.run/sym->dep)
                                        dep-new+  (set (vals sym->dep'))
                                        env-5     (-> env-4
                                                      (assoc :convex.run/dep+       dep-new+
                                                             :convex.watch/sym->dep sym->dep')
                                                      (dissoc :convex.run/sym->dep))]
                                    (if (env-5 :convex.error/error)
                                      env-5
                                      (if (= (not-empty dep-new+)
                                             dep-old+)
                                        (-> env-5
                                            (dissoc :convex.watch/dep-lock
                                                    :convex.watch/extra->change)
                                            $.sync/patch
                                            $.sync/eval
                                            exec-trx+)
                                        (if (= dep-new+
                                               dep-lock)
                                          (dissoc env-5
                                                  :convex.run/dep+
                                                  :convex.run/dep-lock
                                                  :convex.run/extra->change)
                                          (-restart a*env
                                                    (dissoc env-5
                                                            :convex.run/dep-lock))))))
                                  (exec-trx+ env-3)))))))))
       ($.watch/start a*env)
       a*env))))


;;;;;;;;;;


(comment


  (eval "(cvm.out (+ 2 2))")



  (load "src/convex/dev/app/run.cvx")



  (def a*env
       (watch "src/convex/dev/app/run.cvx"))

  (clojure.pprint/pprint (dissoc @a*env
                                 :convex.sync/input->code))

  ($.watch/stop a*env)


  (agent-error a*env)


  )
