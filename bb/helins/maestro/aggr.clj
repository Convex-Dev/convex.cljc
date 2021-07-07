(ns helins.maestro.aggr
  
  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [alias]))


;;;;;;;;;;


(defn alias

  ""

  [ctx alias _config]

  (update ctx
          :maestro/require
          conj
          alias))



(defn env

  ""

  [acc _alias config]

  (update acc
          :maestro/env
          merge
          (:maestro/env config)))
