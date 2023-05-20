(ns convex.aws.loadnet

  (:refer-clojure :exclude [await])
  (:require [clojure.data.json         :as json]
            [clojure.string            :as string]
            [cognitect.aws.client.api  :as aws]
            [cognitect.aws.credentials :as aws.cred]
            [protosens.process         :as P.process]))


(declare ip+
         status)


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
                                [(format "IpPeer%d"
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


(defn await

  [client stack]

  (let [status- (status client
                        stack)]
    (case status-
      ;;
      "CREATE_IN_PROGRESS"
      (do
        (Thread/sleep 2000)
        (recur client
               stack))
      ;;
      "CREATE_COMPLETE"
      nil
      ;;
      (throw (ex-info "Something failed while creating the stack"
                      {:convex.aws.stack/status status-})))))



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
                             (System/currentTimeMillis)))
         stack      (-> (aws/invoke client
                                    {:op      :CreateStack
                                     :request {;:OnFailure  "DELETE"
                                               :Parameters  (-request-param+ param+)
                                               :StackName    stack-name
                                               :Tags         (:Tags option+)
                                               :TemplateBody (json/write-str (template option+))}})
                        (-validate-result)
                        (assoc :StackName
                               stack-name))]
     (await client
            stack)
     (ip+ client
          stack))))



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

  (let [output+ (:Outputs (describe client
                                    stack))]
    (assoc stack
           :convex.aws.ip/peer+
           (->> output+
                (keep (fn [output]
                       (let [^String k (output :OutputKey)]
                         (when (string/starts-with? k
                                                    "IpPeer")
                           [(Integer/parseInt (.substring k
                                                          6))
                            (output :OutputValue)]))))
                (sort-by first)
                (mapv second)))))



(defn ready?

  [client stack]

  (= (status client
             stack)
     "CREATE_COMPLETE"))



(defn resrc+

  [client stack]

  (-> (aws/invoke client
                  {:op      :DescribeStackResources
                   :request (select-keys stack
                                         [:StackName])})
      (-validate-result)
      (:StackResources)))



(defn status

  [client stack]

  (:StackStatus (describe client
                          stack)))


;;;;;;;;;; Remote commands


(defn ssh

  [key ip command]

  (P.process/shell (concat ["ssh"
                            "-i" key
                            "-o" "StrictHostKeyChecking=no"
                            (str "ubuntu@"
                                 ip)]
                           command)))



(defn cvx

  [key ip]

  (P.process/shell ["ssh"
                    "-i" key
                    "-o" "StrictHostKeyChecking=no"
                    (str "ubuntu@"
                         ip)
                    "cvx"
                    "'(.worker.start {:pipe \"peer\"})'"
                    "&"]
                   ))




;;;;;;;;;;


(comment


  (def c (client))


  (sort (keys (aws/ops c))) ; :CreateStack  :DeleteStack

  (aws/doc c :DescribeStacks)


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
  (status c s)


  (cost c {:KeyName "foo"})


  (def p
       (ssh "/Users/adam/Desktop/Test.pem"
            (first (ip+ c s))
            ["cvx"
             "'(inc 42)'"
             ]))

  (def p
       (cvx "/Users/adam/Desktop/Test.pem"
            (first (ip+ c s))))

  (P.process/destroy p)



  )
