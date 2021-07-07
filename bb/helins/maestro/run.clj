(ns helins.maestro.run

  ""

  {:author "Adam Helinski"}

  (:require [babashka.tasks :as bb.task]
            [helins.maestro :as $]))


;;;;;;;;;;


(defn clojure

  ""

  [letter ctx]

  (let [command ($/clojure letter
                           ctx)]
    (when (ctx :maestro/debug?)
      (println command))
    (bb.task/clojure {:extra-env (ctx :maestro/env)}
                     command)))



(defn clojure-M

  ""

  
  ([]

   (clojure-M ($/ctx)))


  ([ctx]

   (clojure "M"
            (-> ctx
                $/walk
                (as->
                  ctx-2
                  (update ctx-2
                          :maestro/arg+
                          (partial cons
                                   (str "-m "
                                        (or ($/main-class ctx-2)
                                            (throw (ex-info "Alias needs `:maestro/main-class` pointing to the class containing the `-main` function"
                                                            {})))))))))))



(defn clojure-X

  ""


  ([]

   (clojure-X ($/ctx)))


  ([ctx]

   (clojure "X"
            ($/walk ctx))))



(defn dev

  ""

  
  ([]

   (dev ($/ctx)))


  ([ctx]

   (clojure "M"
            (-> ctx
                (dissoc :maestro/require)
                ($/walk (concat [:task/test
                                 :task/dev]
                                ($/dev ctx)
                                (ctx :maestro/require)))
                $/require-test))))
