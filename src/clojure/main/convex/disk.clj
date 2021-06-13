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


;;;;;;;;;; Miscellaneous


(defn read

  "Reads the file located at `path` and returns Convex code."

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;; Loading files once


(defn load

  "Into the given `ctx` (or a newly created one if not provided), reads the given files and intern then as unevaluted code
   under their respective symbols.

   Returns a map which contains the prepared `:ctx` or an `:error` if something failed.

   This interned code can they be used as needed through the power of Lisp. Typically, either `deploy` or `eval` is used.

   Eg. Reading a file and deploying as a library (without error checking):
   
   ```clojure
   (-> (convex.disk/load {'my-lib \"./path/to/lib.cvx\"})
       :ctx
       (convex.clj.eval/ctx '(def my-lib
                                  (deploy my-lib))))
   ```

   An `:error` is a 2-tuple vector where the first item is a keyword indicating an error type and second item is information:

   | Position 0 | Position 1 |
   |---|---|
   | `:input->error` | Map of `file path` -> `Java exception occured during reading` |
   | `eval` | Map with `:exception` (either Java or CVM exception) and `:input` (which input caused this evaluation error) |"


  ([sym->path]

   (load ($.cvm/ctx)
         sym->path))


  ([ctx sym->path]

   (let [input+        (reduce (fn [input+ [sym ^String path]]
                                 (conj input+
                                       [(.getCanonicalPath (File. path))
                                        ($.code/symbol (str sym))]))
                               []
                               sym->path)
         path->cvm-sym (into {}
                             input+)
         read-input    (fn [env path]
                         (try
                           ($.sync/assoc-code env
                                              path
                                              ($.code/def (path->cvm-sym path)
                                                          ($.code/quote (read path))))
                           (catch Throwable ex
                             ($.sync/assoc-err-read env
                                                    path
                                                    ex))))]
     ($.sync/eval ($.sync/load {:input+        (mapv first
                                                     input+)
                                :path->cvm-sym path->cvm-sym
                                :read          read-input})
                  ctx))))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(defn watch

  "Exactly like [[load]] but live-reloads the given files on change.

   After interning all files on a forked `ctx`, `on-run` is called with the same map as in [[load]].

   Returns an object which can be be `deref` to a fork of an always updated context.

   Eg. Translating example in [[load]]:

   ```clojure
   (def w*ctx
        (convex.disk/load {'my-lib \"./path/to/lib.cvx\"}
                          (fn [env]
                            (update env
                                    :ctx
                                    convex.clj.eval/ctx
                                    '(def my-lib
                                          (deploy my-lib))))))

   (deref w*ctx)

   (.close w*ctx)
   ```"


  ([sym->path on-run]

   (watch ($.cvm/ctx)
          sym->path
          on-run))


  ([ctx sym->path on-run]

   (let [on-run-2 (fn [env]
                    (try
                      (on-run env)
                      (catch Throwable _err
                        (dissoc env
                                :ctx))))
         a*env    (agent nil)
         env      (-> (load ($.cvm/fork ctx)
                            sym->path)
                      on-run-2)
         watcher  (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                            kind]}]
                                               (let [nano-change (System/nanoTime)
                                                     path        (.getCanonicalPath file)]
                                                 (send a*env
                                                       (fn [env]
                                                         (-> env
                                                             (assoc-in [:path->change
                                                                        path]
                                                                       kind)
                                                             (assoc :nano-change
                                                                    nano-change)
                                                             (update :f*debounce
                                                                     (fn [f*debounce]
                                                                       (some-> f*debounce
                                                                               future-cancel)
                                                                       (future
                                                                         (Thread/sleep 20)
                                                                         (send a*env
                                                                               (fn [env]
                                                                                 (if (= (env :nano-change)
                                                                                        nano-change)
                                                                                   (-> (reduce-kv (fn [env-2 path change]
                                                                                                    ((if (identical? change
                                                                                                                     :delete)
                                                                                                       $.sync/unload
                                                                                                       $.sync/reload)
                                                                                                     env-2
                                                                                                     path))
                                                                                                  env
                                                                                                  (env :path->change))
                                                                                       (dissoc :f*debounce
                                                                                               :path->change)
                                                                                       ($.sync/eval ($.cvm/fork ctx))
                                                                                       on-run-2)
                                                                                   env)))))))))))

                                    :paths   (env :input+)}])
         ret      (reify

                    clojure.lang.IDeref
              
                      (deref [_]
                        (some-> (@a*env :ctx)
                                $.cvm/fork))
              
                    java.lang.AutoCloseable
              
                      (close [_]
                        (watcher/stop! watcher)))]
     (send a*env
           (constantly (assoc env
                              :watcher
                              watcher)))
     ret)))
