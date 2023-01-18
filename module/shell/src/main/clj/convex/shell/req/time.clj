(ns convex.shell.req.time

  "Requests relating to time."

  {:author "Adam Helinski"}

  (:require [convex.cell       :as $.cell]
            [convex.clj        :as $.clj]
            [convex.cvm        :as $.cvm]
            [convex.shell.time :as $.shell.time]
            [convex.std        :as $.std]))


;;;;;;;;;; Private


(defn -millis

  ;; Ensures `millis` is a positive CVX Long.

  [ctx millis]

  (or (when-not ($.std/long? millis)
        [false
         ($.cvm/exception-set ctx
                              ($.cell/code-std* :ARGUMENT)
                              ($.cell/* "Interval must be a long representing milliseconds"))])
      (let [millis-2 ($.clj/long millis)]
        (or (when (neg? millis-2)
              [false
               ($.cvm/exception-set ctx
                                    ($.cell/code-std* :ARGUMENT)
                                    ($.cell/* "Interval must be >= 0"))])
            [true
             millis-2]))))


;;;;;;;;;; CVM time


(defn advance

  "Request for moving forward the CVM timestamp."

  [ctx [millis]]
  
  (let [[ok?
         x]  (-millis ctx
                      millis)]
    (or (when-not ok?
          x)
        (let [millis-2 (min x
                            (- Long/MAX_VALUE
                               ($.clj/long ($.cvm/time ctx))))]
          (-> ctx
              ($.cvm/time-advance millis-2)
              ($.cvm/result-set ($.cell/long millis-2)))))))


;;;;;;;;;; Current time


(defn nano

  "Request for returning the current time according to the JVM high-resolution
   timer."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long ($.shell.time/nano))))



(defn unix

  "Request for returning the current Unix timestamp of the machine."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long ($.shell.time/unix))))


;;;;;;;;;; Conversions


(defn iso->unix

  "Request for converting an ISO 8601 UTC string into a Unix timestamp."

  [ctx [iso-string]]

  (or (when-not ($.std/string? iso-string)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "ISO time to convert must be a string")))
      ($.cvm/result-set ctx
                        (-> iso-string
                            ($.clj/string)
                            ($.shell.time/iso->unix)
                            (some-> ($.cell/long))))))


(defn unix->iso

  "Opposite of [[iso->unix]]."

  [ctx [time-unix]]

  (or (when-not ($.std/long? time-unix)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Unix timestamp to convert must be a long")))
      ($.cvm/result-set ctx
                        (-> time-unix
                            ($.clj/long)
                            ($.shell.time/unix->iso)
                            ($.cell/string)))))


;;;;;;;;;; Miscellaneous


(defn sleep

  "Request for temporarily blocking execution."

  [ctx [millis]]

  (let [[ok?
         x]  (-millis ctx
                      millis)]
    (or (when-not ok?
          x)
        (do
          (Thread/sleep x)
          ctx))))
