(ns convex.watch

  "Loading Convex Lisp files in contextes + live-reloading.
  
   See [[convex.example.disk]] namespace for examples."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.disk :as $.disk]
            [convex.cvm  :as $.cvm]
            [convex.sync :as $.sync]
            [hawk.core   :as watcher]))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(defn ctx

  ""

  [a*env]

  (some-> (@a*env :ctx)
          $.cvm/fork))



(defn init

  ""

  [env]

  (agent (-> env
             (update :cycle
                     #(or %
                          0))
             (update :ctx-base
                     #(or %
                          ($.cvm/ctx)))
             (update :extra+
                     #(into #{}
                            (map $.disk/path-canonical)
                            %)))))



(defn -start

  ""

  [a*env env]

  (let [sym->dep (env :sym->dep)]
    (-> ($.disk/load ($.cvm/fork (env :ctx-base))
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

  "Exactly like [[load]] but live-reloads the given files on change.

   Can also watch extra files which are not processed but if a change is detected, inputs are not updated and
   the user can decide what to do. Also see [[convex.sync/patch]] and [[convex.sync/eval]].

   `on-change` is like `on-run` in [[load]] but it is also called on every change. It must handle errors and always
   returned an environment map.

   The environment map it receives contains in addition:

   | Key | Value |
   |---|---|
   | `:ctx-base` | Context that is used and forked for processing any update |
   | `:cycle` | Starts at 0, is incremented each time prior to calling `on-change` |
   | `:extra+| If any, set of extra paths that are being monitored |
   | `:extra->change` | A map of `extra path` to one of `#{:create :delete :modify} if any extra path changed |
   | `:input->change` | Like `:extra->change` but for inputs that were not automatically processed |
   | `:ms-debounce` | Milliseconds, changes are debounced for better behavior with editors and OS |
   | `:nano-change` | Last time change has been detected (uses `System/nanoTime`) |
   | `:watcher` | Actual watcher object, not a user concern |

   Any of these key-values can be altered in `on-change` if needed.

   `option+` is a map which can contain any of those key-values for initializing the required behavior.

   Eg. Translating example in [[load]]:

   ```clojure
   (def w*ctx
        (convex.disk/watch {'my-lib \"./path/to/lib.cvx\"}
                           (fn [env]
                             (update env
                                     :ctx
                                     convex.clj.eval/ctx
                                     '(def my-lib
                                           (deploy my-lib))))))

   (deref w*ctx)

   (.close w*ctx)
   ```"

  [a*env]

  (send-off a*env
            (partial -start
                     a*env)))





(defn -stop

  ""

  [env]

  (some-> (env :watcher)
          watcher/stop!)
  env)



(defn stop

  "Stops the given [[watcher]]."
  
  [a*env]

  (-stop @a*env)
  a*env)
