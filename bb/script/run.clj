(ns script.run

  "Running dev mode, tests, etc."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.fs     :as bb.fs]
            [babashka.tasks  :as bb.task]
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
                                  [:test
                                   :dev]
                                  (get-in deps-edn
                                          [:aliases
                                           (first (input :alias-cli+))
                                           :maestro/dev])))
                 ($.input/expand deps-edn)
                 ($.input/require-test-global deps-edn)))))



(defn kaocha-edn

  ""

  [{:as   input
    :keys [deps-edn]}]

  (when-not (bb.fs/exists? "private")
    (bb.fs/create-dir "private"))
  (spit "private/maestro_kaocha.edn"
        (pr-str {:kaocha/source-paths (maestro/path+ deps-edn
                                                     (input :alias-main+))
                 :kaocha/test-paths   (maestro/path+ deps-edn
                                                     (input :alias-test+))}))
  input)



(defn test

  ""

  [f-require-test]

  (clojure "M"
           (-> ($.input/prepare)
               (update :alias+
                       conj
                       :test)
               $.input/expand
               f-require-test
               kaocha-edn
               (update :arg+
                       (fn [arg+]
                         (concat ["-m kaocha.runner"
                                  "--config-file kaocha.edn"]
                                 arg+))))))



(defn test-narrow

  ""

  []

  (test $.input/require-test-narrow))



(defn test-global

  ""

  []

  (test $.input/require-test-global))



(defn main

  ""

  []

  (clojure "M"
           (-> ($.input/prepare)
               $.input/expand)))


(defn exec

  ""

  []

  (clojure "X"
           (-> ($.input/prepare)
               $.input/expand)))
