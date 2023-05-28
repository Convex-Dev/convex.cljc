(ns convex.aws.loadnet.template

  (:require [convex.aws.loadnet.default :as $.aws.loadnet.default]))


;;;;;;;;;;


(defn net


  ([]

   (net nil))


  ([env]

   (let [volume-size   (:convex.aws.instance/volume env)
         ebs           (cond->
                         {:DeleteOnTermination true}
                         volume-size
                         (assoc :VolumeSize
                                volume-size))
         name-load+    (mapv (fn [i-load]
                               (str "Load"
                                    i-load))
                             (range (or (:convex.aws.region/n.load env)
                                        $.aws.loadnet.default/n-load)))
         name-peer+    (mapv (fn [i-peer]
                               (str "Peer"
                                    i-peer))
                             (range (or (:convex.aws.region/n.peer env)
                                        $.aws.loadnet.default/n-peer)))]
     {:Description "Convex network for load testing"
      :Mappings    {"RegionalAMI" {"ap-southeast-1" {"AMI" "ami-0d8378a705bbaad85"}
                                   "eu-central-1"   {"AMI" "ami-05028054e79ce04d4"}
                                   "us-east-1"      {"AMI" "ami-0f06c7dc7086d1fe5"}
                                   "us-west-1"      {"AMI" "ami-0f523bb7cfa9fe01c"}}}
      :Outputs     (-> {}
                       (into (map (fn [i-load name-load]
                                    [(format "IpLoad%d"
                                             i-load)
                                     {:Description (format "%s public IP"
                                                           name-load)
                                      :Value       {"Fn::GetAtt" [name-load
                                                                  "PublicIp"]}}])
                                  (range)
                                  name-load+))
                       (into (map (fn [i-peer name-peer]
                                    [(format "IpPeer%d"
                                             i-peer)
                                     {:Description (format "%s public IP"
                                                           name-peer)
                                      :Value       {"Fn::GetAtt" [name-peer
                                                                  "PublicIp"]}}])
                                  (range)
                                  name-peer+)))
      :Parameters  {"DetailedMonitoring" {:Type        "String"
                                          :Description "Enables detailed monitoring (defaults to true)"
                                          :Default     "true"}
                    "KeyName"            {:Type                  "AWS::EC2::KeyPair::KeyName"
                                          :Description           "Name of an existing EC2 KeyPair to enable SSH access to peers"
                                          :ConstraintDescription "Must be the name of an existing EC2 KeyPair"}
                    "InstanceTypeLoad"   {:Type        "String"
                                          :Default     $.aws.loadnet.default/instance-type
                                          :Description "Instance type to be used for load generators"}
                    "InstanceTypePeer"   {:Type        "String"
                                          :Default     $.aws.loadnet.default/instance-type
                                          :Description "Instance type to be used for peers"}
                    "RolePeer"           {:Type        "String"
                                          :Default     "CloudWatchAgentServerRole"
                                          :Description "IAM role for Peers, see https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/create-iam-roles-for-cloudwatch-agent-commandline.html"}}
      :Resources   (-> {"SecurityGroup" {:Type       "AWS::EC2::SecurityGroup"
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
                       (into (map (fn [i-load name-load]
                                    [name-load
                                     {:Type       "AWS::EC2::Instance"
                                      :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                                                          :Ebs        ebs}]
                                                   :ImageId             {"Fn::FindInMap" ["RegionalAMI"
                                                                                          {:Ref "AWS::Region"}
                                                                                          "AMI"]}
                                                   :InstanceType        {:Ref "InstanceTypeLoad"}
                                                   :KeyName             {:Ref "KeyName"}
                                                   :SecurityGroups      [{:Ref "SecurityGroup"}]
                                                   :Tags                [{:Key   "Name"
                                                                          :Value {"Fn::Join" [" "
                                                                                              [{:Ref "AWS::StackName"}
                                                                                               (str "Load " i-load)]]}}]}}])
                                  (range)
                                  name-load+))
                       (into (map (fn [i-peer name-peer]
                                    [name-peer
                                     {:Type       "AWS::EC2::Instance"
                                      :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                                                          :Ebs        ebs}]
                                                   :IamInstanceProfile  {:Ref "RolePeer"}
                                                   :ImageId             {"Fn::FindInMap" ["RegionalAMI"
                                                                                          {:Ref "AWS::Region"}
                                                                                          "AMI"]}
                                                   :InstanceType        {:Ref "InstanceTypePeer"}
                                                   :Monitoring          {:Ref "DetailedMonitoring"}
                                                   :KeyName             {:Ref "KeyName"}
                                                   :SecurityGroups      [{:Ref "SecurityGroup"}]
                                                   :Tags                [{:Key   "Name"
                                                                          :Value {"Fn::Join" [" "
                                                                                              [{:Ref "AWS::StackName"}
                                                                                               (str "Peer " i-peer)]]}}]}}])
                                  (range)
                                  name-peer+))
                       )})))
