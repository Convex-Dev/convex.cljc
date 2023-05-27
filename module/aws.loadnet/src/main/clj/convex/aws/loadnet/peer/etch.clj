(ns convex.aws.loadnet.peer.etch

  (:require [babashka.fs                   :as bb.fs]
            [clojure.test.check.generators :as TC.gen]
            [convex.aws.loadnet.rpc        :as $.aws.loadnet.rpc]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.db                     :as $.db]
            [convex.gen                    :as $.gen]
            [convex.key-pair               :as $.key-pair]
            [convex.std                    :as $.std]
            [kixi.stats.core               :as kixi.stats]
            [taoensso.timbre               :as log]))


;;;;;;;;;;


(defn download


  ([env]

   (download env
             nil))


  ([env i-peer]

   (let [dir      (format "%s/etch"
                          (env :convex.aws.loadnet/dir))
         i-peer-2 (or i-peer
                      0)
         path     (format "%s/%d.etch"
                          dir
                          i-peer-2)]
     (log/info (format "Downloading Etch instance from peer %d to '%s'"
                       i-peer-2
                       path))
     (bb.fs/create-dirs dir)
     ($.aws.loadnet.rpc/rsync env
                              i-peer-2
                              path
                              {:src "store.etch"})
     (log/info (format "Finished downloading Etch instance from peer %d"
                       i-peer-2)))
   env))


;;;;;;;;;;


(defn stat+


  ([env]

   (stat+ env
          nil))


  ([env i-peer]

   (let [i-peer-2    (or i-peer
                         0)
         dir         (format "%s/etch"
                             (env :convex.aws.loadnet/dir))
         path        (format "%s/%s.etch"
                             dir
                             i-peer-2)
         _           (log/info (format "Computing Etch stats from '%s'"
                                       path))
         result-file (format "%s/%d.edn"
                             dir
                             i-peer-2)
         instance    ($.db/open path)]
     (try
       ($.db/current-set instance)
       (if-some [order (get-in ($.db/root-read)
                               [($.cell/* :belief)
                                ($.cell/* :orders)
                                (-> (TC.gen/generate $.gen/blob-32
                                                     0
                                                     (+ 12 ; address of first peer controller
                                                        i-peer-2))
                                    ($.key-pair/ed25519)
                                    ($.key-pair/account-key))
                                ($.cell/* :value)])]
         (let [etch-size        ($.db/size)
               block+           ($.std/get order
                                           ($.cell/* :blocks))
               n-block          ($.std/count block+)
               n-trx+           (mapv (fn [block]
                                        (-> (get-in block
                                                    [($.cell/* :value)
                                                     ($.cell/* :transactions)])
                                            ($.std/count)))
                                      block+)
               block-size       (transduce identity
                                           kixi.stats/summary
                                           n-trx+)
               cp               ($.clj/long ($.std/get order
                                                       ($.cell/* :consensus-point)))
               pp               ($.clj/long ($.std/get order
                                                       ($.cell/* :proposal-point)))
               n-trx-consensus (reduce +
                                       (take cp
                                             n-trx+))
               result          {:block-size      block-size
                                :etch-size       etch-size
                                :n.block         n-block
                                :n.trx.consensus n-trx-consensus
                                :point.consensus cp
                                :point.proposal  pp}]
           (log/info (format "Etch size (MB) = %.2f"
                              (/ etch-size
                                 1e6)))
           (log/info (format "Number of blocks = %d"
                             n-block))
           (log/info (format "Consensus point = %d"
                             cp))
           (log/info (format "Proposal point = %d"
                             pp))
           (log/info (format "Number of transactions in consensus = %d"
                             n-trx-consensus))
           (log/info (format "Block size quartiles = %s"
                             block-size))
           (let [result-2 (if (<= n-block
                                  1)
                            (do
                              (log/warn "Cannot compute further Etch stats, more than 1 block needed")
                              result)
                            (let [timestamp       (fn [i-block]
                                                    (-> ($.std/nth block+
                                                                   i-block)
                                                        (get-in [($.cell/* :value)
                                                                 ($.cell/* :timestamp)])
                                                        ($.clj/long)))
                                  timestamp-first (timestamp 0)
                                  timestamp-last  (timestamp (dec n-block))
                                  duration        (double (/ (- timestamp-last
                                                                timestamp-first)
                                                             1000))
                                  bps             (double (/ cp
                                                             duration))
                                  tps             (double (/ n-trx-consensus
                                                             duration))]
                              (log/info (if (< duration
                                               60000)
                                          (format "Load duration (seconds) = %.2f"
                                                  duration)
                                          (format "Load duration (minutes) = %.2f"
                                                  (/ duration
                                                     60))))
                              (log/info (format "Blocks / second = %.2f"
                                                bps))
                              (log/info (format "Transactions / second = %.2f"
                                                tps))
                              (assoc result
                                     :bps             bps
                                     :duration        duration
                                     :timestamp.first timestamp-first
                                     :timestamp.last  timestamp-last
                                     :tps             :tps)))]
             (log/info (format "Saving Etch stats to '%s'"
                               result-file))
             (spit result-file
                   result-2)
           (assoc-in env
                     [:convex.aws.loadnet.etch/stat+
                      i-peer-2]
                     result-2)))
         (do
           (log/warn "Ordering missing, cannot compute Etch stats")
           env))
       ;;
       (finally
         ($.db/close))))))
