(ns convex.run.main

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data AList)
           (convex.core.lang Symbols)
           (java.io File)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.java.io]
            [convex.data      :as $.data]
            [convex.read      :as $.read]
            [convex.cvm       :as $.cvm]
            [convex.run.ctx   :as $.run.ctx]
            [convex.run.err   :as $.run.err]
            [convex.run.exec  :as $.run.exec]
            [convex.run.kw    :as $.run.kw]
            [convex.run.sym   :as $.run.sym]
            [convex.sync      :as $.sync]
            [convex.watch     :as $.watch]))


(declare sym->dep)


;;;;;;;;;; Computing dependencies


(defn sym->dep

  "Used when a main file is read. Looks at the first transaction under `:convex.run/trx+` and computes
   dependencies if it is a `(sreq/dep ...)` form."

  [env]

  (or (when-some [trx (first (env :convex.run/trx+))]
        (when ($.data/list? trx)
          (let [^AList form (first trx)]
            (when (and ($.data/list? form)
                       (= (count form)
                          3)
                       (= (.get form
                                0)
                          Symbols/LOOKUP)
                       (let [addr (.get form
                                        1)]
                         (or (= addr
                                $.run.sym/sreq)
                             (= addr
                                $.run.ctx/addr-$)))
                       (= (.get form
                                2)
                          $.run.sym/dep))
              (let [sym->dep (second trx)]
                (if ($.data/map? sym->dep)
                  (reduce (fn [env-2 [cvm-sym cvm-str-dep]]
                            (if-some [err-message (cond
                                                    (not ($.data/string? cvm-str-dep)) (str "Dependency must be a path to a file (string), not: "
                                                                                            cvm-str-dep)
                                                    (not ($.data/symbol? cvm-sym))     (format "Dependency '%s' must be defined under a symbol, not: %s"
                                                                                               cvm-str-dep
                                                                                               cvm-sym))]
                              (reduced ($.run.err/fail env
                                                       (-> ($.data/error ($.data/code-std* :CAST)
                                                                         ($.data/string err-message))
                                                           ($.run.err/assoc-phase $.run.kw/dep))))
                              (assoc-in env-2
                                        [:convex.run/sym->dep
                                         (str cvm-sym)]
                                        (-> cvm-str-dep
                                            str
                                            File.
                                            .getCanonicalPath))))
                          (update env
                                  :convex.run/trx+
                                  rest)
                          sym->dep)
                  ($.run.err/fail env
                                  (-> ($.data/error ($.data/code-std* :CAST)
                                                    ($.data/string "Dependencies must but a map of 'symbol' -> 'file path (string)'"))
                                      ($.run.err/assoc-phase $.run.kw/dep)))))))))
      env))
          

;;;;;;;;;; Initialization


(defn init

  "Initializes important functions and values in the given `env`, using defaults when relevant.

   Must be used prior to other preparatory functions such as [[main-file]].

   Operates over:

   | Key | Action | Mandatory |
   |---|---|---|
   | `:convex.run/path` | If present, ensures the path to the main file is canonical |
   | `:convex.run.hook/end` | Ensures a default end hook, see namespace description |
   | `:convex.run.hook/error` | Ensures a default error hook, see namespace description |
   | `:convex.run.hook/out` | Ensures a default output hook, see namespace description |
   | `:convex.run/single-run? | Whether code is run once or more (watch mode), defaults to false |
   | `:convex.sync/cx-base | Ensures a default base context, see [[convex.run.ctx/base]] |"


  [env ^String path]

  (-> env
      (assoc :convex.sync/ctx-base ($.cvm/fork (env :convex.sync/ctx))
             :convex.run/path      (.getCanonicalPath (File. path)))
      (dissoc :convex.sync/ctx)
      $.run.ctx/main))


;;;;;;;;;; Error handling aspects


(defn check-err-sync

  "Signals a sync error if needed (see [[convex.run.err/sync]]).
  
   Returns nil otherwise, meaning no sync error has been detected and handled."

  [env]

  (when-some [err (env :convex.sync/error)]
    ($.run.err/fail env
                    ($.run.err/sync err))))


;;;;;;;;;; Accessing a main file


(defn main-file

  "Reads and parses the main file under `:convex.run/path` and prepares transactions using [[assoc-trx+]]."

  [env]

  (let [[err
         trx+] (try

                 [nil
                  ($.read/file+ (env :convex.run/path))]

                 (catch NoSuchFileException _ex
                   [($.run.err/main-src-access)
                    nil])

                 (catch Throwable _ex
                   [($.run.err/main-src ($.data/string "Main file cannot be read and parsed as Convex Lisp"))
                    nil]))]
    (if err
      ($.run.err/fail env
                      err)
      (-> env
          (assoc :convex.run/trx+
                 trx+)
          sym->dep))))


;;;;;;;;;; Loading a main file


(defn load

  "Reads and executes the main file under `path` by using [[once]]."


  ([path]

   (load nil
         path))


  ([env path]

   (let [env-2 (-> env
                   (init path)
                   main-file)]
     (if (env-2 :convex.run/error)
       env-2
       (if-some [sym->dep' (env-2 :convex.run/sym->dep)]
         (let [env-3    (merge env-2
                               ($.sync/disk ($.cvm/fork (env-2 :convex.sync/ctx-base))
                                            sym->dep'))]
           (or (check-err-sync env-3)
               ($.run.exec/load env-3)))
         (-> env-2
             (assoc :convex.sync/ctx
                    ($.cvm/fork (env-2 :convex.sync/ctx-base)))
             $.run.exec/load))))))


;;;;;;;;;; Watching a main file


(let [-restart (fn [a*env env]
                 ;;
                 ;; Restart watcher whenever a change in dependency is detected.
                 ;;
                 ($.watch/-stop env)
                 ($.watch/-start a*env
                                 (-> env
                                     (dissoc :convex.run/error
                                             :convex.run/juice-last
                                             :convex.run/state-stack
                                             :convex.sync/ctx
                                             :convex.sync/input+
                                             :convex.sync/input->code
                                             :convex.sync/input->cvm-sym
                                             :convex.watch/error
                                             :convex.watch/watcher))))]

  (defn watch

    "Executes a main file just like [[load]] but provides a live-reloading experience.

     Whenever the main file or any of its dependencies changes, relevant assets are reloaded and
     the whole is rexecuted anew.
   
     Built on top of the 'watch' project (see the [[convex.watch]] namespace)."

    ;; Implementation of the `:convex.watch/on-change` is a tad convoluted because it handles various kind of errors
    ;; and dependency changes.
    ;;
    ;; Essentially, it first checks for errors at the level of the watcher, then errors at the level of the syncing,
    ;; then checks if dependencies changed, and ultimately run transactions unless it waits for a correction in dependencies
    ;; given that an error occured at that level (eg. requesting an nonexistent file).

    ([path]

     (watch nil
            path))


    ([env ^String path]

     (let [a*env (-> env
                     (assoc :convex.run/watch?
                            true)
                     (init path)
                     (assoc :convex.watch/extra+
                            #{(.getCanonicalPath (File. path))})
                     $.watch/init)]
       (send a*env
             (fn [env]
               (assoc env
                      :convex.watch/on-change
                      (fn on-change [{:as      env-2
                                      dep-old+ :convex.run/dep+}]
                        (some-> (env-2 :convex.run.watch/cycle)
                                future-cancel)
                        (let [env-3 (dissoc env-2
                                            :convex.run/error
                                            :convex.run.watch/cycle)]
                          (or ;;
                              ;; Handles watcher error if any.
                              ;;
                              (when-some [[etype
                                           arg]  (env-3 :convex.watch/error)]
                                (case etype
                                  :not-found (if (= arg
                                                    (first (env-3 :convex.watch/extra+)))
                                               ($.run.err/fail env-3
                                                               ($.run.err/main-src-access))
                                               ;;
                                               ;; Dependency is missing, restart watching only main file for retrying on new changes.
                                               ;; A "dep-lock" is used so that a new watcher with the same failing dependencies is not restarted right away.
                                               ;;
                                               (-restart a*env
                                                         (-> env-3
                                                             (assoc :convex.run/dep-lock
                                                                    dep-old+)
                                                             (dissoc :convex.run/dep+
                                                                     :convex.watch/sym->dep)
                                                             ($.run.err/fail ($.run.err/sync :load
                                                                                             {arg [:not-found]})))))
                                  :unknown   ($.run.err/report env-3
                                                               ($.run.err/watcher-setup)
                                                               arg)))
                              ;;
                              ;; Handles sync error if any.
                              ;;
                              (check-err-sync env-3)
                              ;;
                              ;; No significant errors were detected so try evaluation.
                              ;;
                              (let [dep-lock (env-3 :convex.run/dep-lock)]
                                (if (or dep-lock
                                        (nil? dep-old+)
                                        (seq (env-3 :convex.watch/extra->change)))
                                  (let [env-4 (main-file env-3)]
                                    (if (env-4 :convex.run/error)
                                      env-4
                                      (let [sym->dep' (env-4 :convex.run/sym->dep)
                                            dep-new+  (set (vals sym->dep'))
                                            env-5     (-> env-4
                                                          (assoc :convex.run/dep+       dep-new+
                                                                 :convex.watch/sym->dep sym->dep')
                                                          (dissoc :convex.run/sym->dep))]
                                        (if (env-5 :convex.run/error)
                                          env-5
                                          (if (= (not-empty sym->dep')
                                                 (not-empty (env-3 :convex.watch/sym->dep)))
                                              ;;
                                              ;; TODO. Ideally, nothing is reloaded if symbol changes put not file path and code reload only happens on dependencies that
                                              ;;       did change. Currently, alls deps are reloaded if anything in `sym->dep` changes.
                                              ;;
                                              ; (= (not-empty dep-new+)
                                              ;    (not-empty dep-old+))
                                            (-> env-5
                                                (dissoc :convex.watch/dep-lock
                                                        :convex.watch/extra->change)
                                                $.sync/patch
                                                $.sync/eval
                                                $.run.exec/watch)
                                            (if (= dep-new+
                                                   dep-lock)
                                              (dissoc env-5
                                                      :convex.run/dep+
                                                      :convex.run/dep-lock
                                                      :convex.run/extra->change
                                                      :convex.watch/sym->dep)
                                              (-restart a*env
                                                        (dissoc env-5
                                                                :convex.run/dep-lock))))))))
                                  ($.run.exec/watch env-3)))))))))
       ($.watch/start a*env)
       a*env))))
