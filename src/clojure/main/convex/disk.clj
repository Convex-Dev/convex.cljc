(ns convex.disk

  "Loading Convex Lisp files in contextes + live-reloading.
  
   See [[convex.example.disk]] namespace for examples."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            load
                            read])
  (:import (java.io File))
  (:require [convex.code :as $.code]
            [convex.cvm  :as $.cvm]
            [convex.sync :as $.sync]
            [hawk.core   :as watcher]))


(declare read)


;;;;;;;;;; Helpers


(defn- -env

  ;; Layer on top of [[$.sync/env]].

  [sym->path option+]

  (let [input+        (reduce (fn [input+ [sym ^String path]]
                                (conj input+
                                      [(.getCanonicalPath (File. path))
                                       ($.code/symbol (str sym))]))
                              []
                              sym->path)
        path->cvm-sym (into {}
                            input+)]
    (-> ($.sync/env (fn [env path]
                      ($.sync/update-code env
                                          path
                                          ($.code/def (path->cvm-sym path)
                                                      ($.code/quote (try
                                                                      (read path)
                                                                      (catch Throwable err
                                                                        (-> env
                                                                            (assoc :error
                                                                                   :input->error)
                                                                            (assoc-in [:input->error
                                                                                       path]
                                                                                      err))))))))
                    (mapv first
                          input+)
                    option+)
        $.sync/load)))


;;;;;;;;;; Miscellaneous


(defn read

  "Reads the file located at `path` and returns Convex code."

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;; Loading files once


(defn load

  "Loads the given `step+`, in order, where each step is a Convex Lisp file, and executes them one by one.

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
    | `:eval` | Evaluating function used when a step does not provide one | [[convex.cvm/eval]] |
    | `:init-ctx` | No-arg function that creates the initial context prior to running through steps | `convex.cvm/ctx` |
  

   Returns a map which holds the resulting context under `:ctx`, unless an `:error` is present. This `:error` points to
   a key in the returned valued that holds diagnostic information. Currently, could be one of:

   | Key | Value |
   |---|---|
   | `:error-eval` | Exception that occured when evaluating a step |
   | `:path->error` | Map of `file path` -> `exception` |"


  ([path->sym]

   (load path->sym
         nil))


  ([path->sym option+]

   (let [env (-env path->sym
                   option+)]
     (if (env :error)
       env
       (-> env
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
     | `:on-error` | Called in case of failure with the same value as returned from [[load]] in the same situation |

     Exceptions are catched only during reading and evaluation. Errors resulting elsewhere (eg. during a step's`:map`)
     must be handled by the user."


    ([sym->input]

     (watch sym->input
            nil))


    ([sym->input option+]

     (let [*env     (atom nil)
           env      (-env sym->input
                          option+)
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
                                      :paths   (env :input+)}])
           ret      (reify

                      clojure.lang.IDeref
                
                        (deref [_]
                          (@*env :ctx))
                
                      java.lang.AutoCloseable
                
                        (close [_]
                          (watcher/stop! watcher)))]
       (reset! *env
               (-> env
                   (assoc :watcher
                          ret)
                   exec-))
       ret))))
