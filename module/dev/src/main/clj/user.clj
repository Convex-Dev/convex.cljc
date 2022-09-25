(ns user

  "Does a bit of setup when starting."

  (:require [clojure.string         :as string]
            [portal.api             :as portal]
            [protosens.maestro.user :as maestro.user]))


;;;;;;;;;;


(let [d*portal (delay
                 (add-tap portal/submit)
                 nil)]
  (defn portal
  
    "Opens a new browser tab with Portal (data exploration UI).
     Anything sent to `tap>` will appear there."
  
    []
  
    @d*portal
    (portal/open {:app    false
                  :window "convex.cljc"})))



(defn req

  "Require all namespaces from this repository that are present on the classpath."

  []

  (maestro.user/require-filtered {:map-namespace  (fn [nmspace]
                                                    (when (string/includes? (str nmspace)
                                                                            "convex")
                                                      [nmspace
                                                       :as
                                                       (symbol (str "$."
                                                                    (second (string/split (str nmspace)
                                                                                          #"convex\."))))]))
                                  :require.before (fn [nmspace]
                                                    (println "Require"
                                                             nmspace))}))



(def required-ns+

  "Namespace required automatically."

  (req))
