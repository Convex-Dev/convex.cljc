(ns convex.shell.req.file

  "Requests relating to file utils.
  
   For the time being, only about opening streams. All other utilities are
   written in Convex Lisp."

  {:author "Adam Helinski"}

  (:import (java.io FileOutputStream)
           (java.nio.channels FileLock))
  (:require [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.shell.io    :as $.shell.io]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(defn- -stream

  ;; Used by [[stream-in]] and [[stream-out]].

  [ctx path open str-op]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Path to file must be a string")))
      (let [[stream
             ctx-err] (try
                        [(open (str path))
                         nil]
                        (catch Throwable _ex
                          [nil
                           ($.cvm/exception-set ctx
                                                ($.cell/* :STREAM)
                                                ($.cell/string (format "Unable to open file for '%s': %s"
                                                                       path
                                                                       str-op)))]))]
        (or ctx-err
            ($.cvm/result-set ctx
                              ($.shell.resrc/create stream))))))


;;;;;;;;; Requests


(defn lock

  "Request for getting an exclusive file lock."

  [ctx [path]]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "File path to lock must be a String")))
      (try
        (let [^FileOutputStream out (FileOutputStream. ^String ($.clj/string path))]
          (try
            ($.cvm/result-set ctx
                              ($.shell.resrc/create (-> out
                                                        (.getChannel)
                                                        (.lock))))
            (catch Throwable _ex
              (try
                ($.cvm/result-set ctx
                                  nil)
                (finally
                  (.close out))))))
        (catch Throwable _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.FILE)
                               ($.cell/* "Unable to open file for acquiring lock"))))))



(defn lock-release

  "Request for releasing an exclusive file lock."

  [ctx [lock]]

  ($.shell.resrc/unwrap-with ctx
                             lock
                             (fn [lock-2]
                               (if (instance? FileLock
                                              lock-2)
                                 (try
                                   ;;
                                   (-> ^FileLock lock-2
                                       (.acquiredBy)
                                       (.close))
                                   ($.cvm/result-set ctx
                                                     nil)
                                   ;;
                                   (catch Throwable _ex
                                     ($.cvm/exception ctx
                                                      ($.cell/* :SHELL.FILE)
                                                      ($.cell/* "Unable to release file lock"))))
                                 ($.cvm/exception-set ctx
                                                      ($.cell/code-std* :ARGUMENT)
                                                      ($.cell/* "Resource is not a file lock"))))))



(defn stream-in

  "Request for opening an input stream for file under `path`."

  [ctx [path]]

  (-stream ctx
           path
           $.shell.io/file-in
           #{:read}))



(defn stream-out

  "Request for opening an output stream for file under `path`."

  [ctx [path append?]]

  (-stream ctx
           path
           (fn [path]
             ($.shell.io/file-out path
                                  append?))
           #{:write}))
