(ns helins.maestro.run

  ""

  {:author "Adam Helinski"}

  (:require [babashka.tasks     :as bb.task]
            [helins.maestro     :as $]
            [helins.maestro.cmd :as $.cmd]))


;;;;;;;;;;


(defn clojure

  ""

  [ctx]

  (let [cmd-2 ($/cmd ctx)]
    (when (ctx :maestro/debug?)
      (println cmd-2))
    (bb.task/clojure {:extra-env (ctx :maestro/env)}
                     cmd-2)))



(defn dev

  ""


  ([]

   (dev ($/ctx)))


  ([ctx]

   (-> ctx
       $.cmd/dev
       clojure)))



(defn function

  ""


  ([]

   (function ($/ctx)))


  ([ctx]

   (-> ctx
       $.cmd/function
       clojure)))



(defn main-class

  ""


  ([]

   (main-class ($/ctx)))


  ([ctx]

   (-> ctx
       $.cmd/main-class
       clojure)))
