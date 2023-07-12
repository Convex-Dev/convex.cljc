(ns convex.aws.loadnet.ec2

  "Direct operations on EC2 instances."

  (:require [convex.aws :as $.aws]))


;;;;;;;;;;


(defn client+

  "Creates EC2 clients for all regions in the stack set."

  [env]

  (assoc env
         :convex.aws.client/ec2+
         (into {}
               (map (fn [region]
                      [region
                       ($.aws/client :ec2
                                     region
                                     env)]))
               (env :convex.aws/region+))))

;;;;;;;;;;


(defn- -invoke

  [env region op request]

  ($.aws/invoke (get-in env
                        [:convex.aws.client/ec2+
                         region])
                op
                request))


;;;


(defn terminate+

  "Terminates instance in the given `region` by ID."

  [env region instance-id+]

  (-> (-invoke env
               region
               :TerminateInstances
               {:InstanceIds instance-id+})
      (:instancesSet)))
