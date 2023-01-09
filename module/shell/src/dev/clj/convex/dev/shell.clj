(ns convex.dev.shell

  {:author "Adam Helinski"}

  (:import (java.io PushbackReader))
  (:require [clojure.edn     :as edn]
            [clojure.java.io :as java.io]
            [convex.cell     :as $.cell]))


;;;;;;;;;;


(defn version+

  []

  (let [alias+ (-> "deps.edn"
                   (java.io/reader)
                   (PushbackReader.)
                   (edn/read)
                   (:aliases))]
    ($.cell/* {version.shell  ~($.cell/string (get-in alias+
                                                      [:module/shell
                                                       :convex.shell/version]))
               version.convex ~($.cell/string (get-in alias+
                                                      [:ext/convex-core
                                                       :extra-deps
                                                       'world.convex/convex-core
                                                       :mvn/version]))})))


;;;;;;;;;;


(spit "module/shell/src/main/cvx/convex/shell/version.cvx"
      (str (version+)))
