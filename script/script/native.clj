(ns script.native

  "Building native iamges."

  {:author "Adam Helinski"}

  (:require [babashka.tasks    :as bb.task]
            [clojure.string]))


;;;;;;;;;;


(defn image

  "Builds a native image."

  []

  (let [path (or (first *command-line-args*)
                 (throw (ex-info "Path to directly-linked uberjar required"
                                 {})))]
    (apply bb.task/shell
           "native-image"
           "-jar"
           path
           "--initialize-at-build-time"
           "--no-fallback"
           "--no-server"
           "--initialize-at-run-time=com.barbarysoftware.jna.CarbonAPI"
           "-H:+ReportExceptionStackTraces"
           (rest *command-line-args*))))
