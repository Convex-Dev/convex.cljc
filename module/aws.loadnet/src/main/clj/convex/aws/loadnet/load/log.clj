(ns convex.aws.loadnet.load.log

  (:require [babashka.fs            :as bb.fs]
            [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]
            [convex.clj             :as $.clj]
            [convex.read            :as $.read]
            [convex.std             :as $.std]
            [kixi.stats.core        :as kixi.stats]
            [taoensso.timbre        :as log]))


;;;;;;;;;;


(defn dir

  [env]

  (format "%s/log/load"
          (env :convex.aws.loadnet/dir)))



(defn download

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

  [env]

  (log/info "Starting finality analysis from load generator logs")
  (let [dir            (dir env)
        entry+         (into []
                             (comp (mapcat (fn [i-load]
                                             ($.read/file (format "%s/%d.cvx"
                                                                  dir
                                                                  i-load))))
                                   (keep (fn [entry]
                                           (let [data ($.std/nth entry
                                                                 3)]
                                             (when ($.std/vector? data)
                                               data)))))
                             (range (* (count (env :convex.aws/region+))
                                       (env :convex.aws.region/n.load))))
       xform-finality  (keep (fn [data]
                               (when (= ($.std/nth data
                                                   0)
                                        ($.cell/* :client.result))
                                 (let [result ($.std/nth data
                                                         1)]
                                   ($.clj/double (get result
                                                      ($.cell/* :rtt)))))))
       finality-avg    (transduce xform-finality
                                  kixi.stats/mean
                                  entry+)
       finality-quart  (transduce xform-finality
                                  kixi.stats/summary
                                  entry+)
       finality-stddev (transduce xform-finality
                                  kixi.stats/standard-deviation
                                  entry+)
       path-result     (format "%s/finality.edn"
                               (env :convex.aws.loadnet/dir))
       result          {:convex.aws.loadnet.finality/avg       finality-avg
                        :convex.aws.loadnet.finality/quartile+ finality-quart
                        :convex.aws.loadnet.finality/stddev    finality-stddev}]
    (log/info (format "Finality (avg) = %f milliseconds"
                      finality-avg))
    (log/info (format "Finality (stddev) = %f milliseconds"
                      finality-stddev))
    (log/info (format "Finality (quartiles) = %s"
                      finality-quart))
    (log/info (format "Saving finality analysis to '%s'"
                      path-result))
    (spit path-result
          result)
    (merge env
           result)))
