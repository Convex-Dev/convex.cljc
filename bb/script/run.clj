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

  ""

  [letter ctx]

  (let [command (maestro/clojure letter
                                 ctx)]
    (when (ctx :maestro/debug?)
      (println command))
    (bb.task/clojure {:extra-env (ctx :maestro/env)}
                     command)))


;;;;;;;;;; Development


(defn dev

  ""

  []

  (clojure "M"
           (-> (maestro/ctx)
               (as->
                 ctx
                 (maestro/walk (dissoc ctx
                                       :maestro/require)
                               (concat [:task/test
                                        :task/dev]
                                       (maestro/dev ctx)
                                       (ctx :maestro/require))))
               maestro/require-test)))



(defn kaocha-edn

  ""

  [ctx]

  (when-not (bb.fs/exists? "private")
    (bb.fs/create-dir "private"))
  (spit "private/maestro_kaocha.edn"
        (pr-str {:kaocha/source-paths (maestro/path+ ctx
                                                     (ctx :maestro/main+))
                 :kaocha/test-paths   (maestro/path+ ctx
                                                     (ctx :maestro/test+))}))
  ctx)



(defn test

  ""

  [f-test-alias+]

  (clojure "M"
           (-> (maestro/ctx)
               (update :maestro/require
                       conj
                       :task/test)
               maestro/walk
               (as->
                 ctx
                 (maestro/require-test (f-test-alias+ ctx)))
               kaocha-edn
               (update :maestro/arg+
                       (fn [arg+]
                         (concat ["-m kaocha.runner"
                                  "--config-file kaocha.edn"]
                                 arg+))))))



(defn test-narrow

  ""

  []

  (test :maestro/cli+))



(defn test-global

  ""

  []

  (test :maestro/require))



(defn run-M

  ""

  []

  (clojure "M"
           (-> (maestro/ctx)
               maestro/walk
               (as->
                 ctx
                 (update ctx
                         :maestro/arg+
                         (partial cons
                                  (str "-m "
                                       (or (maestro/main-class ctx)
                                           (throw (ex-info "Alias needs `:maestro/main-class` pointing to the class containing the `-main` function"
                                                           {}))))))))))



(defn run-X

  ""

  []

  (clojure "X"
           (-> (maestro/ctx)
               maestro/walk)))



(defn -jar

  ""

  [dir alias f]

  (clojure "X"
           (let [ctx        (-> (maestro/ctx)
                                 maestro/walk)
                 alias-main (last (ctx :maestro/cli+))]
             (-> ctx
                 (assoc :maestro/require
                        [alias])
                 maestro/walk
                 (assoc :maestro/main
                        alias-main)
                 (as->
                   ctx-2
                   (update ctx-2
                           :maestro/arg+
                           (partial cons
                                    (let [root (or (maestro/root ctx-2
                                                                 alias-main)
                                                   (throw (ex-info "Alias needs `:maestro/root` pointing to project root directory"
                                                                   {})))]
                                      (format ":jar build/%s/%s.jar :aliases '%s' :pom-file '\"%s/pom.xml\"'"
                                              dir
                                              root
                                              (ctx-2 :maestro/require)
                                              root)))))
                 f))))



(defn jar

  ""

  []

  (-jar "jar"
        :task/jar
        identity))



(defn uberjar

  ""

  []

  (-jar "uberjar"
        :task/uberjar
        (fn [ctx]
          (if-some [main-class (maestro/main-class ctx)]
            (update ctx
                    :maestro/arg+
                    (partial cons
                             (str ":main-class "
                                  main-class)))
            ctx))))
