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

  (format "%s/src/clj/dev/"
          (:dir ($.input/project))))



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
