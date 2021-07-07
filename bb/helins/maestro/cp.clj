(ns helins.maestro.cp

  "Working with the classpath."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [print])
  (:require [babashka.fs     :as bb.fs]
            [clojure.string]
            [helins.maestro  :as $]))


;;;;;;;;;;


(defn delete

  "Deletes the computed classpath."

  []

  (bb.fs/delete-tree ".cpcache"))


;;;;;;;;;;


(defn string

  "Uses Clojure Deps to compute the classpath.
  
   Aliases can be given as CLI arguments."

  [ctx]

  (with-out-str (-> ctx
                    (update :maestro/arg+
                            (partial cons
                                     "-Spath"))
                    (assoc :maestro/exec-letter
                           "A")
                    $/clojure)))



(defn split

  ""

  [cp-string]

  (map clojure.string/trim-newline
       (clojure.string/split cp-string
                             (re-pattern ":"))))
