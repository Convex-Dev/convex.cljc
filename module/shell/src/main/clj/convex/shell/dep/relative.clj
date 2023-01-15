(ns convex.shell.dep.relative

  (:import (convex.core.exceptions ParseException)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [clojure.string        :as string]
            [convex.cell           :as $.cell]
            [convex.read           :as $.read]
            [convex.shell.dep.fail :as $.shell.dep.fail]
            [convex.std            :as $.std]))


;;;;;;;;;;


(defn path

  [project-child dep-parent actor-path]

  (format "%s/%s/%s.cvx"
          (get project-child
               ($.cell/* :dir))
          (second dep-parent)
          (string/join "/"
                       (rest actor-path))))



(defn read

  [env path]

  (let [fail (fn [code message]
               ($.shell.dep.fail/with-ancestry (env :convex.shell/ctx)
                                               code
                                               ($.cell/* {:filename ~($.cell/string path)
                                                          :message  ~(some-> message
                                                                             ($.cell/string))})
                                               (env :convex.shell.dep/ancestry)))]
    (try
      ;;
      (let [src        ($.read/file path)
            first-form (first src)]
        (-> (if ($.std/map? first-form)
              ($.std/assoc first-form
                           ($.cell/* :src)
                           (or ($.std/next src)
                               ($.cell/* ())))
              ($.cell/* {:src ~src}))
            ($.std/assoc ($.cell/* :filename)
                         ($.cell/string path))))
      ;;
      (catch NoSuchFileException _ex
        (fail ($.cell/* :FS)
              "File not found"))
      ;;
      (catch ParseException ex
        (fail ($.cell/* :READER)
              (.getMessage ex)))
      ;;
      (catch Throwable _ex
        (fail ($.cell/* :UNKNOWN)
              nil)))))



(defn validate-required

  [ctx required ancestry]

  (let [fail (fn [message]
               ($.shell.dep.fail/with-ancestry ctx
                                               ($.cell/code-std* :ARGUMENT)
                                               ($.cell/string message)
                                               ancestry))]
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
      (when-not (or ($.std/list? actor-path)
                    ($.std/vector? actor-path))
        (fail (format "Actor path for `%s` must be a list or a vector"
                      actor-sym)))
      (when ($.std/empty? actor-path)
        (fail (format "Actor path for `%s` is empty"
                      actor-sym)))))
  required)


;;;;;;;;; Fetching actors


(defn content

  [env project-child dep-parent actor-sym actor-path]

  (let [content (read env
                      (path project-child
                            dep-parent
                            actor-path))]
    (if (= actor-sym
           ($.cell/* _))
      env
      (update env
            :convex.shell.dep/content
            $.std/assoc
            actor-sym
            content))))



(defn fetch

  [env project-child dep-parent actor-sym actor-path]

  (let [src-path        (path project-child
                              dep-parent
                              actor-path)
        content         (read env
                              src-path)
        hash            ($.cell/hash content)
        ancestry        (env :convex.shell.dep/ancestry)
        required-parent ($.std/get content
                                   ($.cell/* :deploy))
        _               (when required-parent
                          (validate-required (env :convex.shell/ctx)
                                             required-parent
                                             ancestry))
        env-2           (-> env
                            (update-in [:convex.shell.dep/hash->ancestry
                                        hash]
                                       #(or %
                                            ancestry))
                            (assoc-in [:convex.shell.dep/hash->file
                                       hash]
                                      content)
                            (update-in [:convex.shell.dep/hash->binding+
                                        (env :convex.shell.dep/hash)]
                                       (fnil conj
                                             [])
                                       [actor-sym
                                        hash]))]
    (when (contains? (env-2 :convex.shell.dep.hash/pending+)
                     hash)
      ($.shell.dep.fail/with-ancestry (env-2 :convex.shell/ctx)
                                      ($.cell/* :SHELL.DEP)
                                      ($.cell/* "Circular dependency")
                                      (env-2 :convex.shell.dep/ancestry)))
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
