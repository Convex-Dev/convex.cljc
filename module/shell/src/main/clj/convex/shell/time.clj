(ns convex.shell.time

  "Miscellaneous time utilities and conversions."

  {:author "Adam Helinski"}

  (:import (java.time Instant
                      ZoneId
                      ZoneOffset)
           (java.time.format DateTimeFormatter
                             DateTimeParseException)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;

(let [^DateTimeFormatter formatter (.withZone DateTimeFormatter/ISO_LOCAL_DATE_TIME
                                              (ZoneId/from ZoneOffset/UTC))]



  (defn instant->iso
  
    "Converts an `Instant` to an ISO 8601 string (UTC)."
  
    [instant]

    (.format formatter
             instant))



  (defn iso->instant

    "Converts an ISO 8601 string to an `Instant`.
    
     Returns nil if the string cannot be parsed."

    ^Instant

    [iso]

    (try
      (Instant/from (.parse formatter
                            iso))
      (catch DateTimeParseException _ex
        nil))))



(defn instant->unix

  "Converts an `Instant` into a Unix timestamp."

  [^Instant instant]

  (.toEpochMilli instant))



(defn nano

  "High-resolution timer."

  []

  (System/nanoTime))


(defn unix

  "Current Unix timestamp in milliseconds."

  []

  (System/currentTimeMillis))



(defn unix->instant

  "Converts a Unix timestamp to an `Instant`."

  ^Instant

  [unix]

  (Instant/ofEpochMilli unix))



(defn unix->iso

  "Converts a Unix timestamp to an ISO 8601 string (UTC)."

  [unix]

  (-> unix
      (unix->instant)
      (instant->iso)))



(defn iso->unix

  "Converts an ISO 8601 string (UTC) to a Unix timestamp."

  [iso]

  (-> iso
      (iso->instant)
      (some-> (instant->unix))))
