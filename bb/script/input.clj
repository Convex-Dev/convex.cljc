(ns script.input

  "Preparing `*in*` and `*command-line-args*`."

  {:author "Adam Helinski"}

  (:require [clojure.edn]
            [clojure.string]
            [helins.maestro  :as maestro]))


;;;;;;;;;;


(defn kw-module

  ""

  [module]

  (keyword (str "module."
                module)))



(defn module

  "Returns a map with command-line arguments under `:arg+` and module under `:module`."

  []

  (if-some [module (first *command-line-args*)]
    {:arg+   (rest *command-line-args*)
     :module (keyword module)}
    (do
      (println "Module must be provided")
      (System/exit 42))))


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
                   cli-alias))
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
                              cli-alias+
                              (some-> (:module hmap-2)
                                      vector))
                      (maestro/deps-edn)))))))



(defn prepare-module

  "Effectively `(prepare (module))`."

  []

  (prepare (module)))
