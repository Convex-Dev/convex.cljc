(ns convex.aws.loadnet.cloudwatch

  (:import (java.util Date))
  (:require [babashka.fs              :as bb.fs]
            [clojure.data.csv         :as csv]
            [clojure.data.json        :as json]
            [clojure.java.io          :as java.io]
            [convex.aws               :as $.aws]
            [convex.aws.loadnet.stack :as $.aws.loadnet.stack]
            [kixi.stats.core          :as kixi.stats]
            [taoensso.timbre          :as log]))


(declare fetch-region)


;;;;;;;;;;


(defn- -dir

  [env]

  (format "%s/cloudwatch"
          (env :convex.aws.loadnet/dir)))



(defn- -period

  [env]

  (if (= (get-in env
                 [:convex.aws.stack/parameter+
                  :DetailedMonitoring])
         "true")
    60
    300))



(defn- -persist-metric+

  [env metric+]

  (let [dir (-dir env)]
    (log/info (format "Saving CloudWatch metrics to '%s'"
                      dir))
    (bb.fs/create-dirs dir)
    (spit (format "%s/metrics.edn"
                  dir)
          metric+)
    (with-open [out (java.io/writer (format "%s/metrics.csv"
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
        period   (-period env)
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
                  :StartTime         (env :convex.aws.loadnet.timestamp/start)}
        result   (reduce (fn [acc result+]
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
                                     :vf :MetricDataResults}))]
    (-> env
        (assoc :convex.aws.loadnet.cloudwatch/metric+
               result)
        (-persist-metric+ result))))



(defn stat+

  [env]

  (let [metric+          (vals (env :convex.aws.loadnet.cloudwatch/metric+))
        cpu-utilization  (transduce (comp (mapcat #(get %
                                                        "CPUUtilization"))
                                          (map second)
                                          (map #(double (/ %
                                                           100))))
                                    kixi.stats/summary
                                    metric+)
        mem-used         (transduce (comp (mapcat #(get %
                                                        "mem_used"))
                                          (map second)
                                          (map #(double (/ %
                                                           1e6))))
                                    kixi.stats/summary
                                    metric+)
        period           (-period env)
        net              (fn [k]
                           (let [byte+  (into []
                                              (comp (mapcat #(get %
                                                                  k))
                                                    (map second))
                                              metric+)
                                 volume (reduce +
                                                byte+)]
                             [(double (/ volume
                                         (count byte+)
                                         period
                                         1e6))
                              (double (/ volume
                                         1e9))]))
        [net-in-mbps
         net-in-volume]  (net "NetworkIn")
        [net-out-mbps
         net-out-volume] (net "NetworkOut")
        summary          {:cpu-utilization-percent cpu-utilization
                          :mem-used-mb             mem-used
                          :net-in-mbps             net-in-mbps
                          :net-in-volume-gb        net-in-volume
                          :net-out-mbps            net-out-mbps
                          :net-out-volume-gb       net-out-volume}
        summary-path     (format "%s/summary.edn"
                                 (-dir env))]
    (log/info (format "CPU utilization per peer (percent) = %s"
                      cpu-utilization))
    (log/info (format "Memory used per peer (MB) = %s"
                      mem-used))
    (log/info (format "Network Input total volume (GB) = %.3f"
                      net-in-volume))
    (log/info (format "Network Input speed per peer (MB/s) = %.3f"
                      net-in-mbps))
    (log/info (format "Network Output total volume (GB) = %.3f"
                      net-out-volume))
    (log/info (format "Network Output speed per peer (MB/s) = %.3f"
                      net-out-mbps))
    (log/info (format "Storing summary of above CloudWatch metrics to '%s'"
                      summary-path))
    (spit summary-path
          summary)
    (assoc env
           :convex.aws.loadnet.cloudwatch/summary
           summary)))


;;;;;;;;;;


(defn download

  [env]

  (-> env
      (fetch)
      (stat+)))
