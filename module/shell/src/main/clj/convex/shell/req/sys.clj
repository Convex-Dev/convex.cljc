(ns convex.shell.req.sys

  (:require [convex.clj  :as $.clj]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn arch

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/string (System/getProperty "os.arch"))))



(defn env

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/map (map (fn [[k v]]
                                       [($.cell/string k)
                                        ($.cell/string v)])
                                     (System/getenv)))))



(defn env-var

  [ctx [env-var]]

  (or (when (nil? env-var)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide an env variable to read")))
      ($.cvm/result-set ctx
                        (some-> (System/getenv (str env-var))
                                ($.cell/string)))))



(defn exit

  [ctx [code]]

  (or (when-not ($.std/long? code)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Exit code must be a long between 0 and 255")))
      (System/exit ($.clj/long code))))



(defn home

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/string (System/getProperty "user.home"))))



(defn os

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/* [~($.cell/string (System/getProperty "os.name"))
                               ~($.cell/string (System/getProperty "os.version"))])))
