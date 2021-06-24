(ns convex.run

  ""

  ;; TOOD. Reader errors cannot be very meaningful as long as Parboiled is used.

  {:author "Adam Helinski"}

  (:import (convex.core ErrorCodes)
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
            [convex.run.strx]
            [convex.run.sym   :as $.run.sym]
            [convex.sync      :as $.sync]
            [convex.watch     :as $.watch]))


;;;;;;;;;; Miscellaneous


(defn sym->dep

  ""

  ;; TODO. Error if invalid format.

  [trx+]

  (when-some [trx-first (first trx+)]
    (when (= ($.run.exec/strx-dispatch trx-first)
             $.run.sym/dep)
      (not-empty (reduce (fn [sym->dep x]
                           (assoc sym->dep
                                  (str (first x))
                                  (.getCanonicalPath (File. (str (second x))))))
                         {}
                         (second trx-first))))))


;;;;;;;;;; 


(defn init

  ""

  [env]

  (-> env
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
      (update :convex.run.hook/trx
              #(or %
                   $.run.exec/compile-run))
      (update :convex.sync/ctx-base
              #(or %
                   $.run.ctx/base))))



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
                            ($.sync/disk ($.cvm/fork (env :convex.sync/ctx-base))
                                         sym->dep'))
            err-sync (env-2 :convex.sync/error)]
        (if err-sync
          ;; TODO. Better error.
          ($.run.err/signal env-2
                            ErrorCodes/STATE
                            nil)
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
                                  (dissoc env
                                          :convex.run/error
                                          :convex.run/i-trx
                                          :convex.run/juice-last
                                          :convex.sync/ctx
                                          :convex.sync/input+
                                          :convex.sync/input->code
                                          :convex.sync/input->cvm-sym
                                          :convex.watch/watcher)))]

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
                                            $.run.exec/cycle)
                                        (if (= dep-new+
                                               dep-lock)
                                          (dissoc env-5
                                                  :convex.run/dep+
                                                  :convex.run/dep-lock
                                                  :convex.run/extra->change)
                                          (-restart a*env
                                                    (dissoc env-5
                                                            :convex.run/dep-lock))))))
                                  ($.run.exec/cycle env-3)))))))))
       ($.watch/start a*env)
       a*env))))


;;;;;;;;;;


(comment


  (eval "(strx/out (help/doc strx 'out))")



  (load "src/convex/dev/app/run.cvx")



  (def a*env
       (watch "src/convex/dev/app/run.cvx"))

  (clojure.pprint/pprint (dissoc @a*env
                                 :convex.sync/input->code))


  
  ($.watch/stop a*env)


  (agent-error a*env)


  )
