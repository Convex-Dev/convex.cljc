(ns convex.aws.loadnet.load

  (:require [babashka.fs            :as bb.fs]
            [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]
            [taoensso.timbre        :as log]))


;;;;;;;;;;


(defn start

  [env]

  (let [n-load (count (env :convex.aws.ip/load+))]
    (if (zero? n-load)
      (do
        (log/info "No load generator to start")
        env)
      (let [ip-peer+   (env :convex.aws.ip/peer+)
            n-peer     (count ip-peer+)
            n-load-cvx ($.cell/long n-load)
            process+   (mapv (fn [i-load]
                               (log/info (format "Starting load generator %d"
                                                 i-load))
                               ($.aws.loadnet.rpc/cvx
                                 env
                                 :convex.aws.ip/load+
                                 i-load
                                 ($.cell/*
                                   (let [_    (.project.dir.set "/home/ubuntu/repo/lab.cvx")
                                         dep+ (.dep.deploy '[$.sim.load (lib sim load)
                                                             scenario   ~(env :convex.aws.loadnet.scenario/path)])]
                                     (def $.sim.load
                                          (get dep+
                                               '$.sim.load))
                                     (def scenario
                                          (get dep+
                                               'scenario))
                                     (.file.txt.write "/tmp/load.pid"
                                                      (.sys.pid))
                                     (.log.level.set :info)
                                     (.log.out.set (.file.stream.out "/tmp/load.cvx"))
                                     ($.sim.load/await
                                       ($.sim.load/start scenario/gen.trx
                                                         {:bucket [~n-load-cvx
                                                                   ~($.cell/long i-load)]
                                                          :host   ~($.cell/string (get ip-peer+
                                                                                       (mod i-load
                                                                                            n-peer)))}))))))
                             (range n-load))]
        (log/info "Done starting all load generators")
        (assoc env
               :convex.aws.loadnet.cvx/load+
               process+)))))



(defn stop

  [env]

  (run! (fn [[i-load f*process]]
          (let [process @f*process]
            (if (zero? (:exit @process))
              (log/info (format "Load generator %d killed"
                                i-load))
              (log/error (format "Problem while killing load generator %d, STDERR = %s"
                                 i-load
                                 (slurp (:err process)))))))
        (mapv (fn [i-load]
                (log/info (format "Killing load generator %d"
                                  i-load))
                [i-load
                 (future
                   ($.aws.loadnet.rpc/kill-process env
                                                   :convex.aws.ip/load+
                                                   i-load
                                            "/tmp/load.pid"))])
              (range (count (env :convex.aws.ip/load+)))))
  env)
