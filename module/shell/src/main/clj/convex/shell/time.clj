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



  (defn instant->iso-string
  
    "Converts an `Instant` to an ISO 8601 string (UTC)."
  
    [instant]

    (.format formatter
             instant))



  (defn iso-string->instant

    "Converts an ISO 8601 string to an `Instant`.
    
     Returns nil if the string cannot be parsed."

    ^Instant

    [iso-string]

    (try
      (Instant/from (.parse formatter
                            iso-string))
      (catch DateTimeParseException _ex
        nil))))



(defn instant->unix

  "Converts an `Instant` into a Unix timestamp."

  [^Instant instant]

  (.toEpochMilli instant))



(defn unix

  "Current Unix timestamp in milliseconds."

  []

  (System/currentTimeMillis))



(defn unix->instant

  "Converts a Unix timestamp to an `Instant`."

  ^Instant

  [unix]

  (Instant/ofEpochMilli unix))



(defn unix->iso-string

  "Converts a Unix timestamp to an ISO 8601 string (UTC)."

  [unix]

  (-> unix
      (unix->instant)
      (instant->iso-string)))



(defn iso-string->unix

  "Converts an ISO 8601 string (UTC) to a Unix timestamp."

  [iso-string]

  (-> iso-string
      (iso-string->instant)
      (some-> (instant->unix))))
