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

  [env code i-step+]

  (update env
          :step+
          (fn [step+]
            (reduce (fn [step-2+ i-step]
                      (update step-2+
                              i-step
                              (fn [{:as   step
                                    :keys [wrap]}]
                                (assoc step
                                       :code
                                       (cond->
                                         code
                                         wrap
                                         wrap)))))
                    step+
                    i-step+))))



(defn cache-purge

  ""


  ([env]

   (update env
           :step+
           (fn [step+]
             (mapv #(dissoc %
                            :code)
                   step+))))


  ([env path-canonical]

   (update env
           :step+
           (fn [step+]
             (reduce (fn [step-2+ i-step]
                       (update step-2+
                               i-step
                               dissoc
                               :code))
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
           (reduce (fn [ctx {:as   step
                             :keys [code
                                    eval]}]
                     (try
                       ((or eval
                            $.cvm/eval)
                        ctx
                        code)
                       (catch Throwable err
                         (throw (ex-info "During evaluation"
                                         {::error :eval
                                          ::step  step}
                                         err)))))
                   ((or init
                        $.cvm/ctx))
                   (eduction (filter :code)
                             step+))
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
       (merge option+))))


;;;;;;;;;;


(defn load

  ""


  ([step+]

   (load step+
         nil))


  ([step+ option+]

   (let [env  (env step+
                   option+)
         src+ (into []
                    (map (juxt identity
                               read))
                    (keys (env :path->i-step+)))]

     (-> (reduce (fn [env-2 [path src]]
                   (cache env-2
                          src
                          (get-in env-2
                                  [:path->i-step+
                                   path])))
                 env
                 src+)
         exec
         :ctx))))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(let [exec- (fn [env]
              (try
                (exec env)
                (catch Throwable err
                  (let [env-2 (dissoc env
                                      :ctx)]
                    (when-some [f (env :on-error)]
                      (f :error-eval
                         (assoc env-2
                                :error-eval err)))
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

     (let [on-error (option+ :on-error)
           
           *env    (atom nil)
           env     (env step+
                        option+)
           path+   (keys (env :path->i-step+))
           env-2   (reduce (fn [x path]
                             (try
                               (assoc-in x
                                         [:path->code
                                          path]
                                         (read path))
                               (catch Throwable err
                                 (assoc-in x
                                           [:path->error
                                            path]
                                           err))))
                           (assoc env
                                  :path->code  {}
                                  :path->error {})
                           path+)
           env-3   (reduce-kv (fn [env-3 path code]
                                (cache env-3
                                       code
                                       (get-in env-3
                                               [:path->i-step+
                                                path])))
                              (dissoc env-2
                                      :path->code)
                              (env-2 :path->code))

           path->error (env-3 :path->error)
           
           watcher (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                             kind]}]
                                                (let [path (.getCanonicalPath file)]
                                                  (if (identical? kind
                                                                  :delete)
                                                    (swap! *env
                                                           (fn [env]
                                                             (let [env-2 (-> env
                                                                             (cache-purge path)
                                                                             (update :path->error
                                                                                     dissoc
                                                                                     path))]
                                                               (if (seq (env-2 :path->error))
                                                                 env-2
                                                                 (exec- env-2)))))
                                                    (let [[err
                                                           code] (try
                                                                   [nil
                                                                    (read path)]
                                                                   (catch Throwable err
                                                                     [err
                                                                      nil]))]
                                                      (swap! *env
                                                             (fn [env]
                                                               (if code
                                                                 (let [env-2 (cache (update env
                                                                                            :path->error
                                                                                            dissoc
                                                                                            path)
                                                                                    code
                                                                                    (get-in env
                                                                                            [:path->i-step+
                                                                                             path]))]
                                                                   (exec- env-2))
                                                                 (let [env-2 (-> env
                                                                                 (assoc-in [:path->error
                                                                                            path]
                                                                                           err)
                                                                                 (dissoc :ctx))]
                                                                   (when on-error
                                                                     (on-error :path->error
                                                                               env-2))
                                                                   env-2))))))))
                                     :paths   path+}])]
       (reset! *env
               (if (seq path->error)
                 (do
                   (when on-error
                     (on-error :path->error
                               env-3))
                   env-3)
                 (exec- env-3)))

       (reify


         clojure.lang.IDeref

           (deref [_]
             (@*env :ctx))
         

         java.lang.AutoCloseable

           (close [_]
             (watcher/stop! watcher)))))))
