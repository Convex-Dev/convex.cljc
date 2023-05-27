(ns convex.aws.loadnet.stack-set.op

  (:require [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.stack          :as $.aws.loadnet.stack]
            [taoensso.timbre                   :as log]))


;;;;;;;;;; Stack set operations


(defn- -stack-set-op

  [env op request msg-ok msg-fail]

  (let [result ($.aws.loadnet.cloudformation/invoke
                 env
                 op
                 (assoc request
                        :OperationPreferences
                        {:RegionConcurrencyType "PARALLEL"}))
        op-id  (result :OperationId)]
    (loop []
      (let [status (-> ($.aws.loadnet.cloudformation/invoke
                         env
                         :DescribeStackSetOperation
                         {:OperationId  op-id
                          :StackSetName (env :convex.aws.stack-set/name)})
                       (get-in [:StackSetOperation
                                :Status]))]
        (if (= status
               "RUNNING")
          (do
            (Thread/sleep 2000)
            (recur))
          [(if (= status
                  "SUCCEEDED")
             (do
               (log/info msg-ok)
               true)
             (do
               (log/error msg-fail)
               false))
           env])))))


;;;


(defn create

  [env]

  (let [n-peer (env :convex.aws.region/n.peer)]
    (log/info (format "Creating regional stacks, %d peer(s) per region = %d peer(s)"
                      n-peer
                      (* n-peer
                         (count (env :convex.aws/region+))))))
  (-stack-set-op env
                 :CreateStackInstances
                 {:Accounts     [(env :convex.aws/account)]
                  :Regions      (env :convex.aws/region+)
                  :StackSetName (env :convex.aws.stack-set/name)}
                 "All stacks created"
                 "Failed to create stacks"))



(defn delete

  [env]

  (log/info "Deleting stacks")
  (-stack-set-op env
                 :DeleteStackInstances
                 {:Accounts     [(env :convex.aws/account)]
                  :Regions      (env :convex.aws/region+)
                  :RetainsStack false
                  :StackSetName (env :convex.aws.stack-set/name)}
                 "All stacks deleted"
                 "Failed to delete stacks"))



(defn fetch

  [env]

  (into []
        (mapcat identity)
        (iteration (fn [next-token]
                     ($.aws.loadnet.cloudformation/invoke
                       env
                       :ListStackInstances
                       (cond->
                         {:StackSetName (env :convex.aws.stack-set/name)}
                         next-token
                         (assoc :NextToken
                                next-token))))
                   {:kf :NextToken
                    :vf :Summaries})))



(defn ip-peer+

  [env]

  (let [region+  (env :convex.aws/region+)
        n-region (count region+)
        result   (into []
                       (mapcat (fn [[region f*ip+]]
                                 (let [ip+ @f*ip+]
                                   (log/info (format "Done retrieving peer IP addresses for region '%s'"
                                                     region))
                                   ip+)))
                       (mapv (fn [region]
                               (log/info (format "Retrieving peer IP addresses for region '%s'"
                                                 region))
                               [region
                                (future
                                  ($.aws.loadnet.stack/ip-peer+ env
                                                                region))])
                             region+))]
    (doseq [[i-peer
             ip]    (partition 2
                               (interleave (range)
                                           result))]
      (log/info (format "Peer %d IP = %s (%s)"
                        i-peer
                        ip
                        (or (get region+
                                 (mod i-peer
                                      n-region))
                            "unknown region"))))
    (log/info "Done retrieving all peer IP addresses")
    (assoc env
           :convex.aws.ip/peer+
           result)))



(defn region->id

  [env]

  (log/info "Retrieving stack IDs")
  (let [result (into {}
                     (map (fn [stack]
                            [(stack :Region)
                             (stack :StackId)])
                     (fetch env)))]
    (log/info "Done retrieving stack IDs by region")
    (assoc env
           :convex.aws.stack/region->id
           result)))
