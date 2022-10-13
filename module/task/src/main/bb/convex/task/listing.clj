(ns convex.task.listing

  "Generating `./module/README.md`."

  (:require [protosens.maestro.idiom.listing :as P.maestro.idiom.listing]))


;;;;;;;;;;


(defn- -pred

  ;; Used for creating lists.

  [type]

  (fn [_alias alias-data]
    (contains? (alias-data :convex/type)
               type)))


;;;


(defn main

  "Prints `./module/README.md`."

  []

  (P.maestro.idiom.listing/main
    {:maestro.idiom.listing/list+ [{:pred (-pred :lib)
                                    :txt  "### Libraries publicly available as [Git dependencies](https://clojure.org/guides/deps_and_cli#_using_git_libraries) for [Clojure CLI](https://clojure.org/guides/deps_and_cli):"}
                                   {:pred (-pred :app)
                                    :txt  "### Applications:"}
                                   {:pred (-pred :test)
                                    :txt  "### Testing and benchmarking:"}
                                   {:pred (-pred :learn)
                                    :txt  "### Learning materials:"}]
     :maestro.idiom.listing/table (fn [prepared-module+ basis]
                                    (P.maestro.idiom.listing/table prepared-module+
                                                                   basis)
                                    (println)
                                    (println "---"))}
                                    
    "module/README.md"))
