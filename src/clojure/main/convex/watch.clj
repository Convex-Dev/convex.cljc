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

  (some-> (@a*env :ctx)
          $.cvm/fork))



(defn init

  "Creates an agent with some initial state which can then be used with [[start]].
  
   `env` will be merged with the environment map (see [[convex.sync/disk]]) and can contain any arbitrary data.

   It **MUST** contain:

   | Key | Value |
   |---|---|
   | `:on-change` | Function `env` -> `env` called after initial load and after each update |

   It **MAY** contain:

   | Key | Value | Default | Can be altered later |
   |---|---|---|---|
   | `:ctx-base` | Base context forked before each evaluation | Result of [[convex.cvm/ctx]] | Yes |
   | `:cycle` | Is incremented each time prior to running `:on-change` | 0 | Yes |
   | `:extra+` | List of files that ought to be monitored as well | `nil` | No |
   | `:ms-debounce` | Milliseconds, changes  debounced for better behavior with editors and OS | 20 (minimum is 1) | Yes |
   | `:sym->dep | Map of `symbol` -> `path to dependency file` | `nil` | No |

   Just like in [[convex.sync/disk]], files from `:sym->dep` will be loaded in [[start]], interned under their respective symbols.

   Extra files will not be read, only monitored for change. However, in case of change, dependencies will not update and the user
   will be in charge of what should be done. This feature looks peculiar at first but it is used wisely by the [[convex.run]] namespace."

  [env]

  (agent (-> env
             (update :ctx-base
                     #(or %
                          ($.cvm/ctx)))
             (update :cycle
                     #(or %
                          0))
             (update :extra+
                     #(into #{}
                            (map (fn [^String path]
                                   (.getCanonicalPath (File. path))))
                            %)))))



(defn -start

  "Implementation for [[start]] (sent to the agent).

   Needs a reference to the agent itself.
  
   Kept public since it is useful for building more complex features (see [[convex.run]] namespace)."

  [a*env env]

  (let [sym->dep (env :sym->dep)]
    (-> ($.sync/disk ($.cvm/fork (env :ctx-base))
                     sym->dep)
        (merge env)
        (assoc :watcher
               (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                         kind]}]
                                            (let [nano-change (System/nanoTime)
                                                  path        (.getCanonicalPath file)]
                                              (send-off a*env
                                                        (fn [env]
                                                          (-> env
                                                              (assoc :nano-change
                                                                     nano-change)
                                                              (assoc-in [(if (contains? (env :extra+)
                                                                                        path)
                                                                           :extra->change
                                                                           :input->change)
                                                                         path]
                                                                        kind)
                                                              (update :f*debounce
                                                                      (fn [f*debounce]
                                                                        (some-> f*debounce
                                                                                future-cancel)
                                                                        (future
                                                                          (Thread/sleep (or (env :ms-debounce)
                                                                                            20))
                                                                          (send a*env
                                                                                (fn [env]
                                                                                  (if (= (env :nano-change)
                                                                                         nano-change)
                                                                                    (-> (if (seq (env :extra->change))
                                                                                          env
                                                                                          (-> env
                                                                                              $.sync/patch
                                                                                              $.sync/eval))
                                                                                        (dissoc :f*debounce)
                                                                                        (update :cycle
                                                                                                inc)
                                                                                        ((env :on-change)))
                                                                                    env)))))))))))
                                 :paths   (concat (env :extra+)
                                                  (vals sym->dep))}]))
        ((env :on-change)))))



(defn start

  "After [[init]], actually starts the file watcher.
  
   The environment will contains all key-values that [[convex.sync/disk]] provides since it is being used under the hood.
   In addition, it will also hold (besides what [[init]] describes):

   | Key | Value |
   |---|---|
   | `:extra->change` | A map of `extra path` to one of `#{:create :delete :modify} if any extra path changed |
   | `:input->change` | Like `:extra->change` but for inputs that were not automatically processed |
   | `:nano-change` | Last time change has been detected (uses `System/nanoTime`) |
   | `:watcher` | Actual watcher object, not a user concern |
  
   See [[stop]]."

  [a*env]

  (send-off a*env
            (partial -start
                     a*env)))



(defn -stop

  "Implementation for [[stop]] (sent to the agent).
  
   Kept public since it is useful for building more complex features (see [[convex.run]] namespace)."

  [env]

  (some-> (env :watcher)
          watcher/stop!)
  env)



(defn stop

  "Stops the given watcher."
  
  [a*env]

  (-stop @a*env)
  a*env)
