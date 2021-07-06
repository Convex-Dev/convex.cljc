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
          (assoc :deps-edn
                 (maestro/deps-edn))
          (as->
            hmap-2
            (assoc hmap-2
                   :alias+     (vec (concat (:alias+ hmap-2)
                                            alias-cli+))
                   :alias-cli+ alias-cli+
                   :arg+       arg+)))))))



(defn expand

  ""


  ([input]

   (expand (dissoc input
                   :alias+)
           (input :alias+)))


  ([input alias+]

   (maestro/walk (fn [acc alias config]
                        (-> acc
                            (maestro/aggr-alias :alias+
                                                alias
                                                config)
                            (maestro/aggr-env :env-extra
                                              alias
                                              config)))
                 input
                 alias+
                 (input :deps-edn))))



(defn require-test

  ""

  [input alias+]

  (let [deps-alias  (get-in input
                            [:deps-edn
                             :aliases])
        alias-main+ (input :alias+)]
    (-> input
        (dissoc :alias+)
        (expand (into []
                      (mapcat (fn [alias]
                                (get-in deps-alias
                                        [alias
                                         :maestro/test])))
                      alias+))
        (as->
          input-2
          (assoc input-2
                 :alias-test+
                 (input-2 :alias+)))
        (assoc :alias-main+
               alias-main+)
        (update :alias+
                concat
                alias-main+))))



(defn require-test-narrow

  ""

  [input]

  (require-test input
                (input :alias-cli+)))



(defn require-test-global

  ""

  [input]

  (require-test input
                (input :alias+)))
