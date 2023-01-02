(ns convex.shell.dep

  (:require [babashka.fs       :as bb.fs]
            [clojure.string    :as string]
            [convex.cell       :as $.cell]
            [convex.read       :as $.read]
            [convex.std        :as $.std]
            [protosens.git     :as P.git]
            [protosens.process :as P.process]
            [protosens.string  :as P.string]))

;;;;;;;;;;


(defn src

  [root path]
  
  (let [root-2 (format "%s/%s"
                       root
                       path)]
    (reduce (fn [acc file]
              (let [^String filename (str file)]
                (if (string/ends-with? filename
                                       ".cvx")
                  (assoc acc
                         (-> (bb.fs/relativize root-2
                                               filename)
                             (str)
                             (P.string/trunc-right 4)
                             (string/replace "/"
                                             ".")
                             ($.cell/symbol))
                         ($.read/file filename))
                  acc)))
            {}
            (file-seq (bb.fs/file root-2)))))



(defn git

  [url sha]

  (let [path     (-> (format "~/.convex-shell/dep/git/%s"
                             (-> ($.cell/string url)
                                 ($.cell/hash)
                                 (str)))
                     (bb.fs/expand-home))
        repo     (format "%s/repo"
                         path)
        worktree (format "%s/worktree/%s"
                         path
                         sha)]
    (when-not (bb.fs/exists? worktree)
      (bb.fs/create-dirs path)
      (when-not (bb.fs/exists? repo)
        (let [p (P.git/exec ["clone"
                             "-l"
                             "--no-tags"
                             url
                             repo])]
          (when-not (P.process/success? p)
            (throw (Exception. (P.process/err p))))))
      (let [p (P.git/exec ["fetch"]
                          {:dir repo})]
        (when-not (P.process/success? p)
          (throw (Exception. (P.process/err p)))))
      (let [p (P.git/exec ["worktree"
                           "add"
                           worktree
                           sha]
                          {:dir repo})]
        (when-not (P.process/success? p)
          (throw (Exception. (P.process/err p))))))
    worktree))



(defn project


  ([]

   (project nil))


  ([dir]

   (let [dir-2 (-> (or dir
                       "./")
                   (bb.fs/expand-home)
                   (bb.fs/canonicalize))
         path  (format "%s/project.cvx"
                       dir-2)]
     (when (bb.fs/exists? path)
       (-> path
           ($.read/file)
           (first)
           ($.std/assoc ($.cell/* :dir)
                        ($.cell/string (str dir-2))))))))



(defn- -walk

  [ctx]

  (if-some [required (ctx :required)]
    (let [[alias
           path
           & required-2] required
          project        (get-in ctx
                                 [:project+ (ctx :project)])
          dep            (get-in project
                                 [($.cell/* :deps)
                                  (first path)])
          dep-type       (get dep
                              ($.cell/* :type))]
      (cond
        (= dep-type
           ($.cell/* :relative))
        (let [src   ($.read/file (format "%s/%s/%s.cvx"
                                         (get project
                                              ($.cell/* :dir))
                                         (get dep
                                              ($.cell/* :relative.path))
                                         (string/join "/"
                                                      (rest path))))
              hash  ($.cell/hash src)
              ctx-2 (-> ctx
                        (assoc-in [:hash->src
                                   hash]
                                  src)
                        (update-in [:dep
                                    (ctx :target)]
                                   (fnil conj
                                         [])
                                   [alias
                                    hash]))
              req   (get (first src)
                         ($.cell/* :require))]
          (when (some (fn [hash-parent]
                        (= hash-parent
                           hash))
                      (ctx :parent+))
            (throw (Exception. "Circular dependency")))
          (-> (if req
                (-> ctx-2
                    (assoc :target   hash
                           :required req)
                    (update :parent+
                            conj
                            hash)
                    (-walk)
                    (merge (select-keys ctx
                                        [:parent+
                                         :target])))
                ctx-2)
              (assoc :required
                     required-2)
              (recur)))
        ;;
        (= dep-type
           ($.cell/* :git))
        (let [sha      (str (get dep
                                 ($.cell/* :git.sha)))
              url      (str (get dep
                                 ($.cell/* :git.url)))
              worktree (git url
                            sha)
              k        [:git worktree]
              ctx-2    (update-in ctx
                                  [:project+ k]
                                  #(or %
                                       (convex.shell.dep/project worktree)))
              ]
          (when (some (fn [hash-parent]
                        (= hash-parent
                           hash))
                      (ctx :parent+))
            (throw (Exception. "Circular dependency")))
          (-> ctx-2
              (assoc :project  k
                     :required ($.cell/* [~alias
                                          ~($.std/next path)]))
              (-walk)
              (merge (select-keys ctx
                                  [:project]))
              (assoc :required
                     required-2)
              (recur)))
        ;;
        :else
        (throw (Exception. "Unknown dependency type"))))
    ctx))



(defn walk

  [dir-project required]

  (-walk {:parent+  []
          :project  :root
          :project+ {:root (project dir-project)}
          :required required
          :target   :root}))
