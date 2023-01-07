(ns convex.shell.dep.relative

  (:import (convex.core.exceptions ParseException)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [clojure.string   :as string]
            [convex.cell      :as $.cell]
            [convex.read      :as $.read]
            [convex.shell.err :as $.shell.err]
            [convex.shell.kw  :as $.shell.kw]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn path

  [project-child dep-parent actor-path]

  (format "%s/%s/%s.cvx"
          (get project-child
               $.shell.kw/dir)
          (second dep-parent)
          (string/join "/"
                       (rest actor-path))))



(defn read

  [env path]

  (let [fail (fn [message]
               (throw (ex-info ""
                               {:convex.shell/exception
                                (-> ($.shell.err/reader-file ($.cell/string path)
                                                             (some-> message
                                                                     ($.cell/string)))
                                    
                                    ($.std/assoc $.shell.kw/ancestry
                                                 (env :convex.shell.dep/ancestry)))})))]
    (try
      ;;
      (let [src        ($.read/file path)
            first-form (first src)]
        (-> (if ($.std/map? first-form)
              ($.std/assoc first-form
                           $.shell.kw/src
                           (or ($.std/next src)
                               ($.cell/* ())))
              ($.cell/* {:src ~src}))
            ($.std/assoc $.shell.kw/path
                         ($.cell/string path))))
      ;;
      (catch NoSuchFileException _ex
        (fail "File not found"))
      ;;
      (catch ParseException ex
        (fail (.getMessage ex)))
      ;;
      (catch Throwable _ex
        (fail nil)))))



(defn validate-required

  [required ancestry]

  (let [fail (fn [message]
               (throw (ex-info ""
                               {:convex.shell/exception
                                (-> ($.cell/error ($.cell/code-std* :ARGUMENT)
                                                  ($.cell/string message))
                                    ($.std/assoc $.shell.kw/ancestry
                                                 ancestry))})))]
    (when-not ($.std/vector? required)
      (fail "Required paths must be in a vector"))
    (when-not (even? ($.std/count required))
      (fail "Required paths must consist of bindings like `let`"))
    (doseq [[actor-sym
             actor-path] (partition 2
                                    required)]
      (when-not ($.std/symbol? actor-sym)
        (fail (format "Actor alias must be a symbol, not:  %s"
                      actor-sym)))
      (when-not ($.std/vector? actor-path)
        (fail (format "Actor path for `%s` must be a vector"
                      actor-sym)))
      (when ($.std/empty? actor-path)
        (fail (format "Actor path for `%s` is empty"
                      actor-sym)))))
  required)


;;;;;;;;; Fetching actors


(defn content

  [env project-child dep-parent _actor-sym actor-path]

  (assoc env
         :convex.shell.dep/content
         (read env
               (path project-child
                     dep-parent
                     actor-path))))



(defn fetch

  [env project-child dep-parent actor-sym actor-path]

  (let [src-path        (path project-child
                              dep-parent
                              actor-path)
        content         (read env
                              src-path)
        src             ($.std/get content
                                   $.shell.kw/src)
        hash            ($.cell/hash content)
        ancestry        (env :convex.shell.dep/ancestry)
        required-parent (some-> ($.std/get content
                                           $.shell.kw/deploy)
                                (validate-required ancestry))
        env-2           (-> env
                            (update-in [:convex.shell.dep/hash->ancestry
                                        hash]
                                       #(or %
                                            ancestry))
                            (assoc-in [:convex.shell.dep/hash->src
                                       hash]
                                      src)
                            (update-in [:convex.shell.dep/hash->binding+
                                        (env :convex.shell.dep/hash)]
                                       (fnil conj
                                             [])
                                       [actor-sym
                                        hash]))]
    (when (contains? (env-2 :convex.shell.dep.hash/pending+)
                     hash)
      (throw (ex-info ""
                      {:convex.shell/exception
                       (-> ($.cell/error ($.cell/code-std* :STATE)
                                         ($.cell/string "Circular dependency"))
                           ($.std/assoc $.shell.kw/ancestry
                                        (env-2 :convex.shell.dep/ancestry)))})))
    (if required-parent
      (-> env-2
          (assoc :convex.shell.dep/hash     hash
                 :convex.shell.dep/required required-parent)
          (update :convex.shell.dep.hash/pending+
                  conj
                  hash)
          ((env-2 :convex.shell.dep/fetch))
          (merge (select-keys env
                              [:convex.shell.dep.hash/pending+
                               :convex.shell.dep/hash])))
      env-2)))
