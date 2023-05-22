(ns convex.aws.loadnet.stack

  (:require [clojure.string                    :as string]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]))


(declare output+
         resrc+)


;;;;;;;;;; Private


(defn- -stack-name

  [env region]

  (get-in env
          [:convex.aws.stack/region->id
           region]))


;;;;;;;;;; Public


(defn describe

  [env region]

  (-> ($.aws.loadnet.cloudformation/invoke
        env
        region
        :DescribeStacks
        {:StackName (-stack-name env
                                 region)})
         
      (:Stacks)
      (first)))



(defn ip-peer+

  [env region]

  (->> (output+ env
                region)
       (keep (fn [output]
              (let [^String k (output :OutputKey)]
                (when (string/starts-with? k
                                           "IpPeer")
                  [(Integer/parseInt (.substring k
                                                 6))
                   (output :OutputValue)]))))
       (sort-by first)
       (mapv second)))



(defn output+

  [env region]

  (-> (describe env
                region)
      (:Outputs)))



(defn peer-instance+

  [env region]

  (filterv (fn [resrc]
             (= (resrc :ResourceType)
                "AWS::EC2::Instance"))
           (resrc+ env
                   region)))



(defn peer-instance-id+

  [env region]

  (mapv :PhysicalResourceId
       (peer-instance+ env
                       region)))



(defn resrc+

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
