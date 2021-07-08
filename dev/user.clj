(ns user

  "Does a bit of setup when starting.")


;;;;;;;;;;


(try
  (require 'dev)
  (catch Throwable _ex))


;;;;;;;;;; Installing a default tap if requested


(defn tap

  [x]

  (println x)
  (flush))


(when (= (System/getenv "CONVEX_TAP")
         "true")
  (add-tap tap))
