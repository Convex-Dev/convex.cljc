(ns convex.disk

  "Loading Convex Lisp files in context, watching and live-reloading.
  
   See [[convex.example.disk]] namespace for exmaples."

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

  ;; Maps `step+` so that each file path ends up in canonical representation.

  [step+]

  (map (fn [step]
         (update step
                 0
                 (fn [^String path]
                   (.getCanonicalPath (File. path)))))
       step+))



(defn- -env

  ;; Layer on top of [[$.sync/env]].

  [step+ option+]

  (-> step+
      -canonical-path+
      ($.sync/env (assoc option+
                         :read
                         read))
      $.sync/load))


;;;;;;;;;; Miscellaneous


(defn read

  "Reads the file located at `path` and return Convex code."

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;; Loading files once


(defn load

  "Loads the given `step+`, in order, where each step is a Convex Lisp file, and execute them one by one.

   A step is a 2-tuple containing:

   - Path to Convex Lisp file
   - Optional map with:
  
   | Key | Optional? | Value | Default |
   |---|---|---|---|
   | `:eval` | True | Evaluating function which runs **code** for the target file (as a Convex object) | See `option+` |
   | `:map` | True | Function **code from target file** -> **code** (as a Convex object) | `identity` |

   `option` is a map of options such as:
   
    | Key | Value | Default
    |---|---|---|
    | `:after-run` | Function **ctx** -> **ctx** run after all steps | `identity` |
    | `:eval` | Evaluating function used when step does not provide one | [[convex.cvm/eval]] |
    | `:init-ctx` | No-arg function which create the initial context prior to running through steps | `convex.cvm/ctx` |
  

   Returns a map which holds the resulting context under `:ctx`, unless an `:error` is present. This `:error` points to
   a key in the returned valued that holds diagnostic information. Currently:

   | Key | Value |
   |---|---|
   | `:error-eval` | Exception that occured when evaluating a step |
   | `:path->error` | Map of `file path` -> `exception` |"


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

    "Like [[load]] but watches the files and provides live-reloading.

     Returns an object which can be deferenced to fork of a context that is always up-to-date.

     Reifies `java.lang.AutoCloseable`, hence can be stopped with `.close`.

     In addition, `option+` can also contain:

     | Key | Value | Default
     |---|---|---|
     | `:on-error` | Called in case of failure with the same value as returned from [[load]] |

     Exceptions are catched only during reading and evaluation. Errors resulting elsewhere (eg. during a step's`:map`)
     must be handled by the user."


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
                                      :paths   (keys (env-2 :input->i-step+))}])
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
