(ns convex.shell.project

  "Convex Lisp projects may have a `project.cvx` file which contains useful
   data for the Shell.
  
   For the time being, this is only used for dependencies (see [[convex.shell.req]])."

  {:author "Adam Helinski"}

  (:import (convex.core.exceptions ParseException)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [babashka.fs      :as bb.fs]
            [clojure.string   :as string]
            [convex.cell      :as $.cell]
            [convex.read      :as $.read]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn read

  "Reads the `project.cvx` file found in `dir`."

  [dir fail]

  (let [dir-2    (-> dir
                     (bb.fs/expand-home)
                     (bb.fs/canonicalize)
                     (str))
        dir-cell ($.cell/string dir-2)
        path     (format "%s/project.cvx"
                         dir-2)
        fail-2   (fn [code message]
                   (fail code
                         ($.cell/* {:dir     ~dir-cell
                                    :message ~($.cell/string message)})))
        project  (try
                   (-> path
                       ($.read/file)
                       (first))
                   ;;
                   (catch NoSuchFileException _ex
                     (fail-2 ($.cell/* :STREAM)
                             "`project.cvx` not found"))
                   ;;
                   (catch ParseException ex
                     (fail-2 ($.cell/* :READER)
                             (format "Cannot read `project.cvx`: %s"
                                     (.getMessage ex)))))]
    (when-not ($.std/map? project)
      (fail-2 ($.cell/code-std* :ARGUMENT)
              "`project.cvx` must contain a map"))
    ($.std/assoc project
                 ($.cell/* :dir)
                 dir-cell)))
    

;;;;;;;;;;


(defn dep+

  "Validates and returns `:deps` found in a `project.cvx`."

  [project fail]

  (let [fail-2 (fn [message]
                 (fail ($.cell/code-std* :ARGUMENT)
                       ($.cell/string message)))]
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
                (let [dir (str ($.std/get project
                                          ($.cell/* :dir)))]
                  (when-not (string/starts-with? (str (bb.fs/canonicalize (format "%s/%s"
                                                                                  dir
                                                                                  path)))
                                                 dir)
                    (fail-2 (format "Relative dependency `%s` in `project.cvx` specifies a path outside of the project"
                                    sym))))))
            ;;
            (= resolution
               ($.cell/* :local))
            (do
              (when (< ($.std/count dep)
                       2)
                (fail-2 (format "Local dependency `%s` in `project.cvx` must specify a path"
                                sym)))
              (when (not-string? ($.std/nth dep
                                            1))
                (fail-2 (format "Local dependency `%s` in `project.cvx` must specify a path as a string"
                                sym))))
            ;;
            :else
            (fail-2 (format "Unknown resolution mechanism for `%s` dependency in `project.cvx`: %s"
                            sym
                            resolution)))))
      dep+)))
