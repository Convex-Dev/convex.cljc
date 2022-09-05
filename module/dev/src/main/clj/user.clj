(ns user

  "Does a bit of setup when starting."

  (:require [clojure.java.classpath       :as classpath]
            [clojure.string               :as string]
            [clojure.tools.namespace.find :as namespace.find]
            [portal.api                   :as portal]))


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



(defn req-cvx

  "Require Convex namespaces found on the classpath aliasing them in
   the usual way."

  []

  (doseq [nmspace (sort (filter (fn [nmspace]
                                  (string/starts-with? (str nmspace)
                                                       "convex"))
                                (namespace.find/find-namespaces (classpath/classpath))))
          :let    [as-alias (-> nmspace
                                (str)
                                (string/split #"\.")
                                (assoc 0
                                       "$")
                                (->> (string/join "."))
                                (symbol))]]
    (println (format "Requiring `%s` as `%s`"
                     nmspace
                     as-alias))
    (try
      (require [nmspace :as as-alias])
      (println "    OK")
      (catch Exception _ex
        (println "    FAIL")
        (println _ex)))))


;;;;;;;;;;


(req-cvx)
