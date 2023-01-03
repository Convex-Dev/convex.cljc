(ns convex.shell.dep

  (:import (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [babashka.fs            :as bb.fs]
            [clojure.string         :as string]
            [convex.cell            :as $.cell]
            [convex.cvm             :as $.cvm]
            [convex.read            :as $.read]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.std             :as $.std]
            [protosens.git          :as P.git]
            [protosens.process      :as P.process]
            [protosens.string       :as P.string]))

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

  [dep dir]

  (let [dir-2 (-> dir
                  (bb.fs/expand-home)
                  (bb.fs/canonicalize))
        path  (format "%s/project.cvx"
                      dir-2)]
    (try
      (-> path
          ($.read/file)
          (first)
          ($.std/assoc ($.cell/* :dir)
                       ($.cell/string (str dir-2))))
      ;;
      (catch NoSuchFileException _ex
        (throw (ex-info ""
                        {:convex.shell/dep       dep
                         :convex.shell.dep/error :project}))))))



(defn- -read

  ;; Core implementation of [[read]].

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
          (when (some (fn [hash-up]
                        (= hash-up
                           src-hash))
                      (state-2 :convex.shell.dep/downstream))
            (throw (Exception. "Circular dependency")))
          (-> (if dep-required
                (-> state-2
                    (assoc :convex.shell.dep/target   src-hash
                           :convex.shell.dep/required dep-required)
                    (update :convex.shell.dep/downstream
                            conj
                            src-hash)
                    (-read)
                    (merge (select-keys state
                                        [:convex.shell.dep/downstream
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
              k-project    [:git git-url git-sha]]
          (-> state
              (update-in [:convex.shell.dep/project+
                          k-project]
                         #(or %
                              (convex.shell.dep/project k-project
                                                        git-worktree)))
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

  (-read {:convex.shell.dep/downstream []
          :convex.shell.dep/project    :root
          :convex.shell.dep/project+   {:root (project :root
                                                       dir-project)}
          :convex.shell.dep/required   required
          :convex.shell.dep/target     :root}))



(defn deploy-read

  [state]

  (let [target  (state :convex.shell.dep/target)
        address (get-in state
                        [:convex.shell.dep/hash->address target])]
    (if address
      ;;
      (assoc state
             :convex.shell.dep/address
             address)
      ;;
      (if-some [child+ (get-in state
                               [:convex.shell.dep/child+ target])]
        ;;
        (let [state-2 (reduce (fn [state-2 [sym src-hash]]
                                 (let [state-3 (deploy-read (assoc state-2
                                                                   :convex.shell.dep/target
                                                                   src-hash))]
                                   (assoc state-3
                                          :convex.shell.dep/let
                                          (conj (state-2 :convex.shell.dep/let)
                                                sym
                                                (state-3 :convex.shell.dep/address)))))
                               (assoc state
                                      :convex.shell.dep/let
                                      [])
                               child+)
              hash-src (get-in state
                               [:convex.shell.dep/hash->src target])]
          (if hash-src
            (let [ctx-2   ($.cvm/deploy (state-2 :convex.shell/ctx)
                                        ($.std/concat ($.cell/* (let ~($.cell/vector (state-2 :convex.shell.dep/let))))
                                                      hash-src))
                  address ($.cvm/result ctx-2)]
              (-> state-2
                  (assoc :convex.shell/ctx         ctx-2
                         :convex.shell.dep/address address
                         :convex.shell.dep/target  target)
                  (assoc-in [:convex.shell.dep/hash->address
                             target]
                            address)))
            state-2))
        ;;
        (if-some [hash-src (get-in state
                                   [:convex.shell.dep/hash->src
                                    target])]
          (let [ctx-2   ($.cvm/deploy (state :convex.shell/ctx)
                                      ($.std/cons ($.cell/* do)
                                                  hash-src))
                address ($.cvm/result ctx-2)]
            (-> state
                (assoc :convex.shell/ctx         ctx-2
                       :convex.shell.dep/address address)
                (assoc-in [:convex.shell.dep/hash->address
                           target]
                          address)))
          state)))))



(defn deploy

  [env dir-project required]

  (try
    ;;
    (let [state (-> (read dir-project
                          required)
                    (assoc :convex.shell/ctx
                           (env :convex.shell/ctx))
                    (deploy-read))]
      (-> env
          (assoc :convex.shell/ctx
                 (state :convex.shell/ctx))
          ($.shell.ctx/def-current (partition 2
                                              (state :convex.shell.dep/let)))
          ($.shell.ctx/def-result nil)))
    ;;
    (catch clojure.lang.ExceptionInfo ex
      (let [data (ex-data ex)]
        (if-some [error (data :convex.shell.dep/error)]
          (case error
            ;;
            :project
            (let [k-project (data :convex.shell/dep)]
              ($.shell.exec.fail/err env
                                     ($.cell/error ($.cell/code-std* :STATE)
                                                   ($.cell/string (if (identical? k-project
                                                                                  :root)
                                                                    "No `project.cvx` found for the current project"
                                                                    (format "No `project.cvx` found for: %s"
                                                                                         k-project)))))))
          (throw ex))))))