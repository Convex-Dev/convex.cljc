(ns convex.disk

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            load
                            read])
  (:import (java.io File))
  (:require [convex.cvm  :as $.cvm]
            [convex.sync :as $.sync]
            [hawk.core   :as watcher]))


(declare read)


;;;;;;;;;; Helpers


(defn- -canonical-path+

  ;;

  [step+]

  (map (fn [step]
         (update step
                 0
                 (fn [^String path]
                   (.getCanonicalPath (File. path)))))
       step+))



(defn- -env

  ;;

  [step+ option+]

  (-> step+
      -canonical-path+
      ($.sync/env (assoc option+
                         :read
                         read))
      $.sync/load))


;;;;;;;;;; Miscellaneous


(defn read

  ""

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;; Loading files once


(defn load

  ""


  ([step+]

   (load step+
         nil))


  ([step+ option+]

   (let [env (-env step+
                   option+)]
     (if (env :error)
       env
       (-> env
           $.sync/sync
           $.sync/exec)))))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(let [err-  (fn [env]
              (when (env :error)
                (let [env-2 (dissoc env
                                    :ctx)
                      f     (:on-error env)]
                  (when f
                    (f env-2))
                  env-2)))
      exec- (fn [env]
              (or (err- env)
                  (let [env-2 ($.sync/exec env)]
                    (or (err- env-2)
                        env-2))))]

  (defn watch

    "Starts a watcher which syncs Convex Lisp files to a context.

     When a file is modified, its source is processed and all sources are evaluated in the given order, step by step.

     `step+` is a collection of steps, 2-tuples composed of:

     - Path to a Convex Lisp file
     - Map with:

     | Key | Optional? | Value | Default |
     |---|---|---|---|
     | `:code` | True | Function **code from target file** -> **code** (as a Convex object) | `identity` |
     | `:eval` | True | Evaluating function which runs **code** for the target file (as a Convex object) | `convex.clj.eval` |

     `option+` is a map of options:

     | Key | Value | Default
     |---|---|---|
     | `:after-run` | Function **ctx** -> **ctx** run after all steps | `identity` |
     | `:init` | No-arg function which create the initial context prior to running through steps | `convex.cvm/ctx` |

     Reifies `java.lang.AutoCloseable`, hence can be stopped with `.close`."


    ([step+]

     (watch step+
            nil))


    ([step+ option+]

     (let [*env     (atom nil)
           env-2    (-> (-env step+
                              option+)
                        $.sync/sync)
           watcher  (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                              kind]}]
                                                 (let [path (.getCanonicalPath file)]
                                                   (swap! *env
                                                          (fn [env]
                                                            (exec- ((if (identical? kind
                                                                                    :delete)
                                                                      $.sync/unload
                                                                      $.sync/reload)
                                                                    env
                                                                    path))))))
                                      :paths   (keys (env-2 :src->i-step+))}])
           ret      (reify

                      clojure.lang.IDeref
                
                        (deref [_]
                          (@*env :ctx))
                
                      java.lang.AutoCloseable
                
                        (close [_]
                          (watcher/stop! watcher)))]
       (reset! *env
               (-> env-2
                   (assoc :watcher
                          ret)
                   exec-))
       ret))))
