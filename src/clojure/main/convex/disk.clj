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


(defn path-canonical

  "Returns the canonical, unique representation of the given `path`."

  [^String path]

  (.getCanonicalPath (File. path)))



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

   Returns a \"environment\" map which contains:

   | Key | Value |
   |---|---|
   | `:ctx` | If there is no `:error`, the prepared context |
   | `:error` | Signals an error and the absence of `:ctx` (see below) |
   | `:input+` | Vector of requested input file paths |
   | `:input->code` | Map of `input file path` -> `CVM code`, uses `:read` |
   | `:path->cvm-symbol` | Map of `file path` -> `CVM symbol used for interning` |
   | `:read` | Read function used by the [[convex.sync]] namespace, not a user concern |

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
                                       [(path-canonical path)
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

   Can also watch extra files which are not processed but if a change is detected, inputs are not updated and
   the user can decide what to do. Also see [[convex.sync/patch]] and [[convex.sync/eval]].

   `on-change` is like `on-run` in [[load]] but it is also called on every change.

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


  ([sym->path on-change]

   (watch sym->path
          on-change
          nil))


  ([sym->path on-change option+]

   (let [on-change-2 (fn [env]
                       (try
                         (on-change env)
                         (catch Throwable _err
                           (dissoc env
                                   :ctx))))
         ctx         (or (option+ :ctx-base)
                         ($.cvm/ctx))
         a*env       (agent nil)
         env         (-> (load ($.cvm/fork ctx)
                               sym->path)
                         (merge option+)
                         (assoc :ctx-base
                                ctx)
                         (update :cycle
                                 #(or %
                                      0))
                         (update :extra+
                                 #(into #{}
                                        (map path-canonical)
                                        %))
                         on-change-2)
         watcher  (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                            kind]}]
                                               (let [nano-change (System/nanoTime)
                                                     path        (.getCanonicalPath file)]
                                                 (send a*env
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
                                                                                       on-change-2)
                                                                                   env)))))))))))

                                    :paths   (concat (env :extra+)
                                                     (env :input+))}])
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



(defn watch-stop

  "Stops the given [[watcher]]."

  [watcher]

  (.close ^java.lang.AutoCloseable (cond->
                                     watcher
                                     (map? watcher)
                                     :watcher)))
