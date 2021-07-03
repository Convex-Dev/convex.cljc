(ns script.input

  "Preparing `*in*` and `*command-line-args*`."

  {:author "Adam Helinski"}

  (:require [clojure.edn]
            [clojure.string]))


;;;;;;;;;;


(defn project

  "Returns a map with command-line arguments under `:arg+` and project directory under `:dir`."

  []

  (if-some [project (first *command-line-args*)]
    {:arg+ (rest *command-line-args*)
     :dir  (str "project/"
                project)}
    (do
      (println "Project name must be provided as argument.")
      (System/exit 42))))


;;;


(defn prepare

  "Given a map, handles command-line arguments under `:arg+` to extract possible Deps profiles to `:profile+`.
  
   Provides `:arg+` in case no argument is provided."


  ([]

   (prepare {:arg *command-line-args*}))


  ([hmap]

  (loop [arg+     (:arg+ hmap)
         profile+ []]
    (if-some [profile (when-some [arg (first arg+)]
                        (when (clojure.string/starts-with? arg
                                                           ":")
                          arg))]
      (recur (rest arg+)
             (conj profile+
                   profile))
      (-> hmap
          (cond->
            (.ready *in*)
            (merge (clojure.edn/read *in*)))
          (update :arg+
                  (fn [x]
                    (into x
                          arg+)))
          (update :profile+
                  (fn [x]
                    (into x
                          profile+))))))))



(defn prepare-project

  "Effectively `(prepare (project))`."

  []

  (prepare (project)))

