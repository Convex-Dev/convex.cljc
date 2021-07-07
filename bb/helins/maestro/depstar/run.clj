(ns helins.maestro.depstar.run

  ""

  {:author "Adam Helinski"}

  (:require [helins.maestro         :as $]
            [helins.maestro.depstar :as $.depstar]
            [helins.maestro.run     :as $.run]))


;;;;;;;;;;


(defn jar

  ""


  ([]

   (jar ($/ctx)))


  ([ctx]

   (-> ctx
       $.depstar/jar
       $.run/clojure)))



(defn uberjar

  ""


  ([]

   (uberjar ($/ctx)))


  ([ctx]

   (-> ctx
       $.depstar/uberjar
       $.run/clojure)))
