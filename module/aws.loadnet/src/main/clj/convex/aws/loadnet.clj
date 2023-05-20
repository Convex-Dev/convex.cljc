(ns convex.aws.loadnet

  (:require [clojure.data.json         :as json]
            [clojure.string            :as string]
            [cognitect.aws.client.api  :as aws]
            [cognitect.aws.credentials :as aws.cred]
            [protosens.process         :as P.process]))


;;;;;;;;;; Private


(defn- -request-param+

  [param+]

  (into []
        (map (fn [[k v]]
               {:ParameterKey   (name k)
                :ParameterValue v}))
        param+))



(defn- -validate-result

  [result]

  (when (result :cognitect.anomalies/category)
    (throw (ex-info "Error while executing AWS operation"
                    result)))
  result)


;;;;;;;;;; CloudFormation templating


(defn template


  ([]

   (template nil))


  ([option+]

   (let [instance-type (or (:InstanceType option+)
                           "m4.2xlarge")
         volume-size   (:VolumeSize option+)
         ebs           (cond->
                         {:DeleteOnTermination true}
                         volume-size
                         (assoc :VolumeSize
                                volume-size))
         name-peer+    (mapv (fn [i-peer]
                               (str "Peer"
                                    i-peer))
                             (range (or (:n-peer option+)
                                        3)))]
     {:Description "Convex network for load testing"
      :Outputs     (into {}
                         (map (fn [i-peer name-peer]
                                [(str "Ip"
                                      i-peer)
                                 {:Description (format "%s public IP"
                                                       name-peer)
                                  :Value       {"Fn::GetAtt" [name-peer
                                                              "PublicIp"]}}])
                              (range)
                              name-peer+))
      :Parameters  {"KeyName" {:Type                  "AWS::EC2::KeyPair::KeyName"
                               :Description           "Name of an existing EC2 KeyPair to enable SSH access to peers"
                               :ConstraintDescription "Must be the name of an existing EC2 KeyPair"}}
      :Resources   (into {"SecurityGroup" {:Type       "AWS::EC2::SecurityGroup"
                                           :Properties {:GroupDescription     "Network access for peers"
                                                        :GroupName            {:Ref "AWS::StackName"}
                                                        :SecurityGroupIngress [{:CidrIp      "0.0.0.0/0"
                                                                                :Description "SSH"
                                                                                :FromPort    "22"
                                                                                :IpProtocol  "tcp"
                                                                                :ToPort      "22"}
                                                                               {:CidrIp      "0.0.0.0/0"
                                                                                :Description "Standard port for Convex peers"
                                                                                :FromPort    "18888"
                                                                                :IpProtocol  "tcp"
                                                                                :ToPort      "18888"}]}}}
                         (map (fn [i-peer name-peer]
                                [name-peer
                                 {:Type       "AWS::EC2::Instance"
                                  :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                                                      :Ebs        ebs}]
                                               :ImageId             "ami-0653c4fba21e9ee3c"
                                               :InstanceType        instance-type
                                               :KeyName             {:Ref "KeyName"}
                                               :SecurityGroups      [{:Ref "SecurityGroup"}]
                                               :Tags                [{:Key   "Name"
                                                                      :Value {"Fn::Join" [" "
                                                                                          [{:Ref "AWS::StackName"}
                                                                                           (str "Peer " i-peer)]]}}]}}])
                              (range)
                              name-peer+))})))


;;;;;;;;;; Stack operations


(defn client


  ([]

   (client nil))


  ([option+]

   (aws/client (merge {:api    :cloudformation
                       :region "eu-central-1"}
                      (when (contains? option+
                                       :access-key-id)
                        {:credentials-provider (aws.cred/basic-credentials-provider option+)})))))


;;;


(defn cost


  ([client param+]

   (cost client
         param+
         nil))


  ([client param+ option+]

   (-> (aws/invoke client
                   {:op      :EstimateTemplateCost
                    :request {:Parameters   (-request-param+ param+)
                              :TemplateBody (json/write-str (template option+))}})
       (-validate-result)
       (:Url))))



(defn create


  ([client param+]

   (create client
           param+
           nil))


  ([client param+ option+]

   (let [stack-name (or (:StackName option+)
                        (str "LoadNet-"
                             (System/currentTimeMillis)))]
     (-> (aws/invoke client
                     {:op      :CreateStack
                      :request {;:OnFailure  "DELETE"
                                :Parameters  (-request-param+ param+)
                                :StackName    stack-name
                                :Tags         (:Tags option+)
                                :TemplateBody (json/write-str (template option+))}})
         (-validate-result)
         (assoc :StackName
                stack-name)))))



(defn describe

  [client stack]

  (-> (aws/invoke client
                  {:op      :DescribeStacks
                   :request (select-keys stack
                                         [:StackName])})
      (-validate-result)
      (:Stacks)
      (first)))



(defn delete

  [client stack]
  
  (-> (aws/invoke client
                  {:op      :DeleteStack
                   :request (select-keys stack
                                         [:StackName])})
      (-validate-result))
  nil)



(defn ip+

  [client stack]

  (-> (describe client
                stack)
      (:Outputs)
      (->> (into []
                 (keep (fn [output]
                         (when (string/starts-with? (output :OutputKey)
                                                    "Ip")
                           (output :OutputValue))))))))



(defn ready?

  [client stack]

  (= (:StackStatus (describe client
                             stack))
     "CREATE_COMPLETE"))



(defn resrc+

  [client stack]

  (-> (aws/invoke client
                  {:op      :DescribeStackResources
                   :request (select-keys stack
                                         [:StackName])})
      (-validate-result)
      (:StackResources)))


;;;;;;;;;;


(comment


  (def c (client))



  (sort (keys (aws/ops c))) ; :CreateStack  :DeleteStack

  (aws/doc c :EstimateTemplateCost)



  (def s (create c
                 {:KeyName "Test"}
                 {:n-peer 1
                  :InstanceType "t2.micro"
                  :Tags [{:Key   "Project"
                          :Value "Ontochain"}]}))

  (delete c s)

  (ready? c s)
  (describe c s)
  (resrc+ c s)
  (ip+ c s)

  (cost c {:KeyName "foo"})



  )
