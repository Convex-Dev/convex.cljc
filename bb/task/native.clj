(ns task.native

  "Building native iamges."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [agent])
  (:require [babashka.tasks              :as bb.task]
            [cheshire.core               :as cheshire]
            [clojure.edn                 :as edn]
            [clojure.java.io]
            [clojure.string]
            [protosens.maestro.required  :as maestro.required]))


;;;;;;;;;;


(defn agent
  
  "Starts the native-image-agent for tracing and output reflection configuration."

  []

  (apply bb.task/shell
         "java"
         "-agentlib:native-image-agent=config-output-dir=./private/agent"
         *command-line-args*))


(defn reflect-config

  "Copies './private/agent/reflect-config.json' to the folder given as CLI arg after doing some preparation."

  ;; Inspired by https://github.com/borkdude/refl/blob/4eefd89b1f70407ac3e50c84ce2a6ceae3babad3/script/gen-reflect-config.clj#L64-L67
  ;;
  ;; Without this, building the native image will fail.

  []

  (cheshire/generate-stream (mapv (fn [hmap]
                                    (if (= (get hmap
                                                "name")
                                           "java.lang.reflect.Method")
                                      {"name"    "java.lang.reflect.AccessibleObject"
                                       "methods" [{"name"           "canAccess"
                                                   "parameterTypes" ["java.lang.Object"]}]}
                                      hmap))
                                  (cheshire/parse-stream (clojure.java.io/reader "./private/agent/reflect-config.json")))
                            (clojure.java.io/writer (str (or (first *command-line-args*)
                                                             (throw (ex-info "Path to project root missing"
                                                                             {})))
                                                         "reflect-config.json"))
                            {:pretty true}))


(defn image

  "Builds a native image."

  []

  (apply bb.task/shell
         "native-image"
         "-jar"
         (-> (maestro.required/create-basis)
             (get-in [:aliases
                      (edn/read-string (first *command-line-args*))
                      :maestro.build.path/output]))
         ;;"--initialize-at-build-time"
         "--no-fallback"
         "-H:+ReportExceptionStackTraces"
         (rest *command-line-args*)))
