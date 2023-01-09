(ns convex.shell.req.time

  (:require [convex.cell       :as $.cell]
            [convex.clj        :as $.clj]
            [convex.cvm        :as $.cvm]
            [convex.shell.time :as $.shell.time]
            [convex.std        :as $.std]))


;;;;;;;;;; CVM time


(defn advance

  [ctx [millis]]
  
  (or (when-not ($.std/long? millis)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Interval must be a long representing milliseconds")))
      (let [millis-2 (min ($.clj/long millis)
                          (- Long/MAX_VALUE
                             ($.clj/long ($.cvm/time ctx))))]
        (-> ctx
            ($.cvm/time-advance millis-2)
            ($.cvm/result-set ($.cell/long millis-2))))))


;;;;;;;;;; Current time


(defn nano

  ;; High-resolution timer.

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long ($.shell.time/nano))))



(defn unix

  ;; Returns UNIX timestamp.

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long ($.shell.time/unix))))


;;;;;;;;;; Conversions


(defn iso->unix

  ;; Convers ISO string to Unix timestamp.

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

  ;; Convers Unix timestamp to ISO string.

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
