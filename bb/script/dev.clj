(ns script.dev
  
  "Miscellaneous helpers for dev."

  {:author "Adam Helinski"}

  (:require [babashka.fs  :as bb.fs]
            [script.input :as $.input]))


(declare templ-dir)


;;;;;;;;;;


(defn templ-del

  "In directory `./src/clj/dev` of project given as as CLI argument, deletes `dev.clj`."

  []

  (bb.fs/delete-if-exists (str (templ-dir)
                               "dev.clj")))



(defn templ-dir

  "Given a project as CLI argument, returns the directory where Clojure dev files are located."

  []
  
  (let [project (first *command-line-args*)]
    (when-not (and project
                   (bb.fs/directory? (str "project/"
                                          project)))
      (println "Must provide one directory from './project'")
      (System/exit 42))
    (format "project/%s/src/clj/dev/"
            project)))


(defn templ-copy

  "In directory designated by [[templ-dir]], copies `./templ.clj` to `./dev.clj` if the latter does not exist."

  []

  (let [dir      (templ-dir)
        path-dev (str dir
                      "dev.clj")]
    (if (bb.fs/exists? path-dev)
      (do
        (println (str "Cannot copy dev template, file already exists: "
                      path-dev))
        (System/exit 42))
      (bb.fs/copy (str dir
                       "templ.clj")
                  path-dev))))
