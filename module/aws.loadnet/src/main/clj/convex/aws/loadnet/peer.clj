(ns convex.aws.loadnet.peer

  (:require [babashka.fs                   :as bb.fs]
            [clojure.test.check.generators :as TC.gen]
            [convex.aws.loadnet.rpc        :as $.aws.loadnet.rpc]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.db                     :as $.db]
            [convex.key-pair               :as $.key-pair]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]
            [kixi.stats.core               :as kixi.stats]
            [taoensso.timbre               :as log]))


;;;;;;;;;;


(defn- -await-ready

  [env i-peer]

  (when-not (= ($.cell/* :ready)
               @($.aws.loadnet.rpc/worker env
                                          i-peer
                                          ($.cell/* :ready)))
    (throw (Exception. (format "Problem while testing if peer %d was ready"
                               i-peer))))
  (log/info (format "Peer process %d ready"
                    i-peer)))



(defn- -dir

  [env]

  (-> (or (env :convex.aws.loadnet/dir)
          "./")
      (bb.fs/canonicalize)
      (str)))


;;;;;;;;;;


(defn start-genesis

  [env]

  (log/info "Starting peer 0 (genesis)")
  (let [env-2
        (assoc env
               :convex.aws.loadnet.cvx/peer+
               [($.aws.loadnet.rpc/cvx
                  env
                  0
                  ($.cell/*
                    (do
                      (.project.dir.set "/home/ubuntu/repo/lab.cvx")
                      (let [dep+ (.dep.deploy '[$.net.test           (lib net test)
                                                $.sim.scenario.torus (lib sim scenario torus)])]
                        (def $.net.test
                             (get dep+
                                  '$.net.test))
                        (def $.sim.scenario.torus
                             (get dep+
                                  '$.sim.scenario.torus))
                        ($.net.test/start.genesis {:dir   "/tmp/peer"
                                                   :state (:state ($.sim.scenario.torus/state
                                                                    ($.net.test/state.genesis
                                                                      {:peer+
                                                                       ~($.cell/vector (map (fn [ip]
                                                                                              ($.cell/* {:host ~($.cell/string ip)}))
                                                                                            (env :convex.aws.ip/peer+)))})
                                                                    5
                                                                    20))})
                        (loop []
                          (.worker.start {:pipe "peer"})
                          (recur))))))])]
    (-await-ready env-2
                  0)
    env-2))



(defn start-syncer+

  [env]

  (log/info "Starting syncers")
  (let [ip+ (env :convex.aws.ip/peer+)]
    (loop [process+ []
           ready+   [(first ip+)]
           todo+    (rest ip+)]
      (if (seq todo+)
        (let [n-ready (count ready+)]
          (recur (into process+
                       (map (fn [[i-peer process]]
                              (-await-ready env
                                            i-peer)
                              process))
                       (mapv (fn [i-batch ip-ready _ip-todo]
                               (let [i-peer (+ n-ready
                                               i-batch)]
                                 (log/info (format "Starting peer process %d"
                                                   i-peer))
                                 [i-peer
                                  ($.aws.loadnet.rpc/cvx
                                   env
                                   i-peer
                                   ($.cell/*
                                     (do
                                       (.project.dir.set "/home/ubuntu/repo/lab.cvx")
                                       (let [dep+ (.dep.deploy '[$.net.test (lib net test)])]
                                         (def $.net.test
                                              (get dep+
                                                   '$.net.test))
                                         ($.net.test/start.sync ~($.cell/long i-peer)
                                                                {:dir "/tmp/peer"
                                                                 :remote.host ~($.cell/string ip-ready)}))
                                       (loop []
                                         (.worker.start {:pipe "peer"})
                                         (recur)))))]))
                             (range)
                             ready+
                             todo+))
                 (into ready+
                       (take n-ready
                             todo+))
                 (drop n-ready
                       todo+)))
        (update env
                :convex.aws.loadnet.cvx/peer+
                into
                process+)))))


;;;


(defn start

  [env]

  (let [env-2 (-> env
                  (start-genesis)
                  (start-syncer+))]
    (log/info "All peer processes ready to receive transactions")
    env-2))



(defn stop

  [env]

  (doseq [[i-peer
           d*result] (mapv (fn [i-peer]
                             (log/info (format "Stopping peer server %d"
                                               i-peer))
                             [i-peer
                              ($.aws.loadnet.rpc/worker env
                                                        i-peer
                                                        ($.cell/*
                                                          (do
                                                            (.peer.stop peer)
                                                            (.db.flush)
                                                            :ok)))])
                           (range (count (env :convex.aws.ip/peer+))))]
    (when-not (= @d*result
                 ($.cell/* :ok))
      (log/error (format "Peer server %d might not have been stopped properly"
                         i-peer))))
  (log/info "All peer servers have been stopped")
  env)


;;;;;;;;;;


(defn etch


  ([env]

   (etch env
         nil))


  ([env i-peer]

   (let [dir      (format "%s/etch"
                          (-dir env))
         i-peer-2 (or i-peer
                      0)]
     (log/info (format "Downloading Etch instance from peer %d to '%s'"
                       i-peer-2
                       dir))
     (bb.fs/create-dirs dir)
     ($.aws.loadnet.rpc/rsync env
                              i-peer-2
                              (format "%s/%d.etch"
                                      dir
                                      i-peer-2)
                              {:src "store.etch"})
     (log/info (format "Finished downloading Etch instance from peer %d"
                       i-peer-2)))))



(defn etch-stat


  ([env]

   (etch-stat env
              nil))


  ([env i-peer]

   (let [i-peer-2 (or i-peer
                      0)
         instance ($.db/open (format "%s/etch/%s.etch"
                                     (env :convex.aws.loadnet/dir)
                                     i-peer-2))]
     (try
       ($.db/current-set instance)
       (let [order           (get-in ($.db/root-read)
                                     [($.cell/* :belief)
                                      ($.cell/* :orders)
                                      (-> (TC.gen/generate $.gen/blob-32
                                                           0
                                                           (+ 12 ; address of first peer controller
                                                              i-peer-2))
                                          ($.key-pair/ed25519)
                                          ($.key-pair/account-key))
                                      ($.cell/* :value)])
             block+          ($.std/get order
                                        ($.cell/* :blocks))
             n-block         ($.std/count block+)
             n-trx+          (mapv (fn [block]
                                     (-> (get-in block
                                                 [($.cell/* :value)
                                                  ($.cell/* :transactions)])
                                         ($.std/count)))
                                   block+)
             cp              ($.clj/long ($.std/get order
                                                    ($.cell/* :consensus-point)))
             timestamp       (fn [i-block]
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
             n-trx-consensus (reduce +
                                     (take cp
                                           n-trx+))
             result          {:block-size      (transduce identity
                                                          kixi.stats/summary
                                                          n-trx+)
                              :bps             (double (/ cp
                                                          duration))
                              :duration        duration
                              :etch-size       ($.db/size)
                              :n.block         n-block
                              :n.trx.consensus n-trx-consensus
                              :point.consensus cp
                              :point.proposal  ($.clj/long ($.std/get order
                                                                      ($.cell/* :proposal-point)))
                              :timestamp.first timestamp-first
                              :timestamp.last  timestamp-last
                              :tps             (double (/ n-trx-consensus
                                      duration))}]
         (log/info (format "Peer %d stats"
                           i-peer-2))
         (log/info (if (< duration
                          60000)
                     (format "Load duration (seconds) = %.2f"
                             duration)
                     (format "Load duration (minutes) = %.2f"
                             (/ duration
                                60))))
         (log/info (format "Number of blocks = %d"
                           (result :n.block)))
         (log/info (format "Consensus point = %d"
                           (result :point.consensus)))
         (log/info (format "Proposal point = %d"
                           (result :point.proposal)))
         (log/info (format "Number of transactions in consensus = %d"
                           (result :n.trx.consensus)))
         (log/info (format "Blocks / second = %.2f"
                           (result :bps)))
         (log/info (format "Transactions / second = %.2f"
                           (result :tps)))
         (log/info (format "Block size quartiles = %s"
                           (result :block-size)))
         (log/info (format "Etch size (MB) = %.2f"
                           (/ (result :etch-size)
                              1e6)))
         result)
       (finally
         ($.db/close))))))



(defn log+

  [env]

  (let [dir (format "%s/log"
                    (-dir env))]
    (log/info (format "Collecting peer logs to '%s'"
                      dir))
    (bb.fs/create-dirs dir)
    (run! deref
          (mapv (fn [i-peer]
                  (future
                    ($.aws.loadnet.rpc/rsync env
                                             i-peer
                                             (format "%s/%d.cvx"
                                                     dir
                                                     i-peer)
                                             {:src "log.cvx"})))
                (range (count (env :convex.aws.ip/peer+))))))
  (log/info "Done collecting peer logs")
  env)
