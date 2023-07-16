(ns convex.aws.loadnet.load.log

  "Log analysis of load generators for computing finality."

  (:import (java.io BufferedReader))
  (:require [babashka.fs            :as bb.fs]
            [clojure.java.io        :as java.io]
            [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]
            [convex.clj             :as $.clj]
            [convex.read            :as $.read]
            [convex.std             :as $.std]
            [kixi.stats.core        :as kixi.stats]
            [taoensso.timbre        :as log]))


;;;;;;;;;;


(defn dir

  "Returns the directory where all logs will be downloaded from load generator
   instances."

  [env]

  (format "%s/log/load"
          (env :convex.aws.loadnet/dir)))



(defn download

  "Downloads all logs from load generator instances.
  
   Also see [[dir]]."

  [env]

  (let [dir (dir env)]
    (log/info (format "Collecting load generator logs to '%s'"
                      dir))
    (bb.fs/create-dirs dir)
    (run! deref
          (mapv (fn [i-peer]
                  (future
                    ($.aws.loadnet.rpc/rsync env
                                             :convex.aws.ip/load+
                                             i-peer
                                             "/tmp/load.cvx"
                                             (format "%s/%d.cvx"
                                                     dir
                                                     i-peer))))
                (range (count (env :convex.aws.ip/load+))))))
  (log/info "Done collecting load generator logs")
  env)



(defn stat+

  "Statistical analysis of load generator logs.
  
   For the time being, computes the finality only."

  [env]

  (log/info "Starting finality analysis from load generator logs")
  (let [dir            (dir env)

        rtt+           (reduce (fn [acc i-load]
                                 (with-open [reader (BufferedReader. (java.io/reader (format "%s/%d.cvx"
                                                                                             dir
                                                                                             i-load)))]
                                   (into acc
                                         (comp (map first)
                                               (take-while some?)
                                               (keep (fn [entry]
                                                       (let [data ($.std/nth entry
                                                                             3)]
                                                         (when (and ($.std/vector? data)
                                                                    (= ($.std/nth data
                                                                                  0)
                                                                       ($.cell/* :client.result)))
                                                           (-> data
                                                               ($.std/nth 1)
                                                               (get ($.cell/* :rtt))
                                                               ($.clj/double)))))))
                                         (repeatedly #($.read/line reader)))))
                               []
                               (range (* (count (env :convex.aws/region+))
                                         (env :convex.aws.region/n.load))))
       finality-avg    (transduce identity
                                  kixi.stats/mean
                                  rtt+)
       finality-quart  (transduce identity
                                  kixi.stats/summary
                                  rtt+)
       finality-stddev (transduce identity
                                  kixi.stats/standard-deviation
                                  rtt+)
       path-result     (format "%s/finality.edn"
                               (env :convex.aws.loadnet/dir))
       result          {:convex.aws.loadnet.finality/avg       finality-avg
                        :convex.aws.loadnet.finality/quartile+ finality-quart
                        :convex.aws.loadnet.finality/stddev    finality-stddev}]
    (log/info (format "Finality (avg) = %.2f milliseconds"
                      finality-avg))
    (log/info (format "Finality (stddev) = %.2f milliseconds"
                      finality-stddev))
    (log/info (format "Finality (quartiles) = %s"
                      finality-quart))
    (log/info (format "Saving finality analysis to '%s'"
                      path-result))
    (spit path-result
          result)
    (merge env
           result)))
