(ns convex.aws.loadnet

  "Running loadnets.
   
   See [[create]]."

  (:import (java.util Locale))
  (:require [babashka.fs                       :as bb.fs]
            [clojure.edn                       :as edn]
            [clojure.string                    :as string]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.cloudwatch     :as $.aws.loadnet.cloudwatch]
            [convex.aws.loadnet.default        :as $.aws.loadnet.default]
            [convex.aws.loadnet.load           :as $.aws.loadnet.load]
            [convex.aws.loadnet.load.log       :as $.aws.loadnet.load.log]
            [convex.aws.loadnet.log]
            [convex.aws.loadnet.peer           :as $.aws.loadnet.peer]
            [convex.aws.loadnet.peer.etch      :as $.aws.loadnet.peer.etch]
            [convex.aws.loadnet.rpc            :as $.aws.loadnet.rpc]
            [convex.aws.loadnet.stack-set      :as $.aws.loadnet.stack-set]
            [convex.cell                       :as $.cell]
            [taoensso.timbre                   :as log]))


(declare start
         stop)


;; On non-US machines, makes `format` behaves consistently with the rest
;; of the system.
;
(Locale/setDefault Locale/US)


;;;;;;;;;;


(defn create

  "Creates and start a loadnet on AWS.

   Provisions the required number of Peers as well as Load Generators that
   will simulate User transactions. Logs the whole process and collects a
   whole series of performance metrics.


   Env is a Map providing options:

     `:convex.aws/account`
        AWS account number.
        Loadnet will be deployed as an AWS CloudFormation stack set in this account.

        It must have self-managed permissions by granting it the `AWSCloudFormationStackSetAdministrationRole`
        role and the `AWSCloudFormationStackSetExecutionRole` role.

        See [this guide](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/stacksets-prereqs-self-managed.html).

        Mandatory.

     `:convex.aws/region+`
       Vector of supported AWS regions (as Strings).
       Currently: ap-southeast-1, eu-central-1, us-east-1, us-west-1
       Mandatory.

     `:convex.aws.key/file`
       Path to private key used for connecting to all EC2 instances.
       The same key pair must be available in all regions under the same name.
       Mandatory.

     `:convex.aws.loadnet/dir`
       Path to directory where all data, metrics, and statistics will be persisted.
       Defaults to `\"./\"`.

     `:convex.aws.loadnet/timer`
       Number of minutes the simulation load will run before shutting down the loadnet.
       Defaults to `nil`, meaning the simulation must be stopped manually.
       For meaningful results, run for at least 5 minutes.
       See [[stop]].

     `:convex.aws.loadnet.peer/native?`
       If `true` (default), Peers will run on the JVM (advised for much better performance).
       If `false`, they will run natively, compiled with GraalVM Native-Image.

     `:convex.aws.loadnet.scenario/path`
       Actor path to a simulation scenario from the following repository (as a List).

     `:convex.aws.loadnet.scenario/param+`
       Configuration for the simulation scenario chosen in `:convex.aws.loadnet.scenario/path`
       (as a Map).
    
     `:convex.aws.region/n.peer`
       Number of Load Generators to deploy per region.
       Defaults to `3`.

     `:convex.aws.region/n.peer`
       Number of Peers to deploy per region.
       Defaults to `8`.
    
     `:convex.aws.stack/parameter+`
       Map of parameters for the CloudFormation template:

          `:DetailedMonitoring`
            Enables 1-minute resolution on CloudWatch metrics (but incurs extra cost).
            Defaults to `\"true\"` (Boolean as a String).

          `:KeyName`
            Name of the key pair as registered on AWS.
            Related to the `:convex.aws.key/file` option.

          `:InstanceTypeLoad`
            EC2 instance type to use for Load Generators.
            Defaults to `\"t2.micro\"`.

          `:InstanceTypePeer`
            EC2 instance type to use for Peers.
            Defaults to `\"m4.2xlarge\"`.
    
     `:convex.aws.stack/tag+`
       Map of AWS tags to attach to the CloudFormation stack set, thus on all created
       resources.


   Uses the [Cognitect AWS API](https://github.com/cognitect-labs/aws-api).
   Follow instructions there for credentials.

   You must also create a `CloudWatchAgentServerRole` that Peer instances will use.
   See [this guide](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/create-iam-roles-for-cloudwatch-agent-commandline.html).

   
   Returns `env` augmented with extra information.


   E.g. Simulation of Automated Market Maker operations over 10 Peers in
        a single region, with 20 Load Generators deployed alongside to
        emulate 2000 Users trading between 5 fungible tokens:

   ```clojure
   (def env
        (create {:convex.aws/account                 \"513128561298\"
                 :convex.aws/region+                 [\"eu-central-1\"]
                 :convex.aws.key/file                \"some/dir/Test\"
                 :convex.aws.loadnet/dir             \"/tmp/loadnet\"
                 :convex.aws.loadnet/timer           10
                 :convex.aws.loadnet.scenario/path   '(lib sim scenario torus)
                 :convex.aws.loadnet.scenario/param+ {:n.token 5
                                                      :n.user  2000}
                 :convex.aws.region/n.load           20
                 :convex.aws.region/n.peer           10
                 :convex.aws.stack/parameter+        {:KeyName \"Test\"}}
                 :convex.aws.stack/tag+              {:Project \"Foo\"}}))
   ```"

  [env]

  (assert (env :convex.aws/account))
  (assert (not-empty (env :convex.aws/region+)))
  (assert (get-in env
                  [:convex.aws.stack/parameter+
                   :KeyName]))
  (assert (env :convex.aws.loadnet.scenario/path))
  (assert (env :convex.aws.loadnet.scenario/param+))
  (let [stack-set-name (or (env :convex.aws.stack/name)
                           (str "LoadNet-"
                                (System/currentTimeMillis)))
        dir            (or (env :convex.aws.loadnet/dir)
                           $.aws.loadnet.default/dir)
        dir-2          (-> (format "%s/%s"
                                   dir
                                   (string/replace stack-set-name
                                                   #"\s"
                                                   "_"))
                            (bb.fs/canonicalize)
                            (str))]
    (bb.fs/create-dirs dir-2)
    (-> env
        (assoc :convex.aws.loadnet/dir            dir-2
               :convex.aws.loadnet/*stopped?      (atom false)
               :convex.aws.loadnet.load/*stopped+ (atom #{})
               :convex.aws.stack/name             stack-set-name)
        (update :convex.aws.loadnet.peer/native?
                #(or %
                     $.aws.loadnet.default/peer-native?))
        (update :convex.aws.loadnet.scenario/path
                $.cell/any)
        (update :convex.aws.loadnet.scenario/param+
                $.cell/any)
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



(defn- delete

  ;; TODO. Document.

  [env]

  (-> env
      (merge (edn/read-string (slurp (format "%s/run.edn"
                                             (env :convex.aws.loadnet/dir)))))
      ($.aws.loadnet.cloudformation/client)
      ($.aws.loadnet.stack-set/delete)))



(defn start

  "If [[create]] has trouble awaiting SSH connections to EC2 instances (as logged),
   run this with the returned `env` to try starting the simulation again on currently
   deployed machines."

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
        (if timer
          (do
            (log/info (format "Simulation will stop in %d minute(s)"
                              timer))
            (assoc env-3
                   :convex.aws.loadnet/f*timer
                   (future
                     (Thread/sleep (* timer ; minutes
                                      60
                                      1000))
                     (when-not @(env-3 :convex.aws.loadnet/*stopped?)
                       (try
                         (stop env-3)
                         (catch Exception ex
                           (log/error ex
                                      "While stopping loadnet")
                           (throw ex)))))))
          env-3))
      (do
        (log/error "Wait a bit and try starting the simulation")
        env-2))))



(defn stop

  "Stops a simulation and deletes the whole CloudFormation stack set.

   Key data and metrics will be logged to STDOUT.
  
   Returns `env` augmented with that kind of results."

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


  (future
    (def env
         (create {:convex.aws/account                 (System/getenv "CONVEX_AWS_ACCOUNT")
                  :convex.aws/region+                 ["eu-central-1"
                                                       ;"us-east-1"
                                                       ;"us-west-1"
                                                       ;"ap-southeast-1"
                                                       ]
                  :convex.aws.key/file                "/Users/adam/Code/convex/clj/private/Test"
                  :convex.aws.loadnet/dir             "/tmp/loadnet"
                  :convex.aws.loadnet/timer           1
                  ;:convex.aws.loadnet.peer/native?    true
                  :convex.aws.loadnet.scenario/path   '(lib sim scenario torus)
                  :convex.aws.loadnet.scenario/param+ {:n.token 5
                                                       :n.user  30}
                  :convex.aws.region/n.load           1
                  :convex.aws.region/n.peer           2
                  :convex.aws.stack/parameter+        {:DetailedMonitoring "false"
                                                       :KeyName            "Test"
                                                       ;:InstanceTypePeer   "t2.micro"
                                                       }
                  :convex.aws.stack/tag+              {:Project "Ontochain"}})))


  ;; If awaiting SSH servers fail, run this to start the load.
  ;
  (future
    (def env
         (start env)))


  (future
    (stop env)
    nil)


  (future
    (delete {:convex.aws/account     (System/getenv "CONVEX_AWS_ACCOUNT")
             :convex.aws.loadnet/dir "/private/tmp/loadnet/LoadNet-1687257891873"})
    nil)


  )
