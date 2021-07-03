(ns script.cp

  "Working with the classpath."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [print])
  (:require [babashka.fs     :as bb.fs]
            [babashka.tasks  :as bb.task]
            [clojure.string]
            [script.input    :as $.input]))


;;;;;;;;;;


(defn delete

  "Deletes a project's computed classpath."

  []

  (bb.fs/delete-tree (format "%s/.cpcache"
                             (:dir ($.input/project)))))



(defn string

  "Uses Clojure Deps to compute the classpath.
  
   Profiles can be given as CLI arguments."

  []

  (-> (bb.task/shell (assoc ($.input/project)
                            :out
                            :string)
                     (str "clojure -Spath "
                          (when-some [profile+ (not-empty (rest *command-line-args*))]
                            (str "-A"
                                 (clojure.string/join ""
                                                      profile+)))))
      :out))



(defn print

  "Prints [[string]] into lines."

  []

  (run! println
        (sort (map clojure.string/trim-newline
                   (clojure.string/split (string)
                                         (re-pattern ":"))))))
