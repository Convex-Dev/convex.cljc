(ns convex.shell.dep        

  (:import (convex.core.exceptions ParseException)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [babashka.fs            :as bb.fs]
            [clojure.string         :as string]
            [convex.cell            :as $.cell]
            [convex.cvm             :as $.cvm]
            [convex.read            :as $.read]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.err       :as $.shell.err]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.kw        :as $.shell.kw]
            [convex.std             :as $.std]
            [protosens.git          :as P.git]
            [protosens.process      :as P.process]
            [protosens.string       :as P.string]))

;;;;;;;;;;


(defn read-file

  [state src-path]

  (let [fail (fn [message]
               (throw (ex-info ""
                               {:convex.shell/exception (-> ($.shell.err/reader-file ($.cell/string src-path)
                                                                                     (some-> message
                                                                                             ($.cell/string)))
                                                            
                                                            ($.std/assoc ($.cell/* :ancestry)
                                                                         (state :convex.shell.dep/ancestry)))})))]
    (try
      ;;
      ($.read/file src-path)
      ;;
      (catch NoSuchFileException _ex
        (fail "File not found"))
      ;;
      (catch ParseException ex
        (fail (.getMessage ex)))
      ;;
      (catch Throwable _ex
        (fail nil)))))



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





(def ^:private git-url-regex
  #"([a-z0-9+.-]+):\/\/(?:(?:(?:[^@]+?)@)?([^/]+?)(?::[0-9]*)?)?(/[^:]+)")

(def ^:private git-scp-regex
  #"(?:(?:[^@]+?)@)?(.+?):([^:]+)")


(defn git-path
  "Convert url into a safe relative path (this is not a reversible transformation)
  based on scheme, host, and path (drop user and port).
  Examples:
    ssh://git@gitlab.com:3333/org/repo.git     => ssh/gitlab.com/org/repo
    git@github.com:dotted.org/dotted.repo.git  => ssh/github.com/dotted.org/dotted.repo
    file://../foo                              => file/REL/_DOTDOT_/foo
    file:///Users/user/foo.git                 => file/Users/user/foo
    ../foo                                     => file/REL/_DOTDOT_/foo
    ~user/foo.git                              => file/REL/_TILDE_user/foo
  * https://git-scm.com/docs/git-clone#_git_urls
  * https://git-scm.com/book/en/v2/Git-on-the-Server-The-Protocols
  "
  [parent-project-dir url]
  (let [[scheme
         host
         path]     (cond
                     ;;
                     (string/starts-with? url "file://")
                     ["file"
                      nil
                      (-> url
                          (subs 7)
                          #_(string/replace #"^([^/])"
                                          "REL/$1"))]
                     ;;
                     (string/includes? url
                                       "://")
                     (let [[_
                            s
                            h
                            p] (re-matches git-url-regex
                                           url)]
                       [s
                        h
                        p])
                     ;;
                     (string/includes? url
                                       ":")
                     (let [[_
                            h
                            p] (re-matches git-scp-regex
                                           url)]
                       ["ssh"
                        h
                        p])
                     ;;
                     :else
                     ["file"
                      nil
                      url
                      #_(string/replace url
                                      #"^([^/])"
                                      "REL/$1")])
        clean-path (-> path
                       (string/replace #"\.git/?$"
                                       "")         ;; remove trailing .git or .git/
                       )
        clean-path-2 (if (= scheme
                            "file")
                       (-> (if (bb.fs/relative? clean-path)
                             (format "%s/%s"
                                     parent-project-dir
                                     clean-path)
                             clean-path)
                           (bb.fs/canonicalize)
                           (str))
                       clean-path)
        dir-parts  (->> (concat [scheme host]
                                (string/split clean-path-2
                                              #"/")) ;; split on /
                        (remove string/blank?) ;; remove any missing path segments
                        (map #(-> % ({"." "_DOT_", ".." "_DOTDOT_"} %))))] ;; replace . or .. segments
    [scheme host clean-path]
    dir-parts
    [scheme (string/join "___" (rest dir-parts))]
    (string/join "/"
                 dir-parts)
    ))



(comment

  (git-path "/root" "a/b/../foo/bar")
  (git-path "./" "/a/b")
  (git-path "./" "ssh://git@gitlab.com:3333/org/repo.git")
  (git-path "./" "https://github.com/clojure/tools.gitlibs.git")
  (git-path "./" "git@github.com:dotted.org/dotted/../repo.git")
  )



(defn git

  [foreign-parent? parent-project-dir url sha]

  (let [path-rel (git-path parent-project-dir
                           url)
        path     (-> (format "~/.convex-shell/dep/git/%s"
                             path-rel)
                     (bb.fs/expand-home))
        repo     (format "%s/repo"
                         path)
        worktree (format "%s/worktree/%s"
                         path
                         sha)
        fail     (fn [message]
                   (throw (ex-info ""
                                   {:convex.shell/exception
                                    ($.shell.err/git ($.cell/string message)
                                                     ($.cell/string url)
                                                     ($.cell/string sha))})))
        scheme-file? (string/starts-with? path-rel
                                          "file")
        scheme-file-relative? (and scheme-file?
                                   (bb.fs/relative? url))]
    (when (and foreign-parent?
               (or (and scheme-file-relative?
                        (not (string/starts-with? (-> (format "%s/%s"
                                                              parent-project-dir
                                                              url)
                                                      (bb.fs/canonicalize)
                                                      (str))
                                                  parent-project-dir)))
                   (and scheme-file?
                        (not (bb.fs/relative? url)))))
      (fail "Foreign project requested a local Git dependency from outside its directory"))
    (when-not (bb.fs/exists? worktree)
      (bb.fs/create-dirs path)
      (when-not (bb.fs/exists? repo)
        (let [p (P.git/exec ["clone"
                             "--quiet"
                             "--no-tags"
                             (if (and scheme-file?
                                      (bb.fs/relative? url))
                               (format "%s/%s"
                                       parent-project-dir
                                       url)
                               url)
                             repo])]
          (when-not (P.process/success? p)
            (fail "Unable to clone Git repository"))))
      (let [p (P.git/exec ["fetch"
                           "--quiet"
                           "origin"
                           sha]
                          {:dir repo})]
        (when-not (P.process/success? p)
          (fail "Unable to fetch Git repository")))
      (let [p (P.git/exec ["worktree"
                           "add"
                           worktree
                           sha]
                          {:dir repo})]
        (when-not (P.process/success? p)
          (fail "Unable to create worktree for Git repository under requested rev"))))
    [(not scheme-file?)
     worktree]))



(defn validate-project

  [project fail dir]

  (let [fail-2 (fn [message]
                 (fail ($.cell/error ($.cell/code-std* :ARGUMENT)
                                     ($.cell/string message))))]
    (when-not ($.std/map? project)
      (fail-2 "`project.cvx` must be a map"))
    (when-some [dep+ ($.std/get project
                                ($.cell/* :deps))]
      (when-not ($.std/map? dep+)
        (fail-2 "`:deps` in `project.cvx` must be a map"))
      (doseq [[sym
               dep] dep+]
        (when-not ($.std/vector? dep)
          (fail-2 (format "`%s` dependency in `project.cvx` must be a vector"
                          sym)))
        (let [resolution  (first dep)
              not-string? (fn [x]
                            (or (not ($.std/string? x))
                                ($.std/empty? x)))]
          (when (nil? resolution)
            (fail-2 (format "Missing resolution mechanism for `%s` dependency in `project.cvx`"
                            sym)))
          (cond
            (= resolution
               ($.cell/* :git))
            (do
              (when-not (= ($.std/count dep)
                           3)
                (fail-2 (format "Git dependency `%s` in `project.cvx` must contain a URL and a SHA"
                                sym)))
              (when (not-string? ($.std/nth dep
                                            1))
                (fail-2 (format "Git dependency `%s` in `project.cvx` must specify the repository URL as a string"
                                sym)))
              (when (not-string? ($.std/nth dep
                                            2))
                (fail-2 (format "Git dependency `%s` in `project.cvx` must specify a commit SHA as a string"
                                sym))))
            ;;
            (= resolution
               ($.cell/* :relative))
            (do
              (when (< ($.std/count dep)
                        2)
                (fail-2 (format "Relative dependency `%s` in `project.cvx` must specify a path"
                                sym)))
              (let [path ($.std/nth dep
                                    1)]
                (when (not-string? path)
                  (fail-2 (format "Relative dependency `%s` in `project.cvx` must specify a path as a string"
                                  sym)))
                (when-not (string/starts-with? (str (bb.fs/canonicalize (str dir "/" path)))
                                               dir)
                  (fail-2 (format "Relative dependency `%s` in `project.cvx` specifies a path outside of the project"
                                  sym)))))
            ;;
            :else
            (fail-2 (format "Unknown resolution mechanism for `%s` dependency in `project.cvx`: %s"
                            sym
                            resolution)))))))
  project)



(defn project

  [dep dir]

  (let [dir-2 (-> dir
                  (bb.fs/expand-home)
                  (bb.fs/canonicalize)
                  (str))
        path  (format "%s/project.cvx"
                      dir-2)
        fail  (fn [shell-ex]
                (throw (ex-info ""
                                {:convex.shell/exception
                                 (-> shell-ex
                                     ($.std/assoc $.shell.kw/dir
                                                  ($.cell/string dir-2))
                                     ($.std/assoc $.shell.kw/project
                                                  dep))})))]
    (try
      (-> path
          ($.read/file)
          (first)
          (validate-project fail
                            dir-2)
          ($.std/assoc ($.cell/* :dir)
                       ($.cell/string dir-2)))
      ;;
      (catch NoSuchFileException _ex
        (fail ($.cell/error $.shell.kw/err-stream
                            ($.cell/string (str "`project.cvx` not found for "
                                                (if (= dep
                                                       $.shell.kw/root)
                                                  "the requested project"
                                                  "a dependency"))))))
      ;;
      (catch ParseException ex
        (fail ($.shell.err/reader-file ($.cell/string path)
                                       ($.cell/string (format "Cannot read `project.cvx`: %s"
                                                              (.getMessage ex)))))))))



(defn- -read

  ;; Core implementation of [[read]].

  [state]

  (if-some [required (state :convex.shell.dep/required)]
    ;; 
    (let [[dep-alias
           dep-path
           & required-2] required
          k-project      (state :convex.shell.dep/project)
          project        (get-in state
                                 [:convex.shell.dep/project+
                                  k-project])
          dep            (get-in project
                                 [($.cell/* :deps)
                                  (first dep-path)])
          dep-type       (first dep)]
      (cond
        (= dep-type
           ($.cell/* :relative))
        (let [ancestry     (-> (state :convex.shell.dep/ancestry)
                               ($.std/conj ($.cell/* [~k-project
                                                      ~dep-path])))
              state-2      (assoc state
                                  :convex.shell.dep/ancestry
                                  ancestry)
              src-path     (format "%s/%s/%s.cvx"
                                   (get project
                                        ($.cell/* :dir))
                                   (second dep)
                                   (string/join "/"
                                                (rest dep-path)))
              src          (read-file state-2
                                      src-path)
              src-hash     ($.cell/hash src)
              dep-required (get (first src)
                                ($.cell/* :require))
              state-3      (-> state-2
                               (update-in [:convex.shell.dep/hash->ancestry
                                           src-hash]
                                          #(or %
                                               ancestry))
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
          (when (contains? (state-3 :convex.shell.dep/downstream)
                           src-hash)
            (throw (ex-info ""
                            {:convex.shell/exception
                             (-> ($.cell/error ($.cell/code-std* :STATE)
                                               ($.cell/string "Circular dependency"))
                                 ($.std/assoc ($.cell/* :ancestry)
                                              (state-3 :convex.shell.dep/ancestry)))})))
          (-> (if dep-required
                (-> state-3
                    (assoc :convex.shell.dep/target   src-hash
                           :convex.shell.dep/required dep-required)
                    (update :convex.shell.dep/downstream
                            conj
                            src-hash)
                    (-read)
                    (merge (select-keys state
                                        [:convex.shell.dep/downstream
                                         :convex.shell.dep/target])))
                state-3)
              (assoc :convex.shell.dep/required
                     required-2)
              (merge (select-keys state
                                  [:convex.shell.dep/ancestry]))
              (recur)))
        ;;
        (= dep-type
           ($.cell/* :git))
        (let [git-sha       ($.std/nth dep
                                       2)
              git-url       ($.std/nth dep
                                       1)
              [foreign?
               git-worktree] (git (state :convex.shell.dep/foreign?)
                                  (str ($.std/get project
                                                  $.shell.kw/dir))
                                  (str git-url)
                                  (str git-sha))
              k-project    ($.cell/* [:git ~git-url ~git-sha])]
          (-> state
              (update-in [:convex.shell.dep/project+
                          k-project]
                         #(or %
                              (convex.shell.dep/project k-project
                                                        git-worktree)))
              (assoc :convex.shell.dep/foreign? foreign?
                     :convex.shell.dep/project  k-project
                     :convex.shell.dep/required ($.cell/* [~dep-alias
                                                           ~($.std/next dep-path)]))
              (-read)
              (merge (select-keys state
                                  [:convex.shell.dep/foreign?
                                   :convex.shell.dep/project]))
              (assoc :convex.shell.dep/required
                     required-2)
              (recur)))))
    ;;
    ;; No dependencies are required.
    ;;
    state))



(defn read

  [dir-project required]

  (-read {:convex.shell.dep/ancestry   ($.cell/* [])
          :convex.shell.dep/downstream #{}
          :convex.shell.dep/foreign?   false
          :convex.shell.dep/project    $.shell.kw/root
          :convex.shell.dep/project+   {$.shell.kw/root (project $.shell.kw/root
                                                                 dir-project)}
          :convex.shell.dep/required   required
          :convex.shell.dep/target     $.shell.kw/root}))




(defn- -deploy-actor

  [state target code]

  (let [ctx ($.cvm/deploy (state :convex.shell/ctx)
                          code)
        ex  ($.cvm/exception ctx)]
    (if ex
      (throw (ex-info ""
                      {:convex.shell/exception (-> ex
                                                   ($.shell.err/mappify)
                                                   ($.std/assoc ($.cell/* :ancestry)
                                                                (get-in state
                                                                        [:convex.shell.dep/hash->ancestry
                                                                         target])))}))
      (let [address ($.cvm/result ctx)]
        (-> state
            (assoc :convex.shell/ctx         ctx
                   :convex.shell.dep/address address)
            (assoc-in [:convex.shell.dep/hash->address
                       target]
                      address))))))



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
            (-> (-deploy-actor state-2
                               target
                               ($.std/concat ($.cell/* (let ~($.cell/vector (state-2 :convex.shell.dep/let))))
                                             hash-src))
                (assoc :convex.shell.dep/target
                       target))
            state-2))
        ;;
        (if-some [hash-src (get-in state
                                   [:convex.shell.dep/hash->src
                                    target])]
          (-deploy-actor state
                         target
                         ($.std/cons ($.cell/* do)
                                     hash-src))
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
      (if-some [shell-ex (:convex.shell/exception (ex-data ex))]
        ($.shell.exec.fail/err env
                               shell-ex)
        (throw ex)))))
