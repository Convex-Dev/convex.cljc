(ns script.cp

  "Working with the classpath."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [print])
  (:require [babashka.fs     :as bb.fs]
            [babashka.tasks  :as bb.task]
            [clojure.string]
            [helins.maestro  :as maestro]))


;;;;;;;;;;


(defn delete

  "Deletes the computed classpath."

  []

  (bb.fs/delete-tree ".cpcache"))



(defn string

  "Uses Clojure Deps to compute the classpath.
  
   Aliases can be given as CLI arguments."

  []

  (-> (bb.task/shell {:out :string}
                     (str "clojure -Spath "
                          (str "-A"
                               (clojure.string/join ""
                                                    (let [deps-edn (maestro/deps-edn)]
                                                      (if-some [alias+ (seq *command-line-args*)]
                                                        (-> (maestro/walk maestro/aggr-alias
                                                                          (map (fn [^String alias]
                                                                                 (keyword (.substring alias
                                                                                                      1)))
                                                                               alias+)
                                                                          deps-edn)
                                                            :maestro/require)
                                                        (filter (fn [kw]
                                                                  (= (namespace kw)
                                                                     "module"))
                                                                (keys (deps-edn :aliases)))))))))
      :out))



(defn print

  "Prints [[string]] into lines."

  []

  (run! println
        (sort (map clojure.string/trim-newline
                   (clojure.string/split (string)
                                         (re-pattern ":"))))))
