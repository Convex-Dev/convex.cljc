(ns user

  "Does a bit of setup when starting."

  (:require [portal.api          :as portal]
            [protosens.namespace :as P.namespace]
            [protosens.symbol    :as P.symbol]))


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

  (P.namespace/require-cp-dir+ (fn [nmspace]
                                 (when (P.symbol/starts-with? nmspace
                                                              'convex.)
                                   [nmspace
                                    :as
                                    (P.symbol/replace-first nmspace
                                                            'convex
                                                            '$)]))))


(def required-ns+

  "Namespace required automatically."

  (req))
