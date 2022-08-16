(ns user

  "Does a bit of setup when starting."

  (:require [clojure.java.classpath       :as classpath]
            [clojure.string               :as string]
            [clojure.tools.namespace.find :as namespace.find]))


;;;;;;;;;;


(try
  (require 'dev)
  (catch Throwable _ex))


;;;;;;;;;; Installing a default tap if requested


(defn tap

  [x]

  (println x)
  (flush))


(when (= (System/getenv "CONVEX_TAP")
         "true")
  (add-tap tap))


(doseq [nmspace (sort (filter (fn [nmspace]
                                (string/includes? (str nmspace)
                                                  "convex"))
                              (namespace.find/find-namespaces (classpath/classpath))))]
  (println "Requiring "
           nmspace)
  (try
    (require nmspace)
    (println "    OK")
    (catch Exception _ex
      (println "    FAIL")
      (println _ex))))
