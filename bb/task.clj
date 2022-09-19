(ns task

  "Implementations of main BB tasks."

  (:require [babashka.fs                :as bb.fs]
            [babashka.tasks             :as bb.task]
            [clojure.edn                :as edn]
            [protosens.maestro.alias    :as maestro.alias]
            [protosens.maestro.profile  :as maestro.profile]
            [protosens.maestro.required :as maestro.required]))


;;;;;;;;;; Generic helpers


(defn- -cp
  
  [aliases]

  (-> (bb.task/shell {:out :string}
                     (format "clojure -A%s -Spath"
                             (maestro.alias/stringify+ aliases)))
      (deref)
      (:out)))


(defn- -tool
  

  ([alias+ sym arg+]

   (-tool alias+
          sym
          arg+
          nil))


  ([alias+ sym arg+ opt+]

   (bb.task/clojure (or opt+
                        {})
                    (str "-T"
                         (maestro.alias/stringify+ alias+))
                    sym
                    arg+)))


;;;;;;;;;; Alias resolution


(defn- -prepare-kaocha
        
  [basis]

  (when-not (bb.fs/exists? "./private")
    (bb.fs/create-dir "./private"))
  (spit "./private/maestro_kaocha.edn"
        (-> (reduce merge
                    {}
                    (keep :salus/kaocha
                          (vals (select-keys (basis :aliases)
                                             (basis :maestro/require)))))
            (assoc :kaocha/source-paths (maestro.alias/extra-path+ basis
                                                                   (maestro.required/not-by-profile+ basis
                                                                                                    '[test]))
                   :kaocha/test-paths   (maestro.alias/extra-path+ basis
                                                                   (maestro.required/by-profile+ basis
                                                                                                 '[test])))))
  basis)


(defn aliases-dev
        
  []

  (-> (maestro.required/create-basis)
      (maestro.required/cli-arg)
      (maestro.alias/append+ [:task/dev])
      (maestro.profile/append+ '[dev
                                 test])
      (maestro.required/search)
      (-prepare-kaocha)
      (maestro.required/print)))


(defn aliases-test
        
  [direct?]

  (-> (maestro.required/create-basis)
      (maestro.required/cli-arg)
      (maestro.alias/append+ [:task/test])
      (maestro.profile/append+ [(with-meta 'test
                                           {:direct? direct?})])
      (maestro.required/search)
      (-prepare-kaocha)
      :maestro/require
      (maestro.alias/stringify+)
      print))


;;;;;;;;;; Artifact-related tasks


(defn build
        
  [arg+]

  (-tool (-> (maestro.required/create-basis)
             (maestro.alias/append+ [:module/build])
             maestro.required/search
             :maestro/require)
         'build/main
         (assoc arg+
                :maestro.build/alias
                (edn/read-string (first *command-line-args*)))))


(defn- -deploy

  [installer str-alias env]

  (let [alias-data (-> (maestro.required/create-basis)
                       (get-in [:aliases
                                (edn/read-string str-alias)]))]
    (-tool [:ext/deps-deploy]
           'deps-deploy.deps-deploy/deploy
           {:artifact  (alias-data :maestro.build.path/output)
            :installer installer
            :pom-file  (format "%s/pom.xml"
                               (alias-data :maestro/dir))}
           {:extra-env env})))


(defn deploy

  []

  (let [[username
         path-token
         str-alias]  *command-line-args*]
    (-deploy :remote
             str-alias
             {"CLOJARS_USERNAME" username
              "CLOJARS_PASSWORD" (slurp path-token)})))


(defn install

  []

  (-deploy :local
           (first *command-line-args*)
           nil))


;;;;;;;;;; Linting


(defn lint
        
  []

  (apply bb.task/shell
         "clj-kondo --parallel --lint"
         (mapcat :extra-paths
                  (-> (maestro.required/create-basis)
                      :aliases
                      vals))))


(defn lint-import
        
  []

  (let [cp (-> (maestro.required/create-basis) :aliases keys -cp)]
    (bb.task/shell "clj-kondo --parallel --copy-configs --lint" cp "--dependencies")))
