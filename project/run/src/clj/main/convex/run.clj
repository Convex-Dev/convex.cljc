(ns convex.run

  ""

  ;; TOOD. Reader errors cannot be very meaningful as long as Parboiled is used.

  {:author "Adam Helinski"}

  (:import (convex.core.data AList
                             AMap)
           (convex.core.lang Symbols)
           (java.io File))
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.java.io]
            [convex.code      :as $.code]
            [convex.cvm       :as $.cvm]
            [convex.run.ctx   :as $.run.ctx]
            [convex.run.err   :as $.run.err]
            [convex.run.exec  :as $.run.exec]
            [convex.run.kw    :as $.run.kw]
            [convex.run.sym   :as $.run.sym]
            [convex.run.sreq]
            [convex.sync      :as $.sync]
            [convex.watch     :as $.watch]))


;;;;;;;;;; Miscellaneous


(defn sym->dep

  ""

  ;; TODO. Error if invalid format.

  [env]

  (or (when-some [trx (first (env :convex.run/trx+))]
        (when ($.code/list? trx)
          (let [^AList form (first trx)]
            (when (and ($.code/list? form)
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
                                $.run.ctx/addr-sreq)))
                       (= (.get form
                                2)
                          $.run.sym/dep))
              (let [sym->dep (second trx)]
                (if ($.code/map? sym->dep)
                  (reduce (fn [env-2 [cvm-sym cvm-str-dep]]
                            (if-some [err-message (cond
                                                    (not ($.code/string? cvm-str-dep)) (str "Dependency must be a path to a file (string), not: "
                                                                                            cvm-str-dep)
                                                    (not ($.code/symbol? cvm-sym))     (format "Dependency '%s' must be defined under a symbol, not: %s"
                                                                                               cvm-str-dep
                                                                                               cvm-sym))]
                              (reduced ($.run.err/signal env
                                                         (-> ($.code/error ($.cvm/code-std* :CAST)
                                                                           ($.code/string err-message))
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
                  ($.run.err/signal env
                                    (-> ($.code/error ($.cvm/code-std* :CAST)
                                                      ($.code/string "Dependencies must but a map of 'symbol' -> 'file path (string)'"))
                                        ($.run.err/assoc-phase $.run.kw/dep)))))))))
      env))
          

;;;;;;;;;; 


(defn init

  ""

  [env]

  (-> env
      (update :convex.run/path
              (fn [^String path]
                (when path
                  (.getCanonicalPath (File. path)))))
      (update :convex.run.hook/end
              #(or %
                   identity))
      (update :convex.run.hook/error
              #(or %
                   (fn [env-2]
                     ((env-2 :convex.run.hook/out)
                      env-2
                      (env-2 :convex.run/error)))))
      (update :convex.run.hook/out
              #(or %
                   (fn [env-2 x]
                     (when x
                       (-> x
                           str
                           tap>))
                     env-2)))
      (update :convex.sync/ctx-base
              #(or %
                   $.run.ctx/base))
      $.run.ctx/init))



(defn err-main

  ""

  [env message]

  ($.run.err/signal env
                    (-> ($.code/error ($.cvm/code-std* :FATAL)
                                      ($.code/string message))
                        ($.run.err/assoc-phase $.run.kw/main))))



(defn err-main-access

  ""

  [env]

  (err-main env
            "Main file not found or not accessible"))



(defn slurp-file

  ""

  [env]

  (let [path  (env :convex.run/path)
        [ex
         src] (try
                [nil
                 (slurp path)]
                (catch Throwable ex
                  [ex
                   nil]))]
    (if ex
      (err-main-access env)
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
      (err-main env
                "Main file cannot be parsed as Convex Lisp")
      (-> env
          (assoc :convex.run/trx+ trx+)
          (dissoc :convex.run/src)
          sym->dep))))



(defn main-file

  ""

  [env]
    
  (let [env-2 (slurp-file env)]
    (if (env-2 :convex.run/error)
      env-2
      (process-src env-2))))



(defn err-sync

  ""

  [env [kind path->reason]]

  ($.run.err/signal env
                    (-> ($.code/error ($.cvm/code-std* :FATAL)
                                      (reduce-kv (fn [^AMap path->reason--2 path reason]
                                                   (.assoc path->reason--2
                                                           ($.code/string path)
                                                           ($.code/string (case kind

                                                                            :load
                                                                            (case (first reason)
                                                                              :not-found "Dependency file not found or inaccessible"
                                                                              :parse     "Dependency file cannot be parsed as Convex Lisp"
                                                                              :unknown   "Unknown error while loading and parsing dependency file")

                                                                            "Unknown error while loading dependency file"))))
                                                 ($.code/map)
                                                 path->reason))
                        ($.run.err/assoc-phase $.run.kw/dep))))



(defn check-err-sync

  ""

  [env]

  (when-some [err (env :convex.sync/error)]
    (err-sync env
              err)))



(defn once

  ""

  [env]

  (if (env :convex.run/error)
    env
    (if-some [sym->dep' (env :convex.run/sym->dep)]
      (let [env-2    (merge env
                            ($.sync/disk ($.cvm/fork (env :convex.sync/ctx-base))
                                         sym->dep'))]
        (or (check-err-sync env-2)
            ($.run.exec/cycle env-2)))
      (-> env
          (assoc :convex.sync/ctx
                 ($.cvm/fork (env :convex.sync/ctx-base)))
          $.run.exec/cycle))))


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
       (assoc :convex.run/path
              path)
       init
       main-file
       once)))


;;;;;;;;;; Watch files


(let [-restart  (fn [a*env env]
                  ;;
                  ;; Restart watcher whenever a change in dependency is detected.
                  ;;
                  ($.watch/-stop env)
                  ($.watch/-start a*env
                                  (dissoc env
                                          :convex.run/error
                                          :convex.run/i-trx
                                          :convex.run/juice-last
                                          :convex.sync/ctx
                                          :convex.sync/input+
                                          :convex.sync/input->code
                                          :convex.sync/input->cvm-sym
                                          :convex.watch/error
                                          :convex.watch/watcher)))]

  (defn watch

    ""


    ([path]

     (watch nil
            path))


    ([env ^String path]

     (let [a*env ($.watch/init (-> env
                                   (assoc :convex.run/path
                                          path)
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
                                  :not-found (if (= arg
                                                    (first (env-3 :convex.watch/extra+)))
                                               (err-main-access env-3)
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
                                                             (err-sync [:load {arg [:not-found]}]))))
                                  :unknown   ($.run.err/signal env
                                                               (-> ($.code/error ($.cvm/code-std* :FATAL)
                                                                                 ($.code/string "Unknown error occured while setting up the file watcher"))
                                                                   ($.run.err/assoc-phase $.run.kw/watch)))))
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
                                          (if (= (not-empty dep-new+)
                                                 (not-empty dep-old+))
                                            (-> env-5
                                                (dissoc :convex.watch/dep-lock
                                                        :convex.watch/extra->change)
                                                $.sync/patch
                                                $.sync/eval
                                                $.run.exec/cycle)
                                            (if (= dep-new+
                                                   dep-lock)
                                              (dissoc env-5
                                                      :convex.run/dep+
                                                      :convex.run/dep-lock
                                                      :convex.run/extra->change)
                                              (-restart a*env
                                                        (dissoc env-5
                                                                :convex.run/dep-lock))))))))
                                  ($.run.exec/cycle env-3)))))))))
       ($.watch/start a*env)
       a*env))))


;;;;;;;;;;


(comment


  (eval "(help/about sreq)")



  (load "project/run/src/cvx/dev/convex/app/run/dev.cvx")



  (def a*env
       (watch "project/run/src/cvx/dev/convex/app/run/dev.cvx"))

  ($.watch/stop a*env)

  (clojure.pprint/pprint (dissoc @a*env
                                 :convex.sync/input->code))

  (agent-error a*env)


  )
