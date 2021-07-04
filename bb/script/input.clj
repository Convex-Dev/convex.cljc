(ns script.input

  "Preparing `*in*` and `*command-line-args*`."

  {:author "Adam Helinski"}

  (:require [clojure.edn]
            [clojure.string]
            [helins.maestro  :as maestro]))


;;;


(defn prepare

  "Given a map, handles command-line arguments under `:arg+` to extract possible Deps profiles to `:profile+`.
  
   Provides `:arg+` in case no argument is provided."


  ([]

   (prepare {:arg+ *command-line-args*}))


  ([hmap]

  (loop [arg+       (:arg+ hmap)
         cli-alias+ []]
    (if-some [cli-alias (when-some [arg (first arg+)]
                          (when (clojure.string/starts-with? arg
                                                             ":")
                            arg))]
      (recur (rest arg+)
             (conj cli-alias+
                   (keyword (.substring ^String cli-alias
                                        1))))
      (as-> hmap
            hmap-2

        (cond->
          hmap-2
          (.ready *in*)
          (merge (clojure.edn/read *in*)))

        (maestro/walk (fn [acc alias config]
                        (-> acc
                            (maestro/aggr-alias :alias+
                                                alias
                                                config)
                            (maestro/aggr-env :env-extra
                                              alias
                                              config)))
                      (dissoc hmap-2
                              :alias+)
                      (concat (:alias+ hmap-2)
                              cli-alias+)
                      (maestro/deps-edn))

        (assoc hmap-2
               :arg+
               arg+))))))
