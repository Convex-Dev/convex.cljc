(ns convex.shell.build

  "Altering and quering informations about the CVM context attached to an env.
  
   All CVX Shell libraries are pre-compiled in advance and a base context is defined in a top-level
   form as well. This significantly improves the start-up time of native images since all of those
   are precomputed at build time instead of run time (~4x improvement)."

  {:author "Adam Helinski"}

  (:import (java.io PushbackReader))
  (:require [clojure.edn      :as edn]
            [clojure.java.io  :as java.io]))


;;;;;;;;;;


(defn version+

  []

  (let [alias+ (-> "deps.edn"
                   (java.io/reader)
                   (PushbackReader.)
                   (edn/read)
                   (:aliases))]
    [(get-in alias+
             [:module/shell
              :convex.shell/version])
     (get-in alias+
             [:ext/convex-core
              :extra-deps
              'world.convex/convex-core
              :mvn/version])]))
