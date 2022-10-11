(ns convex.task.doc

  "Generates documentation and READMEs."

  (:require [babashka.fs       :as bb.fs]
            [clojure.java.io   :as java.io]
            [clojure.string    :as string]
            [protosens.maestro :as $.maestro]
            [protosens.git     :as $.git]
            [protosens.string  :as $.string]
            [selmer.parser     :as selmer.parser]))


;;;;;;;;;; Helpers


(defn latest-stable-tag

  []

  (some->> (not-empty (filter (fn [tag]
                                (string/starts-with? tag
                                                     "stable/"))
                              ($.git/tag+)))
           (reduce (fn [acc tag]
                     (if (pos? (compare tag
                                        acc))
                       tag
                       acc)))))


;;;;;;;;;; Tasks


(defn changelog+

  []

  (let [dir        (System/getProperty "user.dir")
        stable-tag (or (some->> (first *command-line-args*)
                                (format "`%s`"))
                       ($.maestro/fail "Missing stable tag as argument"))
        templ      (fn [path]
                     (spit path
                           (selmer.parser/render-file path
                                                      {:next-release stable-tag}
                                                      {:custom-resource-path dir})))]
    (templ "doc/changelog.md")
    (doseq [[alias
             data] (sort-by first
                            (:aliases ($.maestro/create-basis)))
            :let   [root (data :maestro/root)]
            :when  root
            :let   [path-changelog (str root
                                        "/doc/changelog.md")]]
      (when (bb.fs/exists? path-changelog)
        (println (format "%s -> %s"
                         alias
                         path-changelog))
        (templ path-changelog)))))



(defn module

  []

  (with-open [writer (java.io/writer "module/README.md")]
    (binding [*out* writer]
      (println "# Modules")
      (println)
      (println "Publicly available as [Git dependencies](https://clojure.org/guides/deps_and_cli#_using_git_libraries) for [Clojure CLI](https://clojure.org/guides/deps_and_cli):")
      (println)
      (println "| Module | Description |")
      (println "|---|---|")
      (doseq [[artifact
               doc
               root]    (sort-by first
                                 (keep (fn [data]
                                         (when-some [artifact (:maestro.module.expose/name data)]
                                           [artifact
                                            (data :maestro/doc)
                                            (data :maestro/root)]))
                                       (-> ($.maestro/create-basis)
                                           (:aliases)
                                           (vals))))]
        (println (format "| [`%s`](./%s) | %s |"
                         (name artifact)
                         (str (bb.fs/relativize "module"
                                                root))
                         (let [line (first (string/split-lines doc))]
                           (cond->
                             line
                             (string/ends-with? line
                                                ".")
                             ($.string/trunc-right 1)))))))))



(defn readme+

  []

  (let [basis      ($.maestro/create-basis)
        git-url    (basis :maestro.module.expose/url)
        stable-tag (latest-stable-tag)
        stable-sha ($.git/resolve stable-tag)]
    (when (and stable-tag
               (not stable-sha))
      ($.maestro/fail (str "Unable to resolve tag to a SHA: "
                           stable-tag)))
    (doseq [[alias
             data] (sort-by first
                            (basis :aliases))
            :let   [root (data :maestro/root)]
            :when  root
            :let   [artifact      (data :maestro.module.expose/name)
                    doc           (data :maestro/doc)
                    path-quickdoc (data :maestro.plugin.quickdoc.path/output)
                    path-readme   (str root
                                       "/doc/README.md")
                    platform+     (not-empty (data :maestro/platform+))]]
      (println (format "%s -> %s"
                       alias
                       path-readme))
      (with-open [writer (java.io/writer (str root
                                              "/README.md"))]
        (binding [*out* writer]
          (println (format "# `%s`%s %s"
                           root
                           (if path-quickdoc
                             (format " - [API](%s)"
                                     (bb.fs/relativize root
                                                       path-quickdoc))
                             "")
                           (if (bb.fs/exists? (str root
                                                   "/doc/changelog.md"))
                             (format " - [CHANGES](%s)"
                                     "doc/changelog.md")
                             "")))
          (println)
          (println ($.string/realign doc))
          (when artifact
            (println)
            (println "```clojure")
            (println ";; Add to dependencies in `deps.edn`:")
            (println ";;")
            (println artifact)
            (println (format "{:deps/root \"%s\""  root))
            (println (format " :git/sha   \"%s\""  (or (some-> stable-sha
                                                               ($.git/shorten-sha))
                                                       "...")))
            (println (format " :git/tag   \"%s\""  (or stable-tag
                                                       "...")))
            (println (format " :git/url   \"%s\"}" git-url))
            (println "```"))
          (when platform+
            (println)
            (println "```clojure")
            (println ";; Supported platforms:")
            (println ";;")
            (println (vec (sort platform+)))
            (println "```"))
          (println)
          (when (bb.fs/exists? path-readme)
            (println)
            (println "---")
            (println)
            (println (slurp path-readme))))))))

