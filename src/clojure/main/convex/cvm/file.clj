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


(let [-code      (fn [sym path]
                   ($.cvm.raw/intern-deploy sym
                                            (try
                                              ($.cvm/read (slurp path))
                                              (catch Throwable e
                                                (throw (ex-info (str "While reading: "
                                                                     path)
                                                                {::path path}
                                                                e))))))
      -ctx-watch (fn [f import+]
                   (f ($.cvm/eval ($.cvm/ctx)
                                  ($.cvm.raw/do (map :code
                                                     (vals import+))))))]
  (defn watch

    "Starts a watcher which syncs Convex Lisp files to a context.

     Returns a object which can be `deref` into that synced context.
    
     The given files are first imported as libraries just like in [[import]]. Then, everytime one of those
     files is modified or deleted, a fresh context is created with all updates.
    
     Very useful for setting up a base context which loads a bunch of files and is then used for development and testing:

     ```clojure
     (def ctx
          (watch {\"some/lib.cvx\" 'lib}))

     (eval (fork @ctx)
           '(lib/my-func 42))
     ```

     Supported options are:

     | Key | Value |
     |---|---|
     | `:after-import` | Function which maps the prepared context after all imports |
    
     Reifies `java.lang.AutoCloseable`, can be stopped with `.close`."


    ([path->alias]

     (watch path->alias
            nil))


    ([path->alias option+]

     (let [after-import (or (:after-import option+)
                            identity)
           import+      (reduce (fn [import+ [^String path sym]]
                                  (let [path-2 (.getCanonicalPath (File. path))
                                        sym-2  ($.cvm.raw/symbol sym)]
                                    (assoc import+
                                           path-2
                                           {:code  (-code sym-2
                                                          path-2)
                                            :sym  sym-2})))
                                {}
                                path->alias)
           *state       (atom {:ctx     (-ctx-watch after-import
                                                    import+)
                               :import+ import+})
           watcher      (watcher/watch! [{:handler (fn [_ {:keys [^File file
                                                                  kind]}]
                                                     (swap! *state
                                                            (fn [{:as   state
                                                                  :keys [import+]}]
                                                              (let [path      (.getCanonicalPath file)
                                                                    import-2+ (if (= kind
                                                                                     :delete)
                                                                                (update import+
                                                                                        path
                                                                                        dissoc
                                                                                        :code)
                                                                                (update import+
                                                                                        path
                                                                                        (fn [{:as   import-
                                                                                              :keys [sym]}]

                                                                                          (assoc import-
                                                                                                 :code
                                                                                                 (-code sym
                                                                                                        path)))))]
                                                                (assoc state
                                                                       :ctx     (-ctx-watch after-import
                                                                                            import-2+)
                                                                       :import+ import-2+))))
                                                     nil)
                                          :paths   (keys import+)}])]
       (reify


         clojure.lang.IDeref

           (deref [_]
             (@*state :ctx))
         

         java.lang.AutoCloseable

           (close [_]
             (watcher/stop! watcher)))))))
