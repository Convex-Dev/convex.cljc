(ns convex.aws.loadnet.stack

  "AWS operations relating to CloudFormation stacks."

  (:require [clojure.string                    :as string]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]))


(declare output+
         resrc+)


;;;;;;;;;; Private


(defn- -stack-name

  ;; Retrieves the cached stack name of the given region.

  [env region]

  (get-in env
          [:convex.aws.stack/region->id
           region]))


;;;;;;;;;; Public


(defn describe

  "Queries a description the stack of the given `region`."

  [env region]

  (-> ($.aws.loadnet.cloudformation/invoke
        env
        region
        :DescribeStacks
        {:StackName (-stack-name env
                                 region)})
         
      (:Stacks)
      (first)))



(defn instance+

  "Queries a Vector of information about instances in the given `region`."

  [env region]

  (filterv (fn [resrc]
             (= (resrc :ResourceType)
                "AWS::EC2::Instance"))
           (resrc+ env
                   region)))



(defn ip+

  "Queries a Vector of instance IPs for the given `region`.
  
   First the IPs of load generators, then the IPs of peers."

  [env region]

  (let [result (output+ env
                        region)
        parse  (fn [str-filter]
                 (let [n-char (count str-filter)]
                   (->> result
                        (keep (fn [output]
                                (let [^String k (output :OutputKey)]
                                  (when (string/starts-with? k
                                                             str-filter)
                                    [(Integer/parseInt (.substring k
                                                                   n-char))
                                     (output :OutputValue)]))))
                        (sort-by first)
                        (mapv second))))]
    [(parse "IpLoad")
     (parse "IpPeer")]))



(defn output+

  "Queries the stack outputs of the given `region`."

  [env region]

  (-> (describe env
                region)
      (:Outputs)))



(defn peer-instance+

  "Queries a Vector of information about peer instances in the given `region`."

  [env region]

  (filterv (fn [resrc]
             (string/starts-with? (resrc :LogicalResourceId)
                                  "Peer"))
           (resrc+ env
                   region)))



(defn peer-instance-id+

  "Queries a Vector of peer instance IDs for the given `region`."

  [env region]

  (mapv :PhysicalResourceId
        (peer-instance+ env
                        region)))



(defn resrc+

  "Queries a sequence of resources for the stack in the `given` region."
  ;; Note: limited to 100 resources, use `ListStackResources` if more is expected.

  [env region]

  (-> ($.aws.loadnet.cloudformation/invoke
        env
        region
        :DescribeStackResources
        {:StackName (-stack-name env
                                 region)})
      (:StackResources)))



(defn status

  "Queries the current status of the stack in the given `region`."

  [env region]

  (-> (describe env
                region)
      (:StackStatus)))


;;;;;;;;;; TODO


;; Adapt to stack sets.
;
; (defn cost
; 
;   [env]
; 
;   (-> (-invoke env
;                :EstimateTemplateCost
;                {:Parameters   (-param+ (update-in env
;                                                   [:convex.aws.stack/parameter+
;                                                    :KeyName]
;                                                   #(or %
;                                                        "Test")))
;                 :TemplateBody (json/write-str ($.aws.loadnet.template/net env))})
;        (:Url)))
