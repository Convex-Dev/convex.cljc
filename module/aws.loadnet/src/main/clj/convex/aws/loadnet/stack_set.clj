(ns convex.aws.loadnet.stack-set

  (:require [clojure.data.json                 :as json]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.default        :as $.aws.loadnet.default]
            [convex.aws.loadnet.peer           :as $.aws.loadnet.peer]
            [convex.aws.loadnet.stack-set.op   :as $.aws.loadnet.stack-set.op]
            [convex.aws.loadnet.rpc            :as $.aws.loadnet.rpc]
            [convex.aws.loadnet.template       :as $.aws.loadnet.template]
            [taoensso.timbre                   :as log]))


;;;;;;;;;;


(defn create

  [env]

  (let [stack-set-name (or (:convex.aws.stack/name env)
                           (str "LoadNet-"
                                (System/currentTimeMillis)))
        region+        (env :convex.aws/region+)
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
                         (log/info (format "Peer instance type = %s"
                                           (or (get-in env
                                                       [:convex.aws.stack/parameter+
                                                        :PeerInstanceType])
                                               $.aws.loadnet.default/instance-type)))
                         (log/info (format "Scenario path = %s"
                                           (or (env :convex.aws.loadnet.scenario/path)
                                               (throw (IllegalArgumentException. "Missing scenario path")))))
                         (log/info (format "Scenario parameters = %s"
                                           (or (env :convex.aws.loadnet.scenario/param+)
                                               (throw (IllegalArgumentException. "Missing scenario parameters"))))))
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
                      ($.aws.loadnet.stack-set.op/ip-peer+))]
        (spit (format "%s/run.edn"
                      (env-3 :convex.aws.loadnet/dir))
              (select-keys env-3
                           [:convex.aws/region+
                            :convex.aws.region/n.peer
                            :convex.aws.ip/peer+
                            :convex.aws.stack/parameter+
                            :convex.aws.stack/region->id
                            :convex.aws.stack/tag+
                            :convex.aws.stack-set/name]))
        (let [env-4 ($.aws.loadnet.rpc/await-ssh env-3)]
          (if (env-4 :convex.aws.loadnet/ssh-ready?)
            ($.aws.loadnet.peer/start env-4)
            (do
              (log/error "Wait a bit and try starting peers manually")
              env-4))))
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
