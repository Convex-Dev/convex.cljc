(ns convex.shell.dep.git

  "Git dependencies are a convenient way of exposing Convex Lisp project
   over the Internet."

  {:author "Adam Helinski"}

  (:import (convex.core.init Init))
  (:require [babashka.fs           :as bb.fs]
            [clojure.string        :as string]
            [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.shell.dep.fail :as $.shell.dep.fail]
            [convex.std            :as $.std]
            [protosens.git         :as P.git]
            [protosens.process     :as P.process]))


;;;;;;;;;; Regular expressions


(def re-scp
  #"(?:(?:[^@]+?)@)?(.+?):([^:]+)")



(def re-url

  #"([a-z0-9+.-]+):\/\/(?:(?:(?:[^@]+?)@)?([^/]+?)(?::[0-9]*)?)?(/[^:]+)")


;;;;;;;;;; Git operations and related utilities


(defn path-cache-repo

  ;; Adapted from https://github.com/clojure/tools.gitlibs/blob/master/src/main/clojure/clojure/tools/gitlibs/impl.clj#L54-L81

  [dir-project-parent url]

  (let [[scheme
         host
         path]       (cond
                       ;;
                       (string/starts-with? url "file://")
                       ["file"
                        nil
                        (subs url
                              7)]
                       ;;
                       (string/includes? url
                                         "://")
                       (rest (re-matches re-url
                                         url))
                       ;;
                       (string/includes? url
                                         ":")
                       (let [[_scheme
                              host
                              path] (re-matches re-scp
                                                url)]
                         ["ssh"
                          host
                          path])
                       ;;
                       :else
                       ["file"
                        nil
                        url])
        path-2    ;; Removes trailing `.git` or `.git/`.
                  (string/replace path
                                  #"\.git/?$"
                                  "")
        path-3    (if (= scheme
                         "file")
                    (-> (if (bb.fs/relative? path-2)
                          (format "%s/%s"
                                  dir-project-parent
                                  path-2)
                          path-2)
                        (bb.fs/canonicalize)
                        (str))
                    path-2)]
    (->> (concat [scheme
                  host]
                 (string/split path-3
                               #"/"))
         (remove string/blank?)
         (map (fn [dir]
                (get {"."  "_DOT_"
                      ".." "_DOTDOT_"}
                     dir
                     dir)))
         (string/join "/"))))



(defn worktree

  "Clones a repo and creates a worktree for the desired SHA (if none of this hasn't been
   done already."

  [env dir-project-parent url sha]

  (let [path-rel     (path-cache-repo dir-project-parent
                                      url)
        path         (-> (format "%s/dep/git/%s"
                                 ($.cvm/look-up (env :convex.shell/ctx)
                                                Init/CORE_ADDRESS
                                                ($.cell/* .shell.*root*))
                                 path-rel)
                         (bb.fs/expand-home))
        repo         (format "%s/repo"
                             path)
        worktree     (format "%s/worktree/%s"
                             path
                             sha)
        fail         (fn [message]
                       ($.shell.dep.fail/with-ancestry (env :convex.shell/ctx)
                                                       ($.cell/* :SHELL.DEP.GIT)
                                                       ($.cell/* {:message  ~($.cell/string message)
                                                                  :sha      ~($.cell/string sha)
                                                                  :url      ~($.cell/string url)})
                                                       (env :convex.shell.dep/ancestry)))
        scheme-file? (string/starts-with? path-rel
                                          "file")]
    (when (and (env :convex.shell.dep/foreign?)
               scheme-file?
               (or (bb.fs/absolute? url)
                   (not (string/starts-with? (-> (format "%s/%s"
                                                         dir-project-parent
                                                         url)
                                                 (bb.fs/canonicalize)
                                                 (str))
                                             dir-project-parent))))
      (fail "Foreign project requested a local Git dependency from outside its directory"))
    (when-not (bb.fs/exists? worktree)
      (try
        (bb.fs/create-dirs path)
        (catch Exception _ex
          (fail (format "Unable to create directories for Git dependency at: %s"
                        path))))
      (when-not (bb.fs/exists? repo)
        (let [p (P.git/exec ["clone"
                             "--quiet"
                             "--no-tags"
                             "--bare"
                             (if (and scheme-file?
                                      (bb.fs/relative? url))
                               (format "%s/%s"
                                       dir-project-parent
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
          (fail "Unable to fetch Git commit")))
      (let [rev (P.git/resolve sha
                               {:dir repo})]
        (when-not (= sha
                     rev)
          (fail "Git commit must be specified as a full SHA"))
        (let [p (P.git/exec ["worktree"
                             "add"
                             worktree
                             sha]
                            {:dir repo})]
          (when-not (P.process/success? p)
            (fail "Unable to create worktree for Git repository under requested commit")))))
    [(not scheme-file?)
     worktree]))


;;;;;;;;; Fetching actors


(defn fetch

  "Used in [[convex.shell.dep/fetch]] for fetching Git dependencies."

  [env project-child dep-parent actor-sym actor-path]

  (let [git-sha          ($.std/nth dep-parent
                                    2)
        git-url          ($.std/nth dep-parent
                                    1)
        [foreign-parent?
         git-worktree]   (worktree env
                                   (str ($.std/get project-child
                                                   ($.cell/* :dir)))
                                   (str git-url)
                                   (str git-sha))]
    (-> env
        (update-in [:convex.shell.dep/dep->project
                    dep-parent]
                   #(or %
                        ((env :convex.shell.dep/read-project) (env :convex.shell/ctx)
                                                              dep-parent
                                                              git-worktree)))
        (assoc :convex.shell.dep/foreign?
               foreign-parent?)
        ((env :convex.shell.dep/jump) dep-parent
                                      actor-sym
                                      actor-path)
        (assoc :convex.shell.dep/foreign?
               (env :convex.shell.dep/foreign?)))))
