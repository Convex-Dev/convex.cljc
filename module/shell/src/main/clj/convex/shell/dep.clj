(ns convex.shell.dep

  (:refer-clojure :exclude [import
                            read])
  (:require [babashka.fs       :as bb.fs]
            [clojure.string    :as string]
            [convex.cell       :as $.cell]
            [convex.cvm        :as $.cvm]
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



(defn- -read

  ;; Helper for [[read]].

  [state]

  (if-some [required (state :convex.shell.dep/required)]
    ;; 
    (let [[dep-alias
           dep-path
           & required-2] required
          project        (get-in state
                                 [:convex.shell.dep/project+
                                  (state :convex.shell.dep/project)])
          dep            (get-in project
                                 [($.cell/* :deps)
                                  (first dep-path)])
          dep-type       (first dep)]
      (cond
        (= dep-type
           ($.cell/* :relative))
        (let [src          ($.read/file (format "%s/%s/%s.cvx"
                                                (get project
                                                     ($.cell/* :dir))
                                                (second dep)
                                                (string/join "/"
                                                             (rest dep-path))))
              src-hash     ($.cell/hash src)
              dep-required (get (first src)
                                ($.cell/* :require))
              state-2      (-> state
                               (assoc-in [:convex.shell.dep/hash->src
                                          src-hash]
                                         (cond->
                                           src
                                           dep-required
                                           ($.std/next)))
                               (update-in [:convex.shell.dep/child+
                                           (state :convex.shell.dep/target)]
                                          (fnil conj
                                                [])
                                          [dep-alias
                                           src-hash]))]
          (when (some (fn [hash-parent]
                        (= hash-parent
                           hash))
                      (state-2 :convex.shell.dep/parent+))
            (throw (Exception. "Circular dependency")))
          (-> (if dep-required
                (-> state-2
                    (assoc :convex.shell.dep/target   src-hash
                           :convex.shell.dep/required dep-required)
                    (update :convex.shell.dep/parent+
                            conj
                            src-hash)
                    (-read)
                    (merge (select-keys state
                                        [:convex.shell.dep/parent+
                                         :convex.shell.dep/target])))
                state-2)
              (assoc :convex.shell.dep/required
                     required-2)
              (recur)))
        ;;
        (= dep-type
           ($.cell/* :git))
        (let [git-sha      (str ($.std/nth dep
                                           2))
              git-url      (str ($.std/nth dep
                                           1))
              git-worktree (git git-url
                                git-sha)
              k-project    [:git git-worktree]]
          (-> state
              (update-in [:convex.shell.dep/project+
                          k-project]
                         #(or %
                              (convex.shell.dep/project git-worktree)))
              (assoc :convex.shell.dep/project  k-project
                     :convex.shell.dep/required ($.cell/* [~dep-alias
                                                           ~($.std/next dep-path)]))
              (-read)
              (merge (select-keys state
                                  [:convex.shell/project]))
              (assoc :convex.shell.dep/required
                     required-2)
              (recur)))
        ;;
        :else
        (throw (Exception. "Unknown dependency type"))))
    ;;
    ;; No dependencies are required.
    ;;
    state))



(defn read

  [dir-project required]

  (-read {:convex.shell.dep/parent+  []
          :convex.shell.dep/project  :root
          :convex.shell.dep/project+ {:root (project dir-project)}
          :convex.shell.dep/required required
          :convex.shell.dep/target   :root}))



(defn deploy

  [walked]

  (let [target  (walked :convex.shell.dep/target)
        address (get-in walked
                        [:deployed target])]
    (println :target target address)
    (if address
      ;;
      (assoc walked
             :address
             address)
      ;;
      (if-some [child+ (get-in walked
                             [:convex.shell.dep/child+ target])]
        ;;
        (let [_ (println :child+ child+)
              walked-2 (reduce (fn [walked-2 [sym src-hash]]
                                 (let [walked-3 (deploy (assoc walked-2
                                                               :convex.shell.dep/target
                                                               src-hash))]
                                   (assoc walked-3
                                          :let
                                          (conj (walked-2 :let)
                                                sym
                                                (walked-3 :address)))))
                               (assoc walked
                                      :let
                                      [])
                               child+)
              hash-src (get-in walked
                               [:convex.shell.dep/hash->src target])]
          (if hash-src
            (let [_ (println :deploy hash-src)
                  ctx-2    ($.cvm/deploy (walked-2 :convex.shell/ctx)
                                         ($.std/concat ($.cell/* (let ~($.cell/vector (walked-2 :let))))
                                                       hash-src))
                  address  ($.cvm/result ctx-2)]
              (println :address address)
              (assoc walked-2
                     :address address
                     :convex.shell/ctx ctx-2
                     :deployed (assoc (walked-2 :deployed)
                                      target
                                      address)
                     :convex.shell.dep/target  target))
            walked-2))
        ;;
        (if-some [hash-src (get-in walked
                                   [:convex.shell.dep/hash->src
                                   target])]
          (let [_ (println :deploy hash-src)
                ctx-2   ($.cvm/deploy (walked :convex.shell/ctx)
                                      ($.std/cons ($.cell/* do)
                                                  hash-src))
                address ($.cvm/result ctx-2)]
            (println :address address)
            (assoc walked
                   :address address
                   :convex.shell/ctx     ctx-2
                   :deployed (assoc (walked :deployed)
                                    target
                                    address)
                   ))
          walked)))))



(defn import

  [env dir-project required]

  (merge env
         (select-keys (deploy (assoc (read dir-project
                                           required)
                                     :convex.shell/ctx
                                     (env :convex.shell/ctx)))
                      [:convex.shell/ctx
                       :deployed
                       :let])))
