(ns helins.maestro.cp

  "Working with the classpath."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [print])
  (:require [babashka.fs         :as bb.fs]
            [clojure.string]
            [helins.maestro.run :as $.run]))


;;;;;;;;;; Helpers


(defn split

  ""

  [cp-string]

  (map clojure.string/trim-newline
       (clojure.string/split cp-string
                             (re-pattern ":"))))


;;;;;;;;;; Tasks


(defn print-stdin

  ""

  []

  (run! println
        (-> (slurp *in*)
            split
            sort)))



(defn rm

  "Deletes the computed classpath."

  []

  (bb.fs/delete-tree ".cpcache"))
