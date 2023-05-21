(ns convex.aws.loadnet.log

  (:import (java.io FileDescriptor
                    FileWriter))
  (:refer-clojure :exclude [newline])
  (:require [taoensso.timbre :as log]))


;;;;;;;;;;


(def ^String newline

  (System/getProperty "line.separator"))



(def ^FileWriter out

  (FileWriter. FileDescriptor/out))


;;;;;;;;;;


(log/swap-config! (fn [config]
                    (-> config
                        (assoc :min-level
                               :info)
                        (update :appenders
                                (fn [appender+]
                                  (-> appender+
                                      (dissoc :println)
                                      (assoc :console
                                             {:enabled? true
                                              :fn       (fn [data]
                                                          (.write out
                                                                  (force (data :output_)))
                                                          (.write out
                                                                  newline)
                                                          (.flush out))})))))))
