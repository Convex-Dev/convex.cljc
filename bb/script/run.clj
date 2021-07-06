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
           (let [input ($.input/prepare)]
             (-> input
                 ($.input/expand (concat [:test
                                          :dev]
                                         (maestro/dev (input :deps-edn)
                                                      (last (input :alias-cli+)))
                                         (input :alias+)))
                 $.input/require-test-global))))



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



(defn run-M

  ""

  []

  (clojure "M"
           (-> ($.input/prepare)
               $.input/expand
               (as->
                 input
                 (update input
                         :arg+
                         (partial cons
                                  (str "-m "
                                       (or (maestro/main-class (input :deps-edn)
                                                               (last (input :alias-cli+)))
                                           (throw (ex-info "Alias needs `:maestro/main-class` pointing to the class containing the `-main` function"
                                                           {}))))))))))



(defn run-X

  ""

  []

  (clojure "X"
           (-> ($.input/prepare)
               $.input/expand)))



(defn -jar

  ""

  [dir alias f]

  (let [input       (-> ($.input/prepare)
                        $.input/expand)
        module-main (last (input :alias-cli+))]
    (clojure "X"
             (-> input
                 (assoc :alias+
                        [alias])
                 $.input/expand
                 (assoc :module-main
                        module-main)
                 (update :arg+
                         (partial cons
                                  (let [root (or (maestro/root (input :deps-edn)
                                                               module-main)
                                                 (throw (ex-info "Alias needs `:maestro/root` pointing to project root directory"
                                                                 {})))]
                                    (format ":jar build/%s/%s.jar :aliases '%s' :pom-file '\"%s/pom.xml\"'"
                                            dir
                                            root
                                            (input :alias+)
                                            root))))

                 f))))



(defn jar

  ""

  []

  (-jar ":jar"
        :jar
        identity))



(defn uberjar

  ""

  []

  (-jar "uberjar"
        :uberjar
        (fn [input]
          (if-some [main-class (maestro/main-class (input :deps-edn)
                                                   (input :module-main))]
            (update input
                    :arg+
                    (partial cons
                             (str ":main-class "
                                  main-class)))
            input))))
