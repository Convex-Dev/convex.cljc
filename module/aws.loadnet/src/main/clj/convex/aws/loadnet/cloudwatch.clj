(ns convex.aws.loadnet.cloudwatch

  (:import (java.util Date))
  (:require [babashka.fs                :as bb.fs]
            [clojure.data.csv           :as csv]
            [clojure.data.json          :as json]
            [clojure.java.io            :as java.io]
            [convex.aws                 :as $.aws]
            [convex.aws.loadnet.default :as $.aws.loadnet.default]
            [convex.aws.loadnet.stack   :as $.aws.loadnet.stack]
            [taoensso.timbre            :as log]))


(declare fetch-region)


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
                              (log/info (format "Retrieving CloudWatch metrics for peer instances in region '%s'"
                                                region))
                              [region
                               (future
                                 (fetch-region env
                                               region))])
                            (env :convex.aws/region+)))]
    (log/info "Done retrieving all CloudWatch metrics")
    result))



(defn fetch-region

  [env region]

  (let [id+      ($.aws.loadnet.stack/peer-instance-id+ env
                                                        region)
        detailed (= (or (get-in env
                                [:convex.aws.stack/parameter+
                                 :DetailedMonitoring])
                        $.aws.loadnet.default/detailed-monitoring)
                    "true")
        period   (if detailed
                   60
                   300)
        metric   (fn [namespace metric-name metric-stat ^String id]
                   {:Label      (json/write-str [metric-name
                                                 id])
                    :MetricStat {:Metric {:Namespace  namespace
                                          :MetricName metric-name
                                          :Dimensions [{:Name  "InstanceId"
                                                        :Value id}]}
                                 :Period period
                                 :Stat   metric-stat}})
        metric+  (fn [id]
                   (for [[namespace
                          metric-name
                          metric-stat] [["AWS/EC2"
                                         "CPUUtilization"
                                         "Average"]
                                        ["AWS/EC2"
                                         "NetworkIn"
                                         "Sum"]
                                        ["AWS/EC2"
                                         "NetworkOut"
                                         "Sum"]
                                        ["Convex/LoadNet"
                                         "mem_used"
                                         "Average"]
                                        ]]
                     (metric namespace
                             metric-name
                             metric-stat
                             id)))
        request  {:EndTime           (or (env :convex.aws.loadnet.timestamp/end)
                                         (System/currentTimeMillis))
                  :MetricDataQueries (map (fn [i metric]
                                            (assoc metric
                                                   :Id
                                                   (format "m%d"
                                                           i)))
                                          (range)
                                          (mapcat metric+
                                                  id+))
                  :StartTime         (env :convex.aws.loadnet.timestamp/start)}]
    (assoc env
           :convex.aws.loadnet.cloudwatch/metric+
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
                               :vf :MetricDataResults})))))


;;;;;;;;;;


(defn save

  [env]

  (let [dir     (format "%s/metric"
                        (env :convex.aws.loadnet/dir))
        metric+ (env :convex.aws.loadnet.cloudwatch/metric+)]
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

                           metric+))))
  env)


;;;;;;;;;;


(defn download

  [env]

  (-> env
      (fetch)
      (save)))
