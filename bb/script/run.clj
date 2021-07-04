(ns script.run

  "Running dev mode, tests, etc."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.tasks  :as bb.task]
            [clojure.edn]
            [clojure.string]
            [helins.maestro  :as maestro]
            [script.input    :as $.input]))


;;;;;;;;;;


(defn clojure

  "Starts dev mode, but no default profile is applied."

  [letter {:keys [alias+
                  arg+
                  debug?
                  env-extra]}]

  (let [command (format "clojure -%s%s %s"
                        letter
                        (clojure.string/join ""
                                             alias+)
                        (clojure.string/join " "
                                             arg+))]
    (when debug?
      (println command))
    (bb.task/shell {:extra-env env-extra}
                   command)))


;;;;;;;;;; Development


(defn dev

  ""

  []

  (clojure "M"
           (let [deps-edn (maestro/deps-edn)
                 input    ($.input/prepare)]
             (-> input
                 (update :alias+
                         (partial concat
                                  (cons :dev
                                        (get-in deps-edn
                                                [:aliases
                                                 (first (input :alias-cli+))
                                                 :maestro/dev]))))
                 $.input/expand))))




(defn test

  "Uses [[script.input/prepare-module]] and then run tests using [[-dev]]."

  []

  (clojure "M"
           (-> ($.input/prepare)
               (update :arg+
                       (fn [arg+]
                         (concat ["-m kaocha.runner"
                                  "--config-file kaocha.edn"]
                                 arg+)))
               #_(update-in [:config
                           :alias
                           :test]
                          (fn [config]
                            (merge-with (fn [a b]
                                          (cond
                                            (map? a)    (merge a
                                                               b)
                                            (vector? a) (into a
                                                              b)
                                            :else       b))
                                        config
                                        '{:extra-deps  {io.helins/mprop     {:mvn/version "0.0.1"}
                                                        lambdaisland/kaocha {:mvn/version "1.0.829"}}
                                          :extra-paths ["src/clj/test"]})))
               (update :profile+
                       conj
                       :test))))
