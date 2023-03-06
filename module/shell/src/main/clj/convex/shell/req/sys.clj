(ns convex.shell.req.sys

  "Requests relating to basic system utilities."

  {:author "Adam Helinski"}

  (:import (java.lang ProcessHandle))
  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn arch

  "Request for returning the chip architecture as a string."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/string (System/getProperty "os.arch"))))



(defn cwd

  "Request for returning the current working directory (where the Shell started)."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/string (System/getProperty "user.dir"))))



(defn env

  "Request for returning the map of process environment variables."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/map (map (fn [[k v]]
                                       [($.cell/string k)
                                        ($.cell/string v)])
                                     (System/getenv)))))



(defn env-var

  "Request for returning the value for a single process environment variable."

  [ctx [env-var]]

  (or (when (nil? env-var)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide an env variable to read")))
      ($.cvm/result-set ctx
                        (some-> (System/getenv (str env-var))
                                ($.cell/string)))))



(defn exit

  "Request for terminating the process."

  [ctx [code]]

  (or (when-not ($.std/long? code)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Exit code must be a Long")))
      (let [code-2 ($.clj/long code)]
        (or (when-not (or (zero? code-2)
                          (<= 128
                              code-2
                              255))
              ($.cvm/exception-set ctx
                                   ($.cell/* :ARGUMENT)
                                   ($.cell/* "Exit code must be 0 or >= 128 and <= 255")))
            (System/exit code-2)))))



(defn home

  "Request for returning the home directory."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/string (System/getProperty "user.home"))))



(defn n-cpu

  "Request for returning the number of available cores."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long (.availableProcessors (Runtime/getRuntime)))))



(defn os

  "Request for returning a tuple `[OS Version]`."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/* [~($.cell/string (System/getProperty "os.name"))
                               ~($.cell/string (System/getProperty "os.version"))])))



(defn pid

  "Request for returning the PID of this process."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/long (.pid (ProcessHandle/current)))))
