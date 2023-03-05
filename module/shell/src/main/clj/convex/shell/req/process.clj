(ns convex.shell.req.process

  (:import (java.io BufferedReader
                    InputStreamReader
                    OutputStreamWriter))
  (:require [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.shell.async :as $.shell.async]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]
            [protosens.process  :as P.process]
            [promesa.core       :as promesa]))


;;;;;;;;;; Private


(defn- -env

  [m]

  (into {}
        (map (fn [[k v]]
               [(str k)
                (str v)]))
        m))


;;;;;;;;;; Requests


(defn run

  [ctx [command dir env env-extra]]

  (or (when-not ($.std/vector? command)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Command must be specified as a Vector")))
      (when-not (>= ($.std/count command)
                    1)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Command must specify at least which program to run")))
      (when-not (or (nil? dir)
                    ($.std/string? dir))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "When provided, directory must be a String")))
      (when-not (or (nil? env)
                    ($.std/map? env))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "When provided, environment must be a Map")))
      (when-not (or (nil? env-extra)
                    ($.std/map? env-extra))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "When provided, extra environment must be a Map")))
      (try
        (let [exit (promesa/deferred)
              p    (P.process/run (map (fn [x]
                                         (if (nil? x)
                                           "nil"
                                           (str x)))
                                       command)
                                  {:dir       (some-> dir
                                                      ($.clj/string))
                                   :env       (some-> env
                                                      -env)
                                   :exit-fn   (fn [p-2]
                                                (promesa/resolve! exit
                                                                  ($.shell.async/success ($.cell/long (:exit p-2)))))
                                   :extra-env (some-> env-extra
                                                      -env)})]
          ($.cvm/result-set ctx
                            ($.cell/* {:err  ~(-> p
                                                  (:err)
                                                  (InputStreamReader.)
                                                  (BufferedReader.)
                                                  ($.shell.resrc/create))
                                       :exit ~($.shell.resrc/create exit)
                                       :in   ~(-> p
                                                  (:in)
                                                  (OutputStreamWriter.)
                                                  ($.shell.resrc/create))
                                       :out  ~(-> p
                                                  (:out)
                                                  (InputStreamReader.)
                                                  (BufferedReader.)
                                                  ($.shell.resrc/create))})))
        (catch Throwable _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.PROCESS)
                               ($.cell/* "Error while starting process"))))))
