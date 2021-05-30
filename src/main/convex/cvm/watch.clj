(ns convex.cvm.watch

  ""

  {:author "Adam Helinski"}

  (:import java.io.File)
  (:require [convex.cvm  :as $.cvm]
            [convex.lisp :as $.lisp]
            [hawk.core   :as watcher]))


;;;;;;;;;;


(defn ^:no-doc -ctx

  ;;

  [f import+]

  (f ($.cvm/eval ($.cvm/ctx)
                 ($.cvm/read-form ($.lisp/templ* (do
                                                   ~@(map :code
                                                          (vals import+))))))))



(defn ^:no-doc -read-code

  ;;

  [path alias]

  ($.lisp/templ* (let [addr (deploy (quote ~($.lisp/literal (slurp path))))]
                   (def *aliases*
                        (assoc *aliases*
                               (quote ~alias)
                               addr)))))



(defn start

  ""


  ([path->alias]

   (start path->alias
          identity))


  ([path->alias f]

   (let [import+ (reduce-kv (fn [import+ ^String path alias]
                              (let [path-2 (.getCanonicalPath (File. path))]
                                (assoc import+
                                       path-2
                                       {:alias alias
                                        :code  (-read-code path-2
                                                           alias)})))
                            {}
                            path->alias)
         *state  (atom {:ctx     (-ctx f
                                       import+)
                        :import+ import+})
         watcher (watcher/watch! [{:handler (fn [_ctx {:keys [^File file
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
                                                                                       :keys [alias]}]

                                                                                   (assoc import-
                                                                                          :code
                                                                                          (-read-code path
                                                                                                      alias)))))]
                                                         (assoc state
                                                                :ctx     (-ctx f
                                                                               import-2+)
                                                                :import+ import-2+))))
                                              nil)
                                   :paths   (keys import+)}])]
     (reify


       clojure.lang.IDeref

         (deref [_]
           (let [ctx (@*state :ctx)]
             (if ($.cvm/exception? ctx)
               ctx
               ($.cvm/fork ctx))))
       

       java.lang.AutoCloseable

         (close [_]
           (watcher/stop! watcher))))))
