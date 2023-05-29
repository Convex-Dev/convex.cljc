(ns convex.aws.loadnet.stack-set.op

  (:require [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.stack          :as $.aws.loadnet.stack]
            [convex.clj                        :as $.clj]
            [convex.cell                       :as $.cell]
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

  (log/info "Creating regional stacks")
  (let [n-region      (count (env :convex.aws/region+))
        n-load-region (env :convex.aws.region/n.load)
        n-load        (* n-region
                         n-load-region)
        n-peer-region (env :convex.aws.region/n.peer)]
    (log/info (format "%d load generator(s) per region = %d load generators(s)"
                      n-load-region
                      n-load))
    (when-not (zero? n-load)
      (log/info (format "Simulated users per load generator = %.2f"
                        (double (/ (or (some-> (get-in env
                                                       [:convex.aws.loadnet.scenario/param+
                                                        ($.cell/* :n.user)])
                                               ($.clj/long))
                                       100)
                                   n-load)))))
    (log/info (format "%d peer(s) per region = %d peer(s)"
                      n-peer-region
                      (* n-peer-region
                         n-region))))
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



(defn ip+

  [env]

  (let [region+   (env :convex.aws/region+)
        n-region  (count region+)
        env-2     (reduce (fn [env-2 [region f*ip+]]
                            (let [[ip-load+
                                   ip-peer+] @f*ip+]
                              (log/info (format "Done retrieving IP addresses for region '%s'"
                                                region))
                              (-> env-2
                                  (update :convex.aws.ip/load+
                                          (fnil into
                                                [])
                                          ip-load+)
                                  (update :convex.aws.ip/peer+
                                          (fnil into
                                                [])
                                          ip-peer+))))
                          env
                          (mapv (fn [region]
                                  (log/info (format "Retrieving IP addresses for region '%s'"
                                                    region))
                                  [region
                                   (future
                                     ($.aws.loadnet.stack/ip+ env
                                                              region))])
                                region+))
        print-ip+ (fn [k-ip+ str-name]
                    (let [ip+ (get env-2
                                   k-ip+)]
                      (doseq [[i-ip
                               [region
                                ip]]   (partition 2
                                                  (interleave (range)
                                                              (mapcat (fn [[region region-ip+]]
                                                                        (map (juxt (constantly region)
                                                                                   identity)
                                                                             region-ip+))
                                                                      (partition 2
                                                                                 (interleave region+
                                                                                             (partition (long (/ (count ip+)
                                                                                                                 n-region))
                                                                                                        ip+))))))]
                        (log/info (format "%s %d IP = %s (%s)"
                                          str-name
                                          i-ip
                                          ip
                                          region)))))]
    (print-ip+ :convex.aws.ip/load+
               "Load")
    (print-ip+ :convex.aws.ip/peer+
               "Peer")
    (log/info "Done retrieving all IP addresses")
    env-2))



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
