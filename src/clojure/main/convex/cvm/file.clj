(ns convex.cvm.file

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            read])
  (:import java.io.File)
  (:require [convex.cvm     :as $.cvm]
            [convex.cvm.raw :as $.cvm.raw]
            [hawk.core      :as watcher]))


;;;;;;;;;;


(defn read

  ""

  [path]

  (-> path
      slurp
      $.cvm/read))



(defn run

  ""


  ([ctx path]

   (run ctx
        path
        identity))


  ([ctx path wrap-read]

   (let [juice- ($.cvm/juice ctx)]
     (.withJuice ($.cvm/eval ($.cvm/fork ctx)
                             (-> path
                                 read
                                 wrap-read))
                 juice-))))


;;;;;;;;;;


(defn deploy

  ""


  ([ctx sym path]

   (deploy ctx
           sym
           path
           identity))


  ([ctx sym path wrap-read]

   (run ctx
        path
        (fn [parsed]
          ($.cvm.raw/intern-deploy ($.cvm.raw/symbol sym)
                                   (wrap-read parsed))))))



(defn intern

  ""


  ([ctx sym path]

   (intern ctx
           sym
           path
           identity))
          

  ([ctx sym path wrap-read]

   (run ctx
        path
        (fn [parsed]
          ($.cvm.raw/def ($.cvm.raw/symbol sym)
                         (wrap-read parsed))))))


;;;;;;;;;; Watching Convex Lisp files and syncing with a context


(let [-cache     (fn [step+ path i-step+]
                   (let [cache (try
                                 (read path)
                                 (catch Throwable e
                                   (throw (ex-info (str "While reading: "
                                                        path)
                                                   {::path path}
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
                             i-step+)))

      -exec      (fn [{:as   env
                       :keys [after-import
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
                                    (init)
                                    step+)
                            after-import
                            after-import)))]

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
     | `:after-import` | Function **ctx** -> **ctx** run after all steps | `identity` |
     | `:init` | No-arg function which create the initial context prior to running through steps | `convex.cvm/ctx` |

     Reifies `java.lang.AutoCloseable`, hence can be stopped with `.close`."


    ([step+]

     (watch step+
            nil))


    ([step+ option+]

     (let [after-import  (or (:after-import option+)
                             identity)
           init          (or (:init option+)
                             $.cvm/ctx)
           env           (reduce (fn [env [i-step [^String path step]]]
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
                                 {:after-import  after-import
                                  :init          init
                                  :path->i-step+ {}
                                  :step+         []}
                                 (partition 2
                                            (interleave (range)
                                                        step+)))
           path->i-step+ (env :path->i-step+)
           *env          (atom (-> env
                                   (update :step+
                                           (fn [step+]
                                             (reduce-kv -cache
                                                        step+
                                                        path->i-step+)))
                                   -exec))
           watcher       (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                                   kind]}]
                                                      (swap! *env
                                                             (fn [env]
                                                               (let [path    (.getCanonicalPath file)
                                                                     i-step+ (get-in env
                                                                                     [:path->i-step+
                                                                                      path])]
                                                                 (-exec (update env
                                                                                :step+
                                                                                (fn [step+]
                                                                                  (if (identical? kind
                                                                                                  :delete)
                                                                                    (reduce (fn [step-2+ i-step]
                                                                                              (update step-2+
                                                                                                      i-step
                                                                                                      dissoc
                                                                                                      :cache))
                                                                                            step+
                                                                                            i-step+)
                                                                                    (-cache step+
                                                                                            path
                                                                                            i-step+)))))))))
                                           :paths   (keys path->i-step+)}])]

       (reify


         clojure.lang.IDeref

           (deref [_]
             (@*env :ctx))
         

         java.lang.AutoCloseable

           (close [_]
             (watcher/stop! watcher)))))))
