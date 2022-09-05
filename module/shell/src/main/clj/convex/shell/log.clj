(ns convex.shell.log

  "Handles logging done via Timbre.
  
   Note: SLF4J from the core Java libraries is being redirected to Timbre."

  (:import (java.time ZoneId
                      ZoneOffset)
           (java.time.format DateTimeFormatter)
           (java.util Date))
  (:require [convex.cell     :as $.cell]
            [convex.std      :as $.std]
            [convex.write    :as $.write]
            [taoensso.timbre :as log]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Helpers


(let [^DateTimeFormatter formatter (.withZone DateTimeFormatter/ISO_LOCAL_DATE_TIME
                                              (ZoneId/from ZoneOffset/UTC))]
  (defn instant->iso-string
  
    "Converts an `Instant` to an ISO 8601 string (UTC)."
  
    [instant]

    (.format formatter
             instant)))


;;;;;;;;;;


;; For the time being, only handle above `:error`. 
;;
(log/swap-config!
  (fn [config]
    (-> config
        (assoc :min-level
               :error
               ;;
               :appenders
               {:cvx {:enabled? true
                      :fn       (fn [entry]
                                  (binding [*out* *err*]
                                    (-> ($.cell/* [~($.cell/string (instant->iso-string (.toInstant ^Date (entry :instant))))
                                                    ~($.cell/keyword (name (entry :level)))
                                                    ~($.cell/* [~($.cell/symbol (entry :?ns-str))
                                                                ~($.cell/long (entry :?line))])
                                                    ~($.cell/vector (keep (fn [x]
                                                                            (let [x-2 ($.cell/any x)]
                                                                              (when ($.std/cell? x-2)
                                                                                x-2)))
                                                                          (entry :vargs)))])
                                        ($.write/string)
                                        (str)
                                        (println))))}}))))
