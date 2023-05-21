(ns convex.aws.loadnet.peer
  
  (:require [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]
            [taoensso.timbre        :as log]))


;;;;;;;;;;


(defn -await-ready

  [env i-peer]

  (log/info (format "Awaiting peer %d"
                    i-peer))
  (when-not (= ($.cell/* :ready)
               ($.aws.loadnet.rpc/worker env
                                         i-peer
                                         ($.cell/* :ready)))
    (throw (Exception. (format "Problem while testing if peer %d was ready"
                               i-peer))))
  (log/info (format "Peer %d ready to receive transactions"
                    i-peer)))


;;;;;;;;;;


(defn genesis

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
                                                                    50))})
                        (loop []
                          (.worker.start {:pipe "peer"})
                          (recur))))))])]
    (-await-ready env-2
                  0)
    env-2))



(defn syncer+

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
                       (map (fn [i-batch ip-ready _ip-todo]
                              (let [i-peer (+ n-ready
                                              i-batch)]
                                (log/info (format "Starting peer %d"
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
