(ns convex.aws.loadnet.peer
  
  (:require [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]))


;;;;;;;;;;


(defn genesis

  [env]

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
    (when-not (= ($.cell/* :ready)
                 ($.aws.loadnet.rpc/worker env-2
                                           0
                                           ($.cell/* :ready)))
      (throw (Exception. "Problem while testing if genesis peer was ready")))
    env-2))
