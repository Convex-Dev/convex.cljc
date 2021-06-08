(ns convex.cvm.file

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            load
                            read])
  (:import java.io.File)
  (:require [convex.cvm :as $.cvm]
            [hawk.core  :as watcher]))


;;;;;;;;;;


(defn read

  ""

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;;


(defn cache
  
  ""

  ([env]

   (update env
           :step+
           (fn [step+]
             (reduce-kv cache
                        step+
                        (env :path->i-step+ )))))


  ([env path-canonical]

   (update env
           :step+
           (fn [step+]
             (cache step+
                    path-canonical
                    (get-in env
                            [:path->i-step+
                             path-canonical])))))


  ([step+ path-canonical i-step+]

   (let [cache (try
                 (read path-canonical)
                 (catch Throwable e
                   (throw (ex-info (str "While reading: "
                                        path-canonical)
                                   {::path path-canonical}
                                   e))))]
     (reduce (fn [step-2+ i-step]
               (update step-2+
                       i-step
                       (fn [{:as    step
                             :keys [code]}]
                         (assoc step
                                :cache
                                (cond->
                                  cache
                                  code
                                  code)))))
             step+
             i-step+))))



(defn cache-purge

  ""


  ([env]

   (update env
           :step+
           (fn [step+]
             (mapv #(dissoc %
                            :cache)
                   step+))))


  ([env path-canonical]

   (update env
           :step+
           (fn [step+]
             (reduce (fn [step-2+ i-step]
                       (update step-2+
                               i-step
                               dissoc
                               :cache))
                     step+
                     (get-in env
                             [:path->i-step+
                              path-canonical]))))))



(defn exec

  ""

  [{:as   env
    :keys [after-run
           init
           step+]}]

  (assoc env
         :ctx
         (cond->
           (reduce (fn [ctx {:keys [cache
                                    eval]}]
                     (if cache
                       ((or eval
                            $.cvm/eval)
                        ctx
                        cache)
                       ctx))
                   ((or init
                        $.cvm/ctx))
                   step+)
           after-run
           after-run)))




(defn prepare-step+

  ""

  [step+]

  (reduce (fn [env [i-step [^String path step]]]
               (let [path-2 (.getCanonicalPath (File. path))]
                 (-> env
                     (update-in [:path->i-step+
                                 path-2]
                                (fnil conj
                                      [])
                               i-step)
                     (update :step+
                             conj
                             (assoc step
                                    :i    i-step
                                    :path path-2)))))
             {:path->i-step+ {}
              :step+         []}
             (partition 2
                        (interleave (range)
                                    step+))))




(defn env

  ""

  
  ([step+]

   (env step+
        nil))


  ([step+ option+]

   (-> step+
       prepare-step+
       (merge option+)
       cache)))


;;;;;;;;;;


(defn load

  ""


  ([step+]

   (load step+
         nil))


  ([step+ option+]

   (-> (env step+
            option+)
       exec
       :ctx)))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(defn watch

  "Starts a watcher which syncs Convex Lisp files to a context.

   When a file is modified, its source is processed and all sources are evaluated in the given order, step by step.

   `step+` is a collection of steps, 2-tuples composed of:

   - Path to a Convex Lisp file
   - Map with:

   | Key | Optional? | Value | Default |
   |---|---|---|---|
   | `:code` | True | Function **code from target file** -> **code** (as a Convex object) | `identity` |
   | `:eval` | True | Evaluating function which runs **code** for the target file (as a Convex object) | `convex.cvm/eval` |

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

   (let [env-    (exec (env step+
                            option+))
         *env    (atom nil)
         watcher (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                           kind]}]
                                              (swap! *env
                                                     (fn [env]
                                                       (let [path (.getCanonicalPath file)]
                                                         (exec (if (identical? kind
                                                                               :delete)
                                                                 (cache-purge env
                                                                              cache)
                                                                 (cache env
                                                                        path)))))))
                                   :paths   (keys (env- :path->i-step+))}])]
     (reset! *env
             (assoc env-
                    :watcher
                    watcher))

     (reify


       clojure.lang.IDeref

         (deref [_]
           (@*env :ctx))
       

       java.lang.AutoCloseable

         (close [_]
           (watcher/stop! watcher))))))
