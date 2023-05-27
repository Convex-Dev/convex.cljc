(ns convex.aws.loadnet.cloudwatch

  (:import (java.util Date))
  (:require [babashka.fs              :as bb.fs]
            [clojure.data.csv         :as csv]
            [clojure.data.json        :as json]
            [clojure.java.io          :as java.io]
            [convex.aws               :as $.aws]
            [convex.aws.loadnet.stack :as $.aws.loadnet.stack]
            [taoensso.timbre          :as log]))


;;;;;;;;;;


(defn client+

  [env]

  (assoc env
         :convex.aws.client/cloudwatch+
         (into {}
               (map (fn [region]
                      [region
                       ($.aws/client :monitoring
                                     region
                                     env)]))
               (env :convex.aws/region+))))

;;;;;;;;;;


(defn- -invoke

  [env region op request]

  ($.aws/invoke (get-in env
                        [:convex.aws.client/cloudwatch+
                         region])
                op
                request))


;;;


(defn fetch-region

  [env region]

  (let [id+     ($.aws.loadnet.stack/peer-instance-id+ env
                                                       region)
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
                                     [[region id]
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
                                  region
                                  :GetMetricData
                                  (cond->
                                    request
                                    next-token
                                    (assoc :NextToken
                                           next-token))))
                       {:kf :NextToken
                        :vf :MetricDataResults}))))



(defn fetch

  [env]

  (let [result (reduce (fn [acc [region f*result]]
                         (let [result @f*result]
                           (log/info (format "Done retrieving CloudWatch metrics from region '%s'"
                                             region))
                           (merge acc
                                  result)))
                       {}
                       (map (fn [region]
                              (log/info (format "Retrieving CloudWatch metrics for instances in region '%s'"
                                                region))
                              [region
                               (future
                                 (fetch-region env
                                               region))])
                            (env :convex.aws/region+)))]
    (log/info "Done retrieving all CloudWatch metrics")
    result))



(defn save

  [env metric+]

  (let [dir (format "%s/metric"
                    (env :convex.aws.loadnet/dir))]
    (log/info (format "Saving CloudWatch metrics to '%s'"
                      dir))
    (bb.fs/create-dirs dir)
    (spit (format "%s/cloudwatch.edn"
                  dir)
          metric+)
    (with-open [out (java.io/writer (format "%s/cloudwatch.csv"
                                            dir))]
      (csv/write-csv out
                     (into []
                           (mapcat (fn [[[region instance-id] metric-name->data]]
                                     (into []
                                           (mapcat (fn [[metric-name data]]
                                                     (map (fn [[timestamp value]]
                                                            [region
                                                             instance-id
                                                             metric-name
                                                             timestamp
                                                             value])
                                                          data)))
                                           metric-name->data)))

                           metric+)))))
