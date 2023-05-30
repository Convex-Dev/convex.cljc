(ns convex.aws.loadnet

  (:import (java.util Locale))
  (:require [babashka.fs                       :as bb.fs]
            [clojure.edn                       :as edn]
            [cognitect.aws.client.api          :as aws]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.cloudwatch     :as $.aws.loadnet.cloudwatch]
            [convex.aws.loadnet.default        :as $.aws.loadnet.default]
            [convex.aws.loadnet.load           :as $.aws.loadnet.load]
            [convex.aws.loadnet.load.log       :as $.aws.loadnet.load.log]
            [convex.aws.loadnet.log]
            [convex.aws.loadnet.peer           :as $.aws.loadnet.peer]
            [convex.aws.loadnet.peer.etch      :as $.aws.loadnet.peer.etch]
            [convex.aws.loadnet.rpc            :as $.aws.loadnet.rpc]
            [convex.aws.loadnet.stack          :as $.aws.loadnet.stack]
            [convex.aws.loadnet.stack-set      :as $.aws.loadnet.stack-set]
            [convex.cell                       :as $.cell]
            [taoensso.timbre                   :as log]))


(declare start
         stop)


(Locale/setDefault Locale/US)


;;;;;;;;;;


(defn create

  [env]

  (assert (env :convex.aws/account))
  (assert (not-empty (env :convex.aws/region+)))
  (assert (get-in env
                  [:convex.aws.stack/parameter+
                   :KeyName]))
  (let [dir (-> (or (env :convex.aws.loadnet/dir)
                    $.aws.loadnet.default/dir)
                (bb.fs/canonicalize)
                (str))]
    (bb.fs/create-dirs dir)
    (-> env
        (assoc :convex.aws.loadnet/dir            dir
               :convex.aws.loadnet/*stopped?      (atom false)
               :convex.aws.loadnet.load/*stopped+ (atom #{}))
        (update :convex.aws.loadnet.peer/native?
                #(or %
                     $.aws.loadnet.default/peer-native?))
        (update :convex.aws.region/n.load
                #(or %
                     $.aws.loadnet.default/n-load))
        (update :convex.aws.region/n.peer
                #(or %
                     $.aws.loadnet.default/n-peer))
        (update :convex.aws.stack/parameter+
                (fn [parameter+]
                  (-> parameter+
                      (update :DetailedMonitoring
                              #(or %
                                   $.aws.loadnet.default/detailed-monitoring))
                      (update :InstanceTypeLoad
                              #(or %
                                   $.aws.loadnet.default/instance-type-load))
                      (update :InstanceTypePeer
                              #(or %
                                   $.aws.loadnet.default/instance-type-peer)))))
        ($.aws.loadnet.cloudwatch/client+)
        ($.aws.loadnet.cloudformation/client+)
        ($.aws.loadnet.stack-set/create)
        (start))))



(defn delete

  [env]

  (-> env
      (merge (edn/read-string (slurp (format "%s/run.edn"
                                             (env :convex.aws.loadnet/dir)))))
      ($.aws.loadnet.cloudformation/client)
      ($.aws.loadnet.stack-set/delete)))



(defn start

  [env]

  (let [env-2 ($.aws.loadnet.rpc/await-ssh env)]
    (if (env-2 :convex.aws.loadnet/ssh-ready?)
      (let [timer (env-2 :convex.aws.loadnet/timer)
            env-3 (-> env-2
                      ($.aws.loadnet.peer/start)
                      ($.aws.loadnet.load/start)
                      (assoc :convex.aws.loadnet.timestamp/start
                             (System/currentTimeMillis)))]
        (log/info "Everything is ready")
        (when-not (zero? (env :convex.aws.region/n.load))
          (log/info "Simulation is running"))
        (when timer
          (log/info (format "Simulation will stop in %d minute(s)"
                            timer))
          (future
            (Thread/sleep (* timer ; minutes
                             60
                             1000))
            (when-not @(env-3 :convex.aws.loadnet/*stopped?)
              (stop env-3))))
        env-3)
      (do
        (log/error "Wait a bit and try starting the simulation")
        env-2))))



(defn stop

  [env]

  (log/info "Stopping simulation and deleting stack set")
  (-> env
      (update :convex.aws.loadnet/*stopped?
              reset!
              true)
      (assoc :convex.aws.loadnet.timestamp/end
             (System/currentTimeMillis))
      ($.aws.loadnet.load/stop)
      ($.aws.loadnet.peer/stop)
      ($.aws.loadnet.peer/log+)
      ($.aws.loadnet.load.log/download)
      ($.aws.loadnet.load.log/stat+)
      ($.aws.loadnet.peer.etch/download)
      ($.aws.loadnet.peer.etch/stat+)
      ($.aws.loadnet.cloudwatch/download)
      ($.aws.loadnet.stack-set/delete)))


;;;;;;;;;;


(comment


  (def env
       (create {:convex.aws/account                  (System/getenv "CONVEX_AWS_ACCOUNT")
                :convex.aws/region+                  ["eu-central-1"
                                                      ;"us-east-1"
                                                      ;"us-west-1"
                                                      ;"ap-southeast-1"
                                                      ]
                :convex.aws.key/file                 "/Users/adam/Code/convex/clj/private/Test"
                :convex.aws.loadnet/dir              "/tmp/loadnet"
                :convex.aws.loadnet/timer            6
                :convex.aws.loadnet.peer/native?     true
                :convex.aws.loadnet.scenario/path    ($.cell/* (lib sim scenario torus))
                :convex.aws.loadnet.scenario/param+  ($.cell/* {:n.token 5
                                                                :n.user  20})
                :convex.aws.region/n.peer           1
                :convex.aws.region/n.load           1
                :convex.aws.stack/parameter+        {;:DetailedMonitoring "false"
                                                     :KeyName            "Test"
                                                     ;:InstanceTypeLoad   "t2.micro"
                                                     ;:InstanceTypePeer   "t2.micro"
                                                     }
                :convex.aws.stack/tag+              {:Project "Ontochain"}}))


  (future
    (stop env)
    nil)


  (future
    (delete {:convex.aws/account     (System/getenv "CONVEX_AWS_ACCOUNT")
             :convex.aws.loadnet/dir "/tmp/loadnet"})
    nil)


  ;; If awaiting SSH servers fail.
  ;
  (future
    (def env
         (start env)))

  (def env
       ($.aws.loadnet.peer/start env))

  (def env
       ($.aws.loadnet.load/start env))



  ($.aws.loadnet.stack-set/describe env)


  (deref ($.aws.loadnet.rpc/worker env :convex.aws.ip/peer+ 2 (convex.cell/* (.sys.exit 0))))



  ($.aws.loadnet.peer/stop env)
  ($.aws.loadnet.load/stop env)
  (time ($.aws.loadnet.peer/log+ env))
  (time ($.aws.loadnet.peer.etch/download env))
  (time ($.aws.loadnet.cloudwatch/download env))
  (time ($.aws.loadnet.peer.etch/stat+ {:convex.aws.loadnet/dir "/tmp/loadnet"}))

  (time ($.aws.loadnet.load.log/download env))
  (time ($.aws.loadnet.load.log/stat+ env))


  )
