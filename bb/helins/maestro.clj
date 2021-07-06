(ns helins.maestro

  ""

  {:author "Adam Helinski"}

  (:require [clojure.edn])
  )


;;;;;;;;;;


(defn deps-edn

  ""

  ([]

   (deps-edn "deps.edn"))


  ([path]

    (-> path
        slurp
        clojure.edn/read-string)))






(defn walk

  ""


  ([f alias+]

   (walk f
         alias+
         (deps-edn)))


  ([f alias+ deps-edn]

   (walk f
         {}
         alias+
         deps-edn))


  ([f acc alias+ deps-edn]

   (reduce (fn [acc-2 alias]
             (if (contains? (acc-2 :maestro/seen+)
                            alias)
               acc-2
               (let [config (get-in deps-edn
                                    [:aliases
                                     alias])]
                 (f (walk f
                          (update acc-2
                                 :maestro/seen+
                                 (fnil conj
                                       #{})
                                 alias)
                          (:maestro/require config)
                          deps-edn)
                    alias
                    config))))
           acc
           alias+)))



(defn aggr-alias

  ""


  ([acc alias _config]

   (aggr-alias acc
               :maestro/require
               alias
               _config))

  ([acc kw alias _config]

   (update acc
           kw
           (fnil conj
                 [])
           alias)))



(defn aggr-env

  ""


  ([acc _alias config]

   (aggr-env acc
             :maestro/env
             _alias
             config))


  ([acc kw _alias config]

   (update acc
           kw
           merge
           (:maestro/env config))))


;;;;;;;;;;


(defn path+

  ""

  [deps-edn alias+]

  (into []
        (comp (map (partial get
                            (deps-edn :aliases)))
              (mapcat :extra-paths))
        alias+))
