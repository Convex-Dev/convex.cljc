(ns convex.watch

  "Builds on [[convex.sync/disk]] and [[convex.sync]] utilities for providing a live-reloading example.

   A watcher is a Clojure agent which tracks involved files and always keeps a context in sync. The data it holds is an environemnt
   map akin to what [[convex.sync/disk]] describes. However, it contains more data.

   See [[init]], [[start]]."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.cvm  :as $.cvm]
            [convex.sync :as $.sync]
            [hawk.core   :as watcher]))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(defn ctx

  ""

  [a*env]

  (some-> (@a*env :convex.sync/ctx)
          $.cvm/fork))



(defn init

  "Creates an agent with some initial state which can then be used with [[start]].
  
   `env` will be merged with the environment map (see [[convex.sync/disk]]) and can contain any arbitrary data.

   It **MUST** contain:

   | Key | Value |
   |---|---|
   | `:convex.watch/on-change` | Function `env` -> `env` called after initial load and after each update |

   It **MAY** contain:

   | Key | Value | Default | Can be altered later |
   |---|---|---|---|
   | `:convex.sync/ctx-base` | Base context forked before each evaluation | Result of [[convex.cvm/ctx]] | Yes |
   | `:convex.watch/cycle` | Is incremented each time prior to running `:on-change` | 0 | Yes |
   | `:convex.watch/f*debounce | If present, future that is used for debouncing file changes (not a user concern) |
   | `:convex.watch/extra+` | List of files that ought to be monitored as well | `nil` | No |
   | `:convex.watch/ms-debounce` | Milliseconds, changes  debounced for better behavior with editors and OS | 20 (minimum is 1) | Yes |
   | `:convex.watch/sym->dep | Map of `symbol` -> `path to dependency file` | `nil` | No |

   Just like in [[convex.sync/disk]], files from `:sym->dep` will be loaded in [[start]], interned under their respective symbols.

   Extra files will not be read, only monitored for change. However, in case of change, dependencies will not update and the user
   will be in charge of what should be done. This feature looks peculiar at first but it is used wisely by the [[convex.run]] namespace.
  
   When processed, dependency files go through [[convex.sync/patch]] and [[convex.sync/eval]]."

  [env]

  (agent (-> env
             (update :convex.sync/ctx-base
                     #(or %
                          ($.cvm/ctx)))
             (update :convex.watch/cycle
                     #(or %
                          0))
             (update :convex.watch/extra+
                     #(into #{}
                            (map (fn [^String path]
                                   (.getCanonicalPath (File. path))))
                            %)))))



(defn -start

  "Implementation for [[start]] (sent to the agent).

   Needs a reference to the agent itself.
  
   Kept public since it is useful for building more complex features (see [[convex.run]] namespace)."

  ;; TODO. Error handling when paths do not exist (starting the watcher throws).

  [a*env env]

  (let [sym->dep (env :convex.watch/sym->dep)]
    (-> ($.sync/disk ($.cvm/fork (env :convex.sync/ctx-base))
                     sym->dep)
        (merge env)
        (assoc :convex.watch/watcher
               (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                         kind]}]
                                            (let [nano-change (System/nanoTime)
                                                  path        (.getCanonicalPath file)]
                                              (send-off a*env
                                                        (fn [env]
                                                          (-> env
                                                              (assoc :convex.watch/nano-change
                                                                     nano-change)
                                                              (assoc-in [(if (contains? (env :convex.watch/extra+)
                                                                                        path)
                                                                           :convex.watch/extra->change
                                                                           :convex.sync/input->change)
                                                                         path]
                                                                        kind)
                                                              (update :convex.watch/f*debounce
                                                                      (fn [f*debounce]
                                                                        (some-> f*debounce
                                                                                future-cancel)
                                                                        (future
                                                                          (Thread/sleep (or (env :convex.watch/ms-debounce)
                                                                                            20))
                                                                          (send a*env
                                                                                (fn [env]
                                                                                  (if (= (env :convex.watch/nano-change)
                                                                                         nano-change)
                                                                                    (-> (if (seq (env :convex.watch/extra->change))
                                                                                          env
                                                                                          (-> env
                                                                                              $.sync/patch
                                                                                              $.sync/eval))
                                                                                        (dissoc :convex.watch/f*debounce)
                                                                                        (update :convex.watch/cycle
                                                                                                inc)
                                                                                        ((env :convex.watch/on-change)))
                                                                                    env)))))))))))
                                 :paths   (concat (env :convex.watch/extra+)
                                                  (vals sym->dep))}]))
        ((env :convex.watch/on-change)))))



(defn start

  "After [[init]], actually starts the file watcher.
  
   The environment will contain all key-values that [[convex.sync/disk]] provides since it is being used under the hood.
   In addition, it will also hold (besides what [[init]] describes):

   | Key | Value |
   |---|---|
   | `:convex.sync/input->change` | Like `:extra->change` but for inputs that were not automatically processed |
   | `:convex.watch/extra->change` | A map of `extra path` to one of `#{:create :delete :modify} if any extra path changed |
   | `:convex.watch/nano-change` | Last time change has been detected (uses `System/nanoTime`) |
   | `:convex.watch/watcher` | Actual watcher object, not a user concern |
  
   See [[stop]]."

  [a*env]

  (send-off a*env
            (partial -start
                     a*env)))



(defn -stop

  "Implementation for [[stop]] (sent to the agent).
  
   Kept public since it is useful for building more complex features (see [[convex.run]] namespace)."

  [env]

  (some-> (env :f*:debounce)
          future-cancel)
  (some-> (env :convex.watch/watcher)
          watcher/stop!)
  env)



(defn stop

  "Stops the given watcher."
  
  [a*env]

  (-stop @a*env)
  a*env)
