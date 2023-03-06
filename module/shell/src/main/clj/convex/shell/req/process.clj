(ns convex.shell.req.process

  (:import (java.io BufferedReader
                    InputStreamReader
                    OutputStreamWriter
                    Reader
                    Writer))
  (:require [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.shell.async :as $.shell.async]
            [convex.shell.io    :as $.shell.io]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]
            [protosens.process  :as P.process]
            [promesa.core       :as promesa]))


;;;;;;;;;; Private


(defn -do-stream

  [ctx class stream f]

  (if (nil? stream)
    (f nil)
    ($.shell.resrc/unwrap-with ctx
                               stream
                               (fn [stream-2]
                                 (if (instance? class
                                                stream-2)
                                   (f stream-2)
                                   ($.cvm/exception-set ctx
                                                        ($.cell/code-std* :ARGUMENT)
                                                        ($.cell/* "Not a stream")))))))



(defn- -env

  [m]

  (into {}
        (map (fn [[k v]]
               [(str k)
                (str v)]))
        m))


;;;;;;;;;; Requests


(defn kill

  [ctx [process]]

  ($.shell.resrc/unwrap-with ctx
                             process
                             (fn [process-2]
                               (or (when-not (instance? babashka.process.Process
                                                        process-2)
                                     ($.cvm/exception-set ctx
                                                          ($.cell/code-std* :ARGUMENT)
                                                          ($.cell/* "Not a process")))
                                   (try
                                     ;;
                                     (P.process/destroy process-2)
                                     ($.cvm/result-set ctx
                                                       nil)
                                     ;;
                                     (catch Throwable _ex
                                       ($.cvm/exception-set ctx
                                                            ($.cell/* :SHELL.PROCESS)
                                                            ($.cell/* "Unable to kill given process"))))))))



(defn run

  [ctx [command dir env env-extra err in out]]

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
      (-do-stream
        ctx
        Writer
        err
        (fn [err-2]
          (-do-stream
            ctx
            Reader
            in
            (fn [in-2]
              (-do-stream
                ctx
                Writer
                out
                (fn [out-2]
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
                                               :err       err-2
                                               :exit-fn   (fn [p-2]
                                                            ;; Given output streams are not flushed automatically.
                                                            (some-> err-2
                                                                    ($.shell.io/flush))
                                                            (some-> out-2
                                                                    ($.shell.io/flush))
                                                            (promesa/resolve! exit
                                                                              ($.shell.async/success ($.cell/long (:exit p-2)))))
                                               :extra-env (some-> env-extra
                                                                  -env)
                                               :in        in-2
                                               :out       out-2})]
                      ($.cvm/result-set ctx
                                        ($.cell/* {:err     ~(when-not err-2
                                                               (-> p
                                                                   (:err)
                                                                   (InputStreamReader.)
                                                                   (BufferedReader.)
                                                                   ($.shell.resrc/create)))
                                                   :exit    ~($.shell.resrc/create exit)
                                                   :in      ~(-> p
                                                                 (:in)
                                                                 (OutputStreamWriter.)
                                                                 ($.shell.resrc/create))
                                                   :out     ~(when-not out-2
                                                               (-> p
                                                                   (:out)
                                                                   (InputStreamReader.)
                                                                   (BufferedReader.)
                                                                   ($.shell.resrc/create)))
                                                   :process ~($.shell.resrc/create p)})))
                    (catch Throwable _ex
                      ($.cvm/exception-set ctx
                                           ($.cell/* :SHELL.PROCESS)
                                           ($.cell/* "Error while starting process"))))))))))))
