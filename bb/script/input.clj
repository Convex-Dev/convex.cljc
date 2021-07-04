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
         alias-cli+ []]
    (if-some [cli-alias (when-some [arg (first arg+)]
                          (when (clojure.string/starts-with? arg
                                                             ":")
                            arg))]
      (recur (rest arg+)
             (conj alias-cli+
                   (keyword (.substring ^String cli-alias
                                        1))))
      (-> hmap
          (cond->
            (.ready *in*)
            (merge (clojure.edn/read *in*)))
          (as->
            hmap-2
            (assoc hmap-2
                   :alias+     (concat (:alias+ hmap-2)
                                       alias-cli+)
                   :alias-cli+ alias-cli+
                   :arg+       arg+)))))))



(defn expand

  ""


  ([input]

   (expand input
           (maestro/deps-edn)))


  ([input deps-edn]

   (maestro/walk (fn [acc alias config]
                        (-> acc
                            (maestro/aggr-alias :alias+
                                                alias
                                                config)
                            (maestro/aggr-env :env-extra
                                              alias
                                              config)))
                      (dissoc input
                              :alias+)
                      (input :alias+)
                      deps-edn)))
