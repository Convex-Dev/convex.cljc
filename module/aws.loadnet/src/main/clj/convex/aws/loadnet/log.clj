(ns convex.aws.loadnet.log

  "Logging setup redirecting entries directly to the STDOUT file descriptor.
   
   Plays nicely at the REPL."

  (:import (java.io FileDescriptor
                    FileWriter))
  (:refer-clojure :exclude [newline])
  (:require [taoensso.timbre :as log]))


;;;;;;;;;;


(def *file

  "File where entries are logged.
   Should be scoped to a run."

  (atom nil))



(def ^String newline

  "Platform-dependend newline."

  (System/getProperty "line.separator"))



(def ^FileWriter out

  "STDOUT"

  (FileWriter. FileDescriptor/out))


;;;;;;;;;;


(defn- -write-entry

  [^FileWriter out entry]

  (.write out
          entry)
  (.write out
          newline)
  (.flush out))


;;;;;;;;;;


(log/swap-config! (fn [config]
                    (-> config
                        (assoc :min-level
                               :info)
                        (update :appenders
                                (fn [appender+]
                                  (-> appender+
                                      (dissoc :println)
                                      (assoc :loadnet
                                             {:enabled? true
                                              :fn       (fn [data]
                                                          (let [entry (force (data :output_))]
                                                            (-write-entry out
                                                                          entry)
                                                            (some-> @*file
                                                                    (-write-entry entry))))})))))))


;;;;;;;;;;


(defn file-set

  "Sets [[*file]]."


  [^String path]

  (reset! *file
          (FileWriter. path))
  path)



(defn file-close

  "Closes [[*file]]."

  []

  (let [^FileWriter old (first (reset-vals! *file
                                            nil))]
    (some-> old
            (.close)))
  nil)
