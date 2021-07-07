(ns helins.maestro.kaocha.run

  ""

  {:author "Adam Helinski"}

  (:require [helins.maestro        :as $]
            [helins.maestro.kaocha :as $.kaocha]
            [helins.maestro.run    :as $.run]))


;;;;;;;;;;


(defn broad

  ""


  ([]

   (broad ($/ctx)))


  ([ctx]

   (-> ctx
       $.kaocha/broad
       $.run/clojure)))



(defn narrow

  ""


  ([]

   (narrow ($/ctx)))


  ([ctx]

   (-> ctx
       $.kaocha/narrow
       $.run/clojure)))
