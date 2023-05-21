(ns convex.aws.loadnet.metric

  (:import (java.util Date))
  (:require [clojure.data.json        :as json]
            [convex.aws               :as $.aws]
            [convex.aws.loadnet.stack :as $.aws.loadnet.stack]))


;;;;;;;;;;


(defn client


  ([]

   (client nil))


  ([env]

   (assoc env
          :convex.aws.client/monitoring
          ($.aws/client :monitoring
                        "eu-central-1"
                        env))))


;;;;;;;;;;


(defn- -invoke

  [env op request]

  ($.aws/invoke (env :convex.aws.client/monitoring)
                op
                request))


;;;


(defn fetch

  [env]

  (let [id+     ($.aws.loadnet.stack/peer-id+ env)
        metric  (fn [metric-name ^String id]
                  {:Label      (json/write-str [metric-name
                                                id])
                   :MetricStat {:Metric {:Namespace  "AWS/EC2"
                                         :MetricName metric-name
                                         :Dimensions [{:Name  "InstanceId"
                                                       :Value id}]}
                                :Period 300
                                :Stat   "Average"}})
        metric+ (fn [id]
                  (for [metric-name ["CPUUtilization"
                                     "DiskReadBytes"
                                     "DiskReadOps"
                                     "DiskWriteBytes"
                                     "DiskWriteOps"
                                     "NetworkIn"
                                     "NetworkOut"
                                     "NetworkPacketsIn"
                                     "NetworkPacketsOut"]]
                    (metric metric-name
                            id)))
        request {:EndTime           (System/currentTimeMillis)
                 :MetricDataQueries (map (fn [i metric]
                                           (assoc metric
                                                  :Id
                                                  (format "m%d"
                                                          i)))
                                         (range)
                                         (mapcat metric+
                                                 id+))
                 :StartTime         0}]
    (reduce (fn [acc result+]
              (reduce (fn [acc-2 result]
                        (let [[metric-name
                               id]         (json/read-str (result :Label))]
                          (update-in acc-2
                                     [id
                                      metric-name]
                                     (fnil into
                                           [])
                                     (sort-by first
                                              (partition 2
                                                         (interleave (map (fn [^Date date]
                                                                            (.toEpochMilli (.toInstant date)))
                                                                          (:Timestamps result))
                                                                     (:Values result)))))))
                      acc
                      result+))
            {}
            (iteration (fn [next-token]
                         (-invoke env
                                  :GetMetricData
                                  (cond->
                                    request
                                    next-token
                                    (assoc :NextToken
                                           next-token))))
                       {:kf :NextToken
                        :vf :MetricDataResults}))))
