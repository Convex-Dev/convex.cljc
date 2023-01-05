(ns convex.shell.dep.relative

  (:import (convex.core.exceptions ParseException)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [read])
  (:require [clojure.string   :as string]
            [convex.cell      :as $.cell]
            [convex.read      :as $.read]
            [convex.shell.err :as $.shell.err]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn read

  [state path]

  (let [fail (fn [message]
               (throw (ex-info ""
                               {:convex.shell/exception (-> ($.shell.err/reader-file ($.cell/string path)
                                                                                     (some-> message
                                                                                             ($.cell/string)))
                                                            
                                                            ($.std/assoc ($.cell/* :ancestry)
                                                                         (state :convex.shell.dep/ancestry)))})))]
    (try
      ;;
      ($.read/file path)
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
                                    ($.std/assoc ($.cell/* :ancestry)
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


(defn fetch

  [env project-child dep-parent actor-sym actor-path]

  (let [ancestry        (-> (env :convex.shell.dep/ancestry)
                            ($.std/conj ($.cell/* [~(env :convex.shell/dep)
                                                   ~actor-path])))
        env-2           (assoc env
                               :convex.shell.dep/ancestry
                               ancestry)
        src-path        (format "%s/%s/%s.cvx"
                                (get project-child
                                     ($.cell/* :dir))
                                (second dep-parent)
                                (string/join "/"
                                             (rest actor-path)))
        src             (read env-2
                              src-path)
        hash            ($.cell/hash src)
        required-parent (some-> (get (first src)
                                     ($.cell/* :require))
                                (validate-required ancestry))
        env-3           (-> env-2
                            (update-in [:convex.shell.dep/hash->ancestry
                                        hash]
                                       #(or %
                                            ancestry))
                            (assoc-in [:convex.shell.dep/hash->src
                                       hash]
                                      (cond->
                                        src
                                        required-parent
                                        ($.std/next)))
                            (update-in [:convex.shell.dep/hash->binding+
                                        (env :convex.shell.dep/hash)]
                                       (fnil conj
                                             [])
                                       [actor-sym
                                        hash]))]
    (when (contains? (env-3 :convex.shell.dep.hash/pending+)
                     hash)
      (throw (ex-info ""
                      {:convex.shell/exception
                       (-> ($.cell/error ($.cell/code-std* :STATE)
                                         ($.cell/string "Circular dependency"))
                           ($.std/assoc ($.cell/* :ancestry)
                                        (env-3 :convex.shell.dep/ancestry)))})))
    (-> (if required-parent
          (-> env-3
              (assoc :convex.shell.dep/hash     hash
                     :convex.shell.dep/required required-parent)
              (update :convex.shell.dep.hash/pending+
                      conj
                      hash)
              ((env-3 :convex.shell.dep/fetch))
              (merge (select-keys env
                                  [:convex.shell.dep.hash/pending+
                                   :convex.shell.dep/hash])))
          env-3)
        (merge (select-keys env
                            [:convex.shell.dep/ancestry])))))
