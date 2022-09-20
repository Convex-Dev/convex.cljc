(ns build

  "Building jars and uberjars."

  (:import (java.nio.file Files)
           (java.nio.file.attribute FileAttribute))
  (:require [clojure.tools.build.api :as tools.build]
            [protosens.maestro.alias :as maestro.alias]
            [protosens.maestro.profile :as maestro.profile]
            [protosens.maestro.required :as maestro.required]))


;;;;;;;;;; Tasks


(defn clean

  [ctx]

  (let [path (ctx :maestro.build.path/output)]
    (println "Removing any previous output:"
             path)
    (tools.build/delete {:path path}))
  ctx)



(defn copy-src

  [ctx]

  (println "Copying source paths")
  (tools.build/copy-dir {:src-dirs   (ctx :maestro.build.path/src+)
                         :target-dir (ctx :maestro.build.path/class)})
  ctx)



(defmulti jar-finalize
  :maestro.build/type)


(defmethod jar-finalize

  :jar

  [{:as        ctx
    artifact   :maestro.build/artifact
    path-class :maestro.build.path/class
    path-jar   :maestro.build.path/output}]

  (println (format "Assemling jar to '%s'"
                   path-jar))
  (tools.build/write-pom {:basis     (ctx :maestro.build/basis)
                          :class-dir path-class
                          :lib       artifact
                          :src-dirs  (ctx :maestro.build.path/src+)
                          :version   (ctx :maestro.build/version)})
  (tools.build/copy-file {:src    (format "%s/META-INF/maven/%s/pom.xml"
                                          path-class
                                          artifact)
                          :target (str (ctx :maestro/dir)
                                       "/pom.xml")})
  (tools.build/jar {:class-dir path-class
                    :jar-file  path-jar})
  ctx)



(defmethod jar-finalize

  :uberjar

  [{:as          ctx
    basis        :maestro.build/basis
    path-class   :maestro.build.path/class
    path-uberjar :maestro.build.path/output}]

  (println "Compiling" (ctx :maestro.build/alias))
  (tools.build/compile-clj {:basis        basis
                            :class-dir    path-class
                            :compile-opts (ctx :maestro.uberjar/compiler)
                            :src-dirs     (ctx :maestro.build.path/src+)})
  (println (format "Assembling uberjar to '%s'"
                   path-uberjar))
  (tools.build/uber {:basis     basis
                     :class-dir path-class
                     :exclude   ["(?i)^META-INF/license/.*"
                                 "^license/.*"]
                     :main      (ctx :maestro.uberjar/main)
                     :uber-file path-uberjar})
  ctx)



(defn main

  [arg+]

  (let [root-alias      (arg+ :maestro.build/alias)
        basis-maestro   (-> (maestro.required/create-basis)
                            (maestro.alias/append+ [root-alias])
                            (maestro.profile/append+ '[release])
                            (maestro.required/search))
        required-alias+ (basis-maestro :maestro/require)
        alias+          (basis-maestro :aliases)
        alias-data      (alias+ root-alias)
        [artifact
         version-map]   (-> alias+
                            (get-in [(keyword "release"
                                              (name root-alias))
                                    :extra-deps])
                            (first))
        dir-tmp         (str (Files/createTempDirectory "convex-cljc-build-"
                                                        (make-array FileAttribute
                                                                    0)))
        path-class      (str dir-tmp
                             "/classes")
        path-src+       (maestro.alias/extra-path+ basis-maestro
                                                   required-alias+)]
    (println "Using temporary directory for building:"
             dir-tmp)
    (-> (merge alias-data
               {:maestro.build/artifact    artifact
                :maestro.build/basis       (tools.build/create-basis {:aliases required-alias+
                                                                      :project "deps.edn"})
                :maestro.build/version     (get version-map
                                                :mvn/version)
                :maestro.build.path/class  path-class
                :maestro.build.path/src+   path-src+
                :maestro.build.path/target dir-tmp}
               arg+)
        (clean)
        (copy-src)
        (jar-finalize))))
