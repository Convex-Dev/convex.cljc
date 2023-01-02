(ns convex.shell.dep

  (:require [babashka.fs       :as bb.fs]
            [clojure.string    :as string]
            [convex.cell       :as $.cell]
            [convex.read       :as $.read]
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
    {:repo     repo
     :worktree worktree}))
