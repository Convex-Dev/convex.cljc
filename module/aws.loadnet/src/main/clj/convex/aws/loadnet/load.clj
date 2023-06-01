(ns convex.aws.loadnet.load

  "Managing Load Generators."

  (:require [convex.aws.loadnet.rpc :as $.aws.loadnet.rpc]
            [convex.cell            :as $.cell]
            [taoensso.timbre        :as log]))


;;;;;;;;;;


(defn start

  "Starts all Load Generators on EC2 instances (if needed).

   Monitors and logs any Load Generator that seemingly terminates before the simulated
   ends, meaning something went wrong and simulated users were not able to transact
   anymore."

  [env]

  (let [n-load (count (env :convex.aws.ip/load+))]
    (if (zero? n-load)
      (do
        (log/info "No load generator to start")
        env)
      (let [ip-peer+   (env :convex.aws.ip/peer+)
            n-peer     (count ip-peer+)
            n-load-cvx ($.cell/long n-load)
            *stopped?  (env :convex.aws.loadnet/*stopped?)
            *stopped+  (env :convex.aws.loadnet.load/*stopped+)
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
                                                                                            n-peer)))}))))
                                 {:exit-fn (fn [process]
                                             (swap! *stopped+
                                                    conj
                                                    i-load)
                                             (let [exit     (:exit @process)
                                                   stopped? @*stopped?]
                                               (if stopped?
                                                 (log/info (format "Load generator %d terminated"
                                                                   i-load))
                                                 (if (zero? exit)
                                                   (log/warn (format "Load generator %d terminated but the simulation has not been stopped"
                                                                     i-load))
                                                   (log/warn (format "Load generator %d terminated with status %d before the simulation was stopped"
                                                                     i-load
                                                                     exit))))))}))
                             (range n-load))]
        (log/info "Done starting all load generators")
        (assoc env
               :convex.aws.loadnet.cvx/load+
               process+)))))



(defn stop

  "Kills all Load Generators (if any)."

  [env]

  (run! (fn [[i-load f*process]]
          (let [process @f*process]
            (when-not (zero? (:exit @process))
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
              (filter (comp not
                            @(env :convex.aws.loadnet.load/*stopped+))
                      (range (count (env :convex.aws.ip/load+))))))
  env)
