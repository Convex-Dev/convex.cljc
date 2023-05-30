(ns convex.aws.loadnet.stack-set

  (:require [clojure.data.json                 :as json]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.stack-set.op   :as $.aws.loadnet.stack-set.op]
            [convex.aws.loadnet.template       :as $.aws.loadnet.template]
            [convex.clj                        :as $.clj]
            [taoensso.timbre                   :as log]))


;;;;;;;;;;


(defn create

  [env]

  (let [stack-set-name (env :convex.aws.stack/name)
        region+        (env :convex.aws/region+)
        parameter+     (env :convex.aws.stack/parameter+)
        _              (do
                         (log/info (format "Creating stack set named '%s' for %d region(s)"
                                           stack-set-name
                                           (count region+)))
                         (doseq [[i-region
                                  region]  (partition 2
                                                      (interleave (range)
                                                                  region+))]
                           (log/info (format "Region %d = %s"
                                             i-region
                                             region)))
                         (log/info (format "Load generator instance type = %s"
                                           (parameter+ :InstanceTypeLoad)))
                         (log/info (format "Peer instance type = %s"
                                           (parameter+ :InstanceTypePeer)))
                         (log/info (format "Peers will run %s"
                                           (if (env :convex.aws.loadnet.peer/native?)
                                             "natively"
                                             "on the JVM")))
                         (log/info (format "EC2 Detailed Monitoring on peer instances = %s"
                                           (parameter+ :DetailedMonitoring)))
                         (log/info (format "Scenario path = %s"
                                           (env :convex.aws.loadnet.scenario/path)))
                         (log/info (format "Scenario parameters = %s"
                                           (env :convex.aws.loadnet.scenario/param+))))
        result         ($.aws.loadnet.cloudformation/invoke
                         env
                         :CreateStackSet
                         {:Description  "Stack set forming a loadnet"
                          :Parameters   ($.aws.loadnet.cloudformation/param+ env)
                          :StackSetName stack-set-name
                          :Tags         (mapv (fn [[k v]]
                                                {:Key   (name k)
                                                 :Value v})
                                              (env :convex.aws.stack/tag+))
                          :TemplateBody (json/write-str ($.aws.loadnet.template/net env))})
        [ok?
         env-2]         (-> env
                            (assoc :convex.aws.stack-set/id   (result :StackSetId)
                                   :convex.aws.stack-set/name stack-set-name)
                            ($.aws.loadnet.stack-set.op/create))]
    (if ok?
      (let [env-3 (-> env-2
                      ($.aws.loadnet.stack-set.op/region->id)
                      ($.aws.loadnet.stack-set.op/ip+))]
        (spit (format "%s/run.edn"
                      (env-3 :convex.aws.loadnet/dir))
              (-> env-3
                  (select-keys [:convex.aws/region+
                                :convex.aws.region/n.load
                                :convex.aws.region/n.peer
                                :convex.aws.ip/load+
                                :convex.aws.ip/peer+
                                :convex.aws.loadnet.scenario/path
                                :convex.aws.loadnet.scenario/param+
                                :convex.aws.stack/parameter+
                                :convex.aws.stack/region->id
                                :convex.aws.stack/tag+
                                :convex.aws.stack-set/name])
                  (update :convex.aws.loadnet.scenario/path
                          $.clj/any)
                  (update :convex.aws.loadnet.scenario/param+
                          $.clj/any)))
        env-3)
      (do
        (log/error "Failed to create stacks, check your AWS console")
        env-2))))



(defn delete

  [env]

  (let [d*delete (delay
                   (log/info "Deleting stack set")
                   ($.aws.loadnet.cloudformation/invoke
                     env
                     :DeleteStackSet
                     {:StackSetName (env :convex.aws.stack-set/name)})
                   (log/info "Stack set deleted"))]
    (if (env :convex.aws.stack/region->id)
      (let [[ok?
             env-2] ($.aws.loadnet.stack-set.op/delete env)]
        (if ok?
          (do
            @d*delete
            (dissoc env-2
                    :convex.aws.stack/region->id))
          (do
            (log/error "Cannot delete the stack set")
            env)))
      (do
        @d*delete
        env))))



(defn describe

  [env]

  (-> ($.aws.loadnet.cloudformation/invoke env
                                           :DescribeStackSet
                                           {:StackSetName (env :convex.aws.stack-set/name)})
      (:StackSet)
      (dissoc :TemplateBody)))
