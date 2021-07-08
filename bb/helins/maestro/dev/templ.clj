(ns helins.maestro.dev.templ
  
  ""

  {:author "Adam Helinski"}

  (:require [babashka.fs :as bb.fs]))


;;;;;;;;;;


(defn copy

  ""

  [root]

  (let [path-dev (str root
                      "/dev.clj")]
    (if (bb.fs/exists? path-dev)
      (throw (ex-info "Cannot copy dev template, file already exists"
                      {::path path-dev}))
      (bb.fs/copy (str root
                       "/dev_templ.clj")
                  path-dev))))



(defn rm

  ""

  [root]

  (bb.fs/delete-if-exists (str root
                               "dev.clj")))
