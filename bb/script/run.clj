(ns script.run

  "Running dev mode, tests, etc."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.tasks  :as bb.task]
            [clojure.string]
            [script.input    :as $.input]))


;;;;;;;;;; Private


(defn- -dev

  "Starts dev mode, but no default profile is applied."

  [{:keys [arg+
           debug?
           dir
           env-extra
           profile+]}]

  (let [command (format "clojure -M%s %s"
                         (clojure.string/join ""
                                              profile+)
                         (clojure.string/join " "
                                              arg+))]
    (when debug?
      (println command))
    (bb.task/shell {:dir       dir
                    :extra-env (merge {"CONVEX_DEV" "true"}
                                      env-extra)}
                   command)))


;;;;;;;;;; Development


(defn dev

  "Starts dev mode, applying default profiles `:dev` and `:test`."

  []

  (-dev (-> ($.input/prepare-project)
            (update :profile+
                    (fn [profile+]
                      (concat [:test
                               :dev]
                              profile+))))))



(defn test

  "Uses [[script.input/prepare-project]] and then run tests using [[-dev]]."

  []

  (-dev (-> ($.input/prepare-project)
            (update :arg+
                    (fn [arg+]
                      (concat ["-m kaocha.runner"
                               "--config-file kaocha.edn"]
                              arg+)))
            (update :profile+
                    conj
                    :test))))
