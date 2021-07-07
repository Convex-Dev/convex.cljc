(ns helins.maestro.kaocha

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.fs        :as bb.fs]
            [helins.maestro     :as $]
            [helins.maestro.run :as $.run]))


;;;;;;;;;;


(defn test

  ""

  [ctx f-test-alias+]

  ($.run/clojure "M"
                 (-> ctx
                     (update :maestro/require
                             conj
                             :task/test)
                     $/walk
                     (as->
                       ctx-2
                       ($/require-test ctx-2
                                       (f-test-alias+ ctx-2))
                       (do
                         (when-not (bb.fs/exists? "private")
                           (bb.fs/create-dir "private"))
                         (spit "private/maestro_kaocha.edn"
                               (pr-str {:kaocha/source-paths ($/path+ ctx-2
                                                                      (ctx-2 :maestro/main+))
                                        :kaocha/test-paths   ($/path+ ctx-2
                                                                      (ctx-2 :maestro/test+))}))
                         ctx-2))
                     (update :maestro/arg+
                             (partial cons
                                      "-m kaocha.runner")))))


;;;;;;;;;;


(defn run

  ""


  ([]

   (run ($/ctx)))


  ([ctx]

   (test ctx
         :maestro/require)))



(defn run-narrow

  ""


  ([]

   (run-narrow ($/ctx)))


  ([ctx]

   (test ctx
         :maestro/cli+)))
