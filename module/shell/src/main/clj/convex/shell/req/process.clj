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


;;;;;;;;;;


(defn run

  [ctx [command dir]]

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
      (try
        (let [exit (promesa/deferred)
              p    (P.process/run (map (fn [x]
                                         (if (nil? x)
                                           "nil"
                                           (str x)))
                                       command)
                                  {:dir     (some-> dir
                                                    ($.clj/string))
                                   :exit-fn (fn [p-2]
                                              (promesa/resolve! exit
                                                                ($.shell.async/success ($.cell/long (:exit p-2)))))})]
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
